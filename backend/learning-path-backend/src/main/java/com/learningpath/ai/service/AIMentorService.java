package com.learningpath.ai.service;

import com.learningpath.ai.client.GeminiClient;
import com.learningpath.ai.dto.AiTestResponse;
import com.learningpath.ai.dto.AIMentorChatRequest;
import com.learningpath.ai.dto.AIMentorChatResponse;
import com.learningpath.ai.dto.LearnerAiContext;
import com.learningpath.entity.AIConversation;
import com.learningpath.entity.AIMessage;
import com.learningpath.entity.Course;
import com.learningpath.entity.User;
import com.learningpath.recommendation.client.MlRecommendationClient;
import com.learningpath.recommendation.dto.MlPredictionRequest;
import com.learningpath.recommendation.dto.MlPredictionResponse;
import com.learningpath.recommendation.service.LearnerFeatureBuilderService;
import com.learningpath.repository.AIConversationRepository;
import com.learningpath.repository.AIMessageRepository;
import com.learningpath.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIMentorService {

    private final GeminiClient geminiClient;
    private final LearnerContextService learnerContextService;
    private final LearnerFeatureBuilderService featureBuilderService;
    private final MlRecommendationClient mlRecommendationClient;
    private final CourseRepository courseRepository;
    private final AIConversationRepository conversationRepository;
    private final AIMessageRepository messageRepository;

    @Transactional
    public AIMentorChatResponse processChat(User user, AIMentorChatRequest request) {
        String userQuery = request.getMessage() != null ? request.getMessage().trim() : "What should I learn today?";

        // 1. Get or create active conversation
        AIConversation conversation;
        if (request.getConversationId() != null && !request.getConversationId().isBlank()) {
            try {
                UUID convId = UUID.fromString(request.getConversationId());
                conversation = conversationRepository.findById(convId)
                        .orElseGet(() -> createNewConversation(user));
            } catch (Exception e) {
                conversation = createNewConversation(user);
            }
        } else {
            conversation = conversationRepository.findFirstByUserOrderByCreatedAtDesc(user)
                    .orElseGet(() -> createNewConversation(user));
        }

        // 2. Persist user message
        AIMessage userMsg = AIMessage.builder()
                .conversation(conversation)
                .role("user")
                .content(userQuery)
                .build();
        messageRepository.save(userMsg);

        // 3. Build real database learner context
        LearnerAiContext context = learnerContextService.buildContext(user);

        // 4. Intent & Topic Entity Extraction
        MentorIntent intent = classifyIntent(userQuery);
        String extractedTopic = extractTopic(userQuery);

        // 5. Course Recommendation / Resource Alignment
        String topRecommendation = null;
        double mlConfidence = 0.0;
        List<AIMentorChatResponse.RecommendedResourceDto> resources = new ArrayList<>();

        if (intent != MentorIntent.GREETING && intent != MentorIntent.CASUAL_CONVERSATION) {
            if (extractedTopic != null && !extractedTopic.isBlank()) {
                // Topic-specific search in course catalog
                List<Course> matchedCourses = courseRepository.findByTitleContainingIgnoreCase(extractedTopic);
                if (!matchedCourses.isEmpty()) {
                    Course bestTopicCourse = matchedCourses.get(0);
                    topRecommendation = bestTopicCourse.getTitle();
                    mlConfidence = 0.90;
                    resources.add(AIMentorChatResponse.RecommendedResourceDto.builder()
                            .title(bestTopicCourse.getTitle())
                            .type("COURSE")
                            .difficulty(bestTopicCourse.getDifficulty() != null ? bestTopicCourse.getDifficulty().name() : "BEGINNER")
                            .url(bestTopicCourse.getUrl() != null ? bestTopicCourse.getUrl() : "/explore-courses")
                            .matchScore(90.0)
                            .build());
                }
            } else if (intent == MentorIntent.RECOMMENDATION_REQUEST || intent == MentorIntent.STUDY_PLAN_REQUEST) {
                // General ML-driven recommendation scan
                List<Course> availableCourses = courseRepository.findAll();
                if (!availableCourses.isEmpty() && context != null) {
                    List<Course> candidates = availableCourses.stream().limit(10).collect(Collectors.toList());
                    Course bestCourse = null;
                    double highestScore = -1.0;

                    for (Course course : candidates) {
                        MlPredictionRequest feat = featureBuilderService.buildFeatureVector(context, course);
                        Optional<MlPredictionResponse> mlRes = mlRecommendationClient.predict(feat);
                        double score = mlRes.map(MlPredictionResponse::recommendationScore).orElse(-1.0);

                        if (score > highestScore) {
                            highestScore = score;
                            bestCourse = course;
                        }
                    }

                    if (bestCourse != null && highestScore >= 0) {
                        topRecommendation = bestCourse.getTitle();
                        mlConfidence = highestScore / 100.0;
                        resources.add(AIMentorChatResponse.RecommendedResourceDto.builder()
                                .title(bestCourse.getTitle())
                                .type("COURSE")
                                .difficulty(bestCourse.getDifficulty() != null ? bestCourse.getDifficulty().name() : "INTERMEDIATE")
                                .url(bestCourse.getUrl() != null ? bestCourse.getUrl() : "/explore-courses")
                                .matchScore(highestScore)
                                .build());
                    }
                }
            }
        }

        // 6. Build Grounded Prompt for Gemini with strict context priority
        String prompt = buildPrompt(context, userQuery, intent, extractedTopic, topRecommendation, mlConfidence);

        String mentorReply;
        try {
            AiTestResponse aiResponse = geminiClient.generateContent(prompt);
            if (aiResponse != null && aiResponse.success() && aiResponse.response() != null && !aiResponse.response().isBlank()) {
                mentorReply = aiResponse.response().trim();
            } else {
                mentorReply = generateFallbackReply(userQuery, intent, extractedTopic, context, topRecommendation);
            }
        } catch (Exception e) {
            log.warn("Gemini service exception: {}. Using deterministic contextual mentor reply.", e.getMessage());
            mentorReply = generateFallbackReply(userQuery, intent, extractedTopic, context, topRecommendation);
        }

        // 7. Persist mentor response
        String activeTopic = extractedTopic != null ? extractedTopic :
                (topRecommendation != null ? topRecommendation : "General Mentorship");
        AIMessage mentorMsg = AIMessage.builder()
                .conversation(conversation)
                .role("mentor")
                .content(mentorReply)
                .topic(activeTopic)
                .recommendedAction(topRecommendation != null ? "Start " + topRecommendation : null)
                .build();
        messageRepository.save(mentorMsg);

        // 8. Structured Action Items & Follow-up Suggestions
        List<AIMentorChatResponse.MentorActionDto> actions = deriveActions(intent, extractedTopic, topRecommendation, context);
        List<String> followUps = deriveFollowUps(intent, extractedTopic);

        return AIMentorChatResponse.builder()
                .messageId(mentorMsg.getId().toString())
                .conversationId(conversation.getId().toString())
                .role("mentor")
                .reply(mentorReply)
                .topic(activeTopic)
                .confidenceScore(mlConfidence)
                .recommendedAction(topRecommendation != null ? "Start " + topRecommendation : null)
                .suggestedFollowUps(followUps)
                .actions(actions)
                .recommendedResources(resources)
                .build();
    }

    @Transactional(readOnly = true)
    public List<AIMessage> getHistory(User user) {
        return conversationRepository.findFirstByUserOrderByCreatedAtDesc(user)
                .map(conversation -> messageRepository.findByConversationOrderByCreatedAtAsc(conversation))
                .orElse(Collections.emptyList());
    }

    @Transactional
    public void clearHistory(User user) {
        conversationRepository.deleteByUser(user);
    }

    private AIConversation createNewConversation(User user) {
        AIConversation conversation = AIConversation.builder()
                .user(user)
                .title("Learning Path Mentorship")
                .build();
        return conversationRepository.save(conversation);
    }

    public MentorIntent classifyIntent(String query) {
        if (query == null || query.isBlank()) return MentorIntent.GREETING;
        String q = query.trim().toLowerCase();

        // Exact / short greetings
        if (q.matches("^(hey|hello|hi|hiya|yo|howdy|sup|good\\s*(morning|afternoon|evening|day)|hey\\s+there|hello\\s+there)[!.]?$")) {
            return MentorIntent.GREETING;
        }
        if (q.equals("how are you") || q.equals("how's it going") || q.equals("who are you") || q.equals("what can you do")) {
            return MentorIntent.CASUAL_CONVERSATION;
        }
        if (q.contains("why are you recommending") || q.contains("why is this recommended") || q.contains("why recommend")) {
            return MentorIntent.WHY_RECOMMENDATION;
        }
        if (q.contains("practice question") || q.contains("practice problem") || q.contains("give me practice") || q.contains("quiz me")) {
            return MentorIntent.PRACTICE_REQUEST;
        }
        if (q.contains("assess my") || q.contains("test my") || q.contains("diagnostic assessment") || q.contains("take an assessment")) {
            return MentorIntent.ASSESSMENT_REQUEST;
        }
        if (q.contains("teach me") || q.contains("i want to learn") || q.contains("how to learn") || q.contains("start learning")) {
            return MentorIntent.LEARNING_REQUEST;
        }
        if (q.startsWith("what is ") || q.startsWith("explain ") || q.startsWith("how does ") || q.contains("difference between")) {
            return MentorIntent.CONCEPT_EXPLANATION;
        }
        if (q.contains("what should i learn") || q.contains("next topic") || q.contains("what course") || q.contains("what to study")) {
            return MentorIntent.RECOMMENDATION_REQUEST;
        }
        if (q.contains("schedule") || q.contains("study plan") || q.contains("weekly plan") || q.contains("routine")) {
            return MentorIntent.STUDY_PLAN_REQUEST;
        }
        if (q.contains("weak") || q.contains("skill gap") || q.contains("what skills do i need") || q.contains("strongest skills")) {
            return MentorIntent.SKILL_QUESTION;
        }
        if (q.contains("progress") || q.contains("hours") || q.contains("streak") || q.contains("how am i doing") || q.contains("how am i progressing")) {
            return MentorIntent.PROGRESS_QUESTION;
        }
        if (q.contains("path") || q.contains("roadmap") || q.contains("curriculum") || q.contains("locked") || q.contains("prerequisite")) {
            return MentorIntent.LEARNING_PATH_QUESTION;
        }
        if (q.contains("career") || q.contains("job") || q.contains("interview") || q.contains("placement")) {
            return MentorIntent.CAREER_QUESTION;
        }
        return MentorIntent.GENERAL_QUESTION;
    }

    public String extractTopic(String query) {
        if (query == null || query.isBlank()) return null;
        String q = query.trim().toLowerCase();

        // Exact skill and domain keywords
        if (q.contains("oop") || q.contains("object oriented") || q.contains("object-oriented")) return "OOP";
        if (q.contains("polymorphism")) return "Polymorphism";
        if (q.contains("inheritance")) return "Inheritance";
        if (q.contains("encapsulation")) return "Encapsulation";
        if (q.contains("binary search")) return "Binary Search";
        if (q.contains("dynamic programming") || q.contains("dp")) return "Dynamic Programming";
        if (q.contains("recursion")) return "Recursion";
        if (q.contains("trees") || q.contains("binary tree")) return "Trees";
        if (q.contains("graphs") || q.contains("graph")) return "Graphs";
        if (q.contains("java") && !q.contains("javascript")) return "Java";
        if (q.contains("python")) return "Python";
        if (q.contains("javascript") || q.contains("js")) return "JavaScript";
        if (q.contains("typescript") || q.contains("ts")) return "TypeScript";
        if (q.contains("react")) return "React";
        if (q.contains("sql") || q.contains("database") || q.contains("postgres")) return "SQL";
        if (q.contains("system design")) return "System Design";
        if (q.contains("machine learning") || q.contains("ml")) return "Machine Learning";
        if (q.contains("dsa") || q.contains("data structures")) return "Data Structures & Algorithms";
        if (q.contains("spring") || q.contains("spring boot")) return "Spring Boot";

        return null;
    }

    public enum MentorIntent {
        GREETING,
        CASUAL_CONVERSATION,
        LEARNING_REQUEST,
        CONCEPT_EXPLANATION,
        PRACTICE_REQUEST,
        ASSESSMENT_REQUEST,
        STUDY_PLAN_REQUEST,
        RECOMMENDATION_REQUEST,
        LEARNING_PATH_QUESTION,
        WHY_RECOMMENDATION,
        PROGRESS_QUESTION,
        SKILL_QUESTION,
        CAREER_QUESTION,
        GENERAL_QUESTION
    }

    private String buildPrompt(LearnerAiContext context, String query, MentorIntent intent, String topic, String topRec, double confidence) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are LearnAI Mentor, an encouraging, world-class technical mentor and AI pair programmer. ");
        sb.append("Answer the learner's message directly, concisely, and helpfully in clean markdown.\n\n");
        sb.append("### CRITICAL CONTEXT PRIORITY & TRUTHFULNESS RULES:\n");
        sb.append("1. **Direct User Query Takes Top Priority**: Address the user's specific question or request directly.\n");
        sb.append("2. If the user asks about a specific topic (e.g., 'Java', 'OOP', 'Polymorphism'), focus 100% on that topic. Do NOT talk about unrelated topics like Binary Search.\n");
        sb.append("3. If the user is saying a greeting (like 'hey' or 'hello'), give a warm, concise 1-2 sentence greeting. Do NOT lecture or give unsolicited recommendations.\n");
        sb.append("4. NEVER invent assessment scores, test history, or streak days that are not explicitly present in the data below.\n");
        sb.append("5. Self-reported statements (e.g., 'I know Java') are intentions, NOT verified mastery. Only verified assessments establish mastery.\n\n");

        sb.append("Detected Query Intent: ").append(intent).append("\n");
        if (topic != null) {
            sb.append("Extracted Focus Topic: ").append(topic).append("\n");
        }
        sb.append("\n");

        if (intent != MentorIntent.GREETING && intent != MentorIntent.CASUAL_CONVERSATION && context != null) {
            sb.append("### Real Learner Facts (Database Truth):\n");
            sb.append("- Name: ").append(context.getFullName()).append("\n");
            sb.append("- Target Career: ").append(context.getTargetCareer()).append("\n");
            sb.append("- Experience Level: ").append(context.getExperienceLevel()).append("\n");
            sb.append("- BKT Overall Mastery: ").append(context.getOverallMasteryPercentage()).append("%\n");
            sb.append("- Active Streak: ").append(context.getActiveStreakDays()).append(" days\n");
            sb.append("- Career Readiness Score: ").append(context.getCareerReadinessScore()).append("%\n");

            if (context.getMasteredSkills() != null && !context.getMasteredSkills().isEmpty()) {
                sb.append("- Mastered Concepts (Verified): ").append(String.join(", ", context.getMasteredSkills())).append("\n");
            }
            if (context.getWeakSkills() != null && !context.getWeakSkills().isEmpty()) {
                sb.append("- Developing/Unassessed Concepts: ").append(String.join(", ", context.getWeakSkills())).append("\n");
            }
            if (context.getSkills() != null && !context.getSkills().isEmpty()) {
                String skills = context.getSkills().stream()
                        .map(s -> s.getSkillName() + " (" + s.getProficiencyLevel() + ", verified=" + s.isVerified() + ")")
                        .collect(Collectors.joining(", "));
                sb.append("- Skill Profile: ").append(skills).append("\n");
            } else {
                sb.append("- Skill Profile: Unassessed (No skills assessed yet)\n");
            }

            if (context.getRecentAssessments() == null || context.getRecentAssessments().isEmpty()) {
                sb.append("- Assessment History: No assessments completed yet.\n");
            }
            sb.append("\n");
        }

        if (intent == MentorIntent.RECOMMENDATION_REQUEST && topRec != null && !topRec.isBlank()) {
            sb.append("### Relevant ML Course Recommendation:\n");
            sb.append("- Next Recommended Module: ").append(topRec).append(" (Confidence: ").append(Math.round(confidence * 100)).append("%)\n\n");
        }

        sb.append("### Learner's Message:\n");
        sb.append(query).append("\n\n");
        sb.append("Provide your mentor response:");

        return sb.toString();
    }

    private List<AIMentorChatResponse.MentorActionDto> deriveActions(
            MentorIntent intent,
            String topic,
            String topRec,
            LearnerAiContext context
    ) {
        List<AIMentorChatResponse.MentorActionDto> actions = new ArrayList<>();

        if (intent == MentorIntent.GREETING || intent == MentorIntent.CASUAL_CONVERSATION) {
            actions.add(AIMentorChatResponse.MentorActionDto.builder()
                    .type("VIEW_LEARNING_PATH")
                    .label("View My Learning Path")
                    .targetUrl("/learning-path")
                    .build());
            actions.add(AIMentorChatResponse.MentorActionDto.builder()
                    .type("EXPLORE_COURSE")
                    .label("Explore Course Catalog")
                    .targetUrl("/explore-courses")
                    .build());
            return actions;
        }

        if (intent == MentorIntent.ASSESSMENT_REQUEST) {
            String skillLabel = topic != null ? topic : "Diagnostic";
            actions.add(AIMentorChatResponse.MentorActionDto.builder()
                    .type("START_ASSESSMENT")
                    .label("Start " + skillLabel + " Assessment")
                    .targetUrl("/assessments")
                    .build());
            return actions;
        }

        if (intent == MentorIntent.STUDY_PLAN_REQUEST) {
            actions.add(AIMentorChatResponse.MentorActionDto.builder()
                    .type("VIEW_WEEKLY_PLAN")
                    .label("View Weekly Schedule")
                    .targetUrl("/learning-path")
                    .build());
            return actions;
        }

        if (topic != null) {
            actions.add(AIMentorChatResponse.MentorActionDto.builder()
                    .type("EXPLORE_COURSE")
                    .label("Explore " + topic + " Courses")
                    .targetUrl("/explore-courses")
                    .build());
            actions.add(AIMentorChatResponse.MentorActionDto.builder()
                    .type("START_ASSESSMENT")
                    .label("Assess " + topic + " Skills")
                    .targetUrl("/assessments")
                    .build());
        }

        actions.add(AIMentorChatResponse.MentorActionDto.builder()
                .type("VIEW_LEARNING_PATH")
                .label("View Full Learning Path")
                .targetUrl("/learning-path")
                .build());

        if (context != null && context.getRevisionRequiredSkills() != null && !context.getRevisionRequiredSkills().isEmpty()) {
            actions.add(AIMentorChatResponse.MentorActionDto.builder()
                    .type("REVISE_TOPIC")
                    .label("Revise " + context.getRevisionRequiredSkills().get(0))
                    .targetUrl("/learning-path")
                    .build());
        }

        if (context != null && context.getWeakSkills() != null && !context.getWeakSkills().isEmpty()) {
            actions.add(AIMentorChatResponse.MentorActionDto.builder()
                    .type("START_ASSESSMENT")
                    .label("Take " + context.getWeakSkills().get(0) + " Assessment")
                    .targetUrl("/assessments")
                    .build());
        }

        if (topRec != null && !topRec.isBlank()) {
            actions.add(AIMentorChatResponse.MentorActionDto.builder()
                    .type("EXPLORE_COURSE")
                    .label("Start " + topRec)
                    .targetUrl("/explore-courses")
                    .build());
        }

        return actions;
    }

    private List<String> deriveFollowUps(MentorIntent intent, String topic) {
        if (topic != null) {
            return List.of(
                    "Show me a practical code example for " + topic,
                    "What are the best practices for " + topic + "?",
                    "Give me 3 practice problems on " + topic,
                    "How is " + topic + " tested in technical interviews?"
            );
        }
        return switch (intent) {
            case GREETING, CASUAL_CONVERSATION -> List.of(
                    "What should I learn today?",
                    "Assess my skills",
                    "Show my learning path",
                    "Give me a study plan"
            );
            case STUDY_PLAN_REQUEST -> List.of(
                    "Break this down into 30-minute milestones",
                    "What prerequisites do I need first?",
                    "Give me practice exercises for today"
            );
            case WHY_RECOMMENDATION -> List.of(
                    "Show alternative courses",
                    "What skills will this unlock?",
                    "How does this relate to my career target?"
            );
            default -> List.of(
                    "Show me a step-by-step example",
                    "Explain the time and space complexity",
                    "Give me 3 practice problems",
                    "Create a 45-minute study plan"
            );
        };
    }

    public String generateFallbackReply(
            String query,
            MentorIntent intent,
            String topic,
            LearnerAiContext context,
            String topRec
    ) {
        String targetCareer = context != null && context.getTargetCareer() != null ? context.getTargetCareer() : "Software Engineer";
        int readiness = context != null ? context.getCareerReadinessScore() : 0;

        if (intent == MentorIntent.GREETING) {
            return "Hey! I'm your LearnAI mentor. What would you like to learn or work on today?";
        }

        if (intent == MentorIntent.CASUAL_CONVERSATION) {
            return "I'm here and ready to help you level up your skills. Ask me any technical questions, request curriculum guidance, or let me know what you'd like to practice!";
        }

        if (intent == MentorIntent.LEARNING_REQUEST) {
            String topicName = topic != null ? topic : "programming fundamentals";
            if ("OOP".equalsIgnoreCase(topicName) || "Object-Oriented Programming (OOP)".equalsIgnoreCase(topicName) || query.toLowerCase().contains("oop")) {
                return "### Object-Oriented Programming (OOP) in Java\n\n" +
                        "OOP is structured around four foundational pillars:\n\n" +
                        "1. **Encapsulation:** Bundling data (fields) and methods into a single class while restricting direct access using `private` modifiers.\n" +
                        "2. **Inheritance:** Enabling a class (`subclass`) to inherit state and behavior from a `superclass` via `extends`.\n" +
                        "3. **Polymorphism:** Allowing objects to take many forms via method overriding (`@Override`) and method overloading.\n" +
                        "4. **Abstraction:** Hiding internal implementation details using `interfaces` and `abstract classes`.\n\n" +
                        "```java\n" +
                        "public interface PaymentService {\n" +
                        "    void processPayment(double amount);\n" +
                        "}\n" +
                        "```\n\n" +
                        "Would you like to explore polymorphic dispatch or encapsulation in depth?";
            }
            if ("Java".equalsIgnoreCase(topicName)) {
                return "Great choice! Java is an essential, high-performance language widely used for enterprise backends and distributed systems.\n\n" +
                        "**Recommended Java Roadmap:**\n" +
                        "1. **Core Fundamentals:** Variables, data types, control flow, and methods\n" +
                        "2. **Object-Oriented Programming:** Classes, encapsulation, inheritance, and polymorphism\n" +
                        "3. **Collections & Generics:** `List`, `Set`, `Map`, and algorithmic complexity\n\n" +
                        "Would you like an introduction to Java syntax or a hands-on coding problem?";
            }
            return String.format("Great goal! To master **%s**, we recommend starting with foundational principles before moving to practical implementations.\n\n" +
                    "Would you like an introductory concept overview or hands-on practice problems for %s?", topicName, topicName);
        }

        if (intent == MentorIntent.CONCEPT_EXPLANATION) {
            if ("OOP".equalsIgnoreCase(topic) || "Object-Oriented Programming (OOP)".equalsIgnoreCase(topic) || query.toLowerCase().contains("oop")) {
                return "### Object-Oriented Programming (OOP) in Java\n\n" +
                        "OOP is structured around four foundational pillars:\n\n" +
                        "1. **Encapsulation:** Bundling data (fields) and methods into a single class while restricting direct access using `private` modifiers.\n" +
                        "2. **Inheritance:** Enabling a class (`subclass`) to inherit state and behavior from a `superclass` via `extends`.\n" +
                        "3. **Polymorphism:** Allowing objects to take many forms via method overriding (`@Override`) and method overloading.\n" +
                        "4. **Abstraction:** Hiding internal implementation details using `interfaces` and `abstract classes`.\n\n" +
                        "```java\n" +
                        "public interface PaymentService {\n" +
                        "    void processPayment(double amount);\n" +
                        "}\n" +
                        "```\n\n" +
                        "Would you like to explore polymorphic dispatch or encapsulation in depth?";
            }
            if ("Polymorphism".equalsIgnoreCase(topic) || query.toLowerCase().contains("polymorphism")) {
                return "### Polymorphism in Java\n\n" +
                        "Polymorphism allows objects of different types to be treated through a unified interface:\n\n" +
                        "- **Compile-time (Static) Polymorphism:** Method overloading (same method name, different parameters).\n" +
                        "- **Runtime (Dynamic) Polymorphism:** Method overriding via dynamic method dispatch at runtime.\n\n" +
                        "Would you like a code example illustrating dynamic dispatch?";
            }
            String topicName = topic != null ? topic : "the requested concept";
            return String.format("### Understanding %s\n\n%s is a core software engineering concept. In production systems, mastering its trade-offs and runtime characteristics is key.", topicName, topicName);
        }

        if (intent == MentorIntent.PRACTICE_REQUEST) {
            String topicName = topic != null ? topic : "Data Structures & Algorithms";
            if ("Java".equalsIgnoreCase(topicName) || "OOP".equalsIgnoreCase(topicName)) {
                return "### Java Practice Questions:\n\n" +
                        "1. **Encapsulation & Validation:** Implement an immutable `BankAccount` class with thread-safe deposit/withdrawal validation.\n" +
                        "2. **Polymorphism in Action:** Design a `Shape` hierarchy (`Circle`, `Rectangle`) with overridden `calculateArea()` methods.\n" +
                        "3. **Collections & Streams:** Given a `List<Employee>`, write a Stream pipeline to filter active employees and group by department.\n\n" +
                        "Which problem would you like to solve first?";
            }
            return String.format("### Practice Questions for %s:\n\n1. Explain the fundamental trade-offs and edge cases.\n2. Implement the basic algorithm from scratch.\n3. Analyze time and space complexity ($O(n)$ bounds).\n\nReady to work on one together?", topicName);
        }

        if (intent == MentorIntent.ASSESSMENT_REQUEST) {
            String topicName = topic != null ? topic : "your technical";
            return String.format("You can assess your **%s** competencies through our Computerized Adaptive Testing (CAT) system.\n\n" +
                    "The assessment adapts dynamically to your skill level and updates your Bayesian Knowledge Tracing model upon completion. Click **Start Assessment** below to begin!", topicName);
        }

        if (intent == MentorIntent.WHY_RECOMMENDATION) {
            String recommended = topRec != null ? topRec : "this module";
            return String.format("We recommend **%s** because it directly targets a prerequisite competency in your **%s** career path.\n\n" +
                    "Mastering this unlocks downstream advanced topics and strengthens your verified readiness profile.", recommended, targetCareer);
        }

        if (intent == MentorIntent.STUDY_PLAN_REQUEST || intent == MentorIntent.RECOMMENDATION_REQUEST) {
            if (topRec != null) {
                return String.format("Based on your target career of **%s**, your best next focus is **%s**.\n\n" +
                        "**Recommended 45-minute Study Plan:**\n" +
                        "1. Core concept review & architectural model (15 min)\n" +
                        "2. Hands-on coding & edge case handling (20 min)\n" +
                        "3. Quiz / Self-assessment verification (10 min)", targetCareer, topRec);
            } else {
                return "To generate a tailored daily study plan, complete your diagnostic assessment to establish your baseline skill profile!";
            }
        }

        if (intent == MentorIntent.PROGRESS_QUESTION) {
            if (readiness > 0) {
                return String.format("You are progressing toward your **%s** goal with an estimated **%d%% career readiness** score based on your verified assessment history.", targetCareer, readiness);
            } else {
                return "You haven't completed any assessments or courses yet. Take your initial diagnostic assessment to begin tracking verified progress!";
            }
        }

        if (intent == MentorIntent.SKILL_QUESTION) {
            if (context != null && context.getWeakSkills() != null && !context.getWeakSkills().isEmpty()) {
                return "Based on your verified assessment history, your primary areas for improvement are: **" +
                        String.join(", ", context.getWeakSkills()) + "**.";
            } else {
                return "You have no assessed skill gaps recorded yet. Complete a diagnostic assessment to calibrate your verified skill profile!";
            }
        }

        if (intent == MentorIntent.LEARNING_PATH_QUESTION) {
            return "Advanced modules are unlocked dynamically as you demonstrate $\\ge 65\\%$ mastery on their required prerequisite concepts.";
        }

        return String.format("Great question! For your **%s** roadmap, focusing on core fundamentals and deliberate practice will yield the highest returns.", targetCareer);
    }
}

