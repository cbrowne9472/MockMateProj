package com.example.MockMate2.service;

import com.example.MockMate2.models.ConversationMemory;
import com.example.MockMate2.repository.ConversationMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PhoneInterviewService {

    @Autowired
    private ConversationMemoryRepository repo;

    private final ChatClient chatClient;

    public PhoneInterviewService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String chat(String userId, String userInput) {
        var memory = repo.findByUserIdAndType(userId, "phone")
                .orElseGet(() -> {
                    var m = new ConversationMemory();
                    m.setUserId(userId);
                    m.setType("phone");
                    m.setHistory(new ArrayList<>(List.of(
                            "You are a friendly and conversational technical phone interviewer. " +
                                    "Start with small talk and soft introductory questions. " +
                                    "Gradually transition into questions about the candidate's background, experience, and technical skills. " +
                                    "After each question, STOP and wait for the candidate's response before continuing. " +
                                    "Your tone should be natural and human-like, not robotic. " +
                                    "Do NOT ask multiple questions at once. Keep it relaxed and engaging."
                    )));
                    return m;
                });

        memory.getHistory().add("Candidate: " + userInput);

        String prompt = String.join("\n", memory.getHistory());
        String reply = chatClient.prompt(prompt).call().content();

        memory.getHistory().add("Interviewer: " + reply);
        repo.save(memory);

        return reply;
    }

    public void reset(String userId) {
        repo.findByUserIdAndType(userId, "phone").ifPresent(repo::delete);
    }
}
