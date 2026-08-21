package com.learningpath.ai.controller;

import com.learningpath.ai.dto.AIMentorChatRequest;
import com.learningpath.ai.dto.AIMentorChatResponse;
import com.learningpath.ai.service.AIMentorService;
import com.learningpath.entity.AIMessage;
import com.learningpath.entity.User;
import com.learningpath.repository.UserRepository;
import com.learningpath.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/mentor")
@RequiredArgsConstructor
@Slf4j
public class AIMentorController {

    private final AIMentorService aiMentorService;
    private final UserRepository userRepository;

    @PostMapping("/chat")
    public ResponseEntity<AIMentorChatResponse> chat(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody AIMentorChatRequest request
    ) {
        User user = resolveUser(principal);
        AIMentorChatResponse response = aiMentorService.processChat(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<AIMessage>> getHistory(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User user = resolveUser(principal);
        List<AIMessage> history = aiMentorService.getHistory(user);
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/history")
    public ResponseEntity<Map<String, String>> clearHistory(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User user = resolveUser(principal);
        aiMentorService.clearHistory(user);
        return ResponseEntity.ok(Map.of("message", "Conversation history cleared"));
    }

    private User resolveUser(UserPrincipal principal) {
        if (principal != null) {
            return userRepository.findById(principal.getId()).orElse(null);
        }
        return userRepository.findAll().stream().findFirst().orElse(null);
    }
}
