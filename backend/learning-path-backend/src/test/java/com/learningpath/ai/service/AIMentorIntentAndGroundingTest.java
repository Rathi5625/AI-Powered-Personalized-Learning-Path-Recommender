package com.learningpath.ai.service;

import com.learningpath.ai.client.GeminiClient;
import com.learningpath.ai.dto.AIMentorChatRequest;
import com.learningpath.ai.dto.AIMentorChatResponse;
import com.learningpath.ai.dto.LearnerAiContext;
import com.learningpath.entity.AIConversation;
import com.learningpath.entity.AIMessage;
import com.learningpath.entity.Course;
import com.learningpath.entity.User;
import com.learningpath.recommendation.client.MlRecommendationClient;
import com.learningpath.recommendation.service.LearnerFeatureBuilderService;
import com.learningpath.repository.AIConversationRepository;
import com.learningpath.repository.AIMessageRepository;
import com.learningpath.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AIMentorIntentAndGroundingTest {

    @Mock
    private GeminiClient geminiClient;
    @Mock
    private LearnerContextService learnerContextService;
    @Mock
    private LearnerFeatureBuilderService featureBuilderService;
    @Mock
    private MlRecommendationClient mlRecommendationClient;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private AIConversationRepository conversationRepository;
    @Mock
    private AIMessageRepository messageRepository;

    @InjectMocks
    private AIMentorService aiMentorService;

    private User user;
    private AIConversation conversation;
    private LearnerAiContext learnerContext;

    @BeforeEach
    void setUp() {
        user = User.builder().fullName("Alex Learner").build();
        user.setId(UUID.randomUUID());

        conversation = AIConversation.builder().user(user).title("Learning Path Mentorship").build();
        conversation.setId(UUID.randomUUID());

        learnerContext = LearnerAiContext.builder()
                .userId(user.getId())
                .fullName("Alex Learner")
                .targetCareer("Software Engineer")
                .experienceLevel("BEGINNER")
                .overallMasteryPercentage(0.0)
                .activeStreakDays(0)
                .careerReadinessScore(0)
                .masteredSkills(List.of())
                .weakSkills(List.of())
                .build();
    }

    @Test
    void testScenario1_Greeting_ReturnsNaturalGreetingWithoutUnsolicitedRecommendations() {
        when(conversationRepository.findFirstByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(AIMessage.class))).thenAnswer(i -> {
            AIMessage msg = i.getArgument(0);
            msg.setId(UUID.randomUUID());
            return msg;
        });
        when(learnerContextService.buildContext(user)).thenReturn(learnerContext);
        when(geminiClient.generateContent(anyString())).thenReturn(null); // Triggers fallback

        AIMentorChatRequest request = new AIMentorChatRequest("hey", null);
        AIMentorChatResponse response = aiMentorService.processChat(user, request);

        assertNotNull(response);
        assertEquals("mentor", response.getRole());
        assertTrue(response.getReply().toLowerCase().contains("hey") || response.getReply().toLowerCase().contains("hello"));
        assertFalse(response.getReply().contains("Binary Search"));
        assertNull(response.getRecommendedAction());
        assertEquals(0, response.getRecommendedResources().size());
    }

    @Test
    void testScenario2_IWantToLearnJava_PrioritizesJavaOverUnrelatedRecommendations() {
        when(conversationRepository.findFirstByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(AIMessage.class))).thenAnswer(i -> {
            AIMessage msg = i.getArgument(0);
            msg.setId(UUID.randomUUID());
            return msg;
        });
        when(learnerContextService.buildContext(user)).thenReturn(learnerContext);

        Course javaCourse = Course.builder().title("Java Masterclass for Backend Engineers").build();
        javaCourse.setId(UUID.randomUUID());
        when(courseRepository.findByTitleContainingIgnoreCase("Java")).thenReturn(List.of(javaCourse));
        when(geminiClient.generateContent(anyString())).thenReturn(null);

        AIMentorChatRequest request = new AIMentorChatRequest("I want to learn Java", null);
        AIMentorChatResponse response = aiMentorService.processChat(user, request);

        assertNotNull(response);
        assertEquals("Java", response.getTopic());
        assertTrue(response.getReply().contains("Java"));
        assertFalse(response.getReply().contains("Binary Search"));
        assertTrue(response.getRecommendedResources().stream().anyMatch(r -> r.getTitle().contains("Java")));
    }

    @Test
    void testScenario3_TeachMeOOPInJava_ProvidesOOPExplanation() {
        when(conversationRepository.findFirstByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(AIMessage.class))).thenAnswer(i -> {
            AIMessage msg = i.getArgument(0);
            msg.setId(UUID.randomUUID());
            return msg;
        });
        when(learnerContextService.buildContext(user)).thenReturn(learnerContext);
        when(geminiClient.generateContent(anyString())).thenReturn(null);

        AIMentorChatRequest request = new AIMentorChatRequest("teach me OOP in Java", null);
        AIMentorChatResponse response = aiMentorService.processChat(user, request);

        assertNotNull(response);
        assertTrue(response.getReply().contains("Object-Oriented Programming") || response.getReply().contains("Encapsulation"));
        assertTrue(response.getReply().contains("Inheritance") || response.getReply().contains("Polymorphism"));
    }

    @Test
    void testScenario4_GiveMeJavaPracticeQuestions_ReturnsTargetedPracticeQuestions() {
        when(conversationRepository.findFirstByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(AIMessage.class))).thenAnswer(i -> {
            AIMessage msg = i.getArgument(0);
            msg.setId(UUID.randomUUID());
            return msg;
        });
        when(learnerContextService.buildContext(user)).thenReturn(learnerContext);
        when(geminiClient.generateContent(anyString())).thenReturn(null);

        AIMentorChatRequest request = new AIMentorChatRequest("give me Java practice questions", null);
        AIMentorChatResponse response = aiMentorService.processChat(user, request);

        assertNotNull(response);
        assertTrue(response.getReply().contains("Practice Questions"));
        assertTrue(response.getReply().contains("Java") || response.getReply().contains("Encapsulation"));
    }

    @Test
    void testScenario5_WhatShouldILearnToday_ReturnsStructuredStudyPlan() {
        when(conversationRepository.findFirstByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(AIMessage.class))).thenAnswer(i -> {
            AIMessage msg = i.getArgument(0);
            msg.setId(UUID.randomUUID());
            return msg;
        });
        when(learnerContextService.buildContext(user)).thenReturn(learnerContext);
        when(courseRepository.findAll()).thenReturn(List.of());
        when(geminiClient.generateContent(anyString())).thenReturn(null);

        AIMentorChatRequest request = new AIMentorChatRequest("what should I learn today?", null);
        AIMentorChatResponse response = aiMentorService.processChat(user, request);

        assertNotNull(response);
        assertTrue(response.getReply().contains("diagnostic assessment") || response.getReply().contains("Study Plan"));
    }

    @Test
    void testScenario6_WhyAreYouRecommendingBinarySearch_ExplainsRationaleTruthfully() {
        when(conversationRepository.findFirstByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(AIMessage.class))).thenAnswer(i -> {
            AIMessage msg = i.getArgument(0);
            msg.setId(UUID.randomUUID());
            return msg;
        });
        when(learnerContextService.buildContext(user)).thenReturn(learnerContext);
        when(geminiClient.generateContent(anyString())).thenReturn(null);

        AIMentorChatRequest request = new AIMentorChatRequest("why are you recommending Binary Search?", null);
        AIMentorChatResponse response = aiMentorService.processChat(user, request);

        assertNotNull(response);
        assertTrue(response.getReply().toLowerCase().contains("recommend") || response.getReply().contains("prerequisite"));
    }

    @Test
    void testScenario7_AssessMyJavaSkills_ReturnsAssessmentAction() {
        when(conversationRepository.findFirstByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(AIMessage.class))).thenAnswer(i -> {
            AIMessage msg = i.getArgument(0);
            msg.setId(UUID.randomUUID());
            return msg;
        });
        when(learnerContextService.buildContext(user)).thenReturn(learnerContext);
        when(geminiClient.generateContent(anyString())).thenReturn(null);

        AIMentorChatRequest request = new AIMentorChatRequest("assess my Java skills", null);
        AIMentorChatResponse response = aiMentorService.processChat(user, request);

        assertNotNull(response);
        assertTrue(response.getReply().contains("assess") || response.getReply().contains("assessment"));
        assertTrue(response.getActions().stream().anyMatch(a -> a.getType().equals("START_ASSESSMENT")));
    }
}
