package com.example.MockMate2.service;

import com.example.MockMate2.models.ConversationMemory;
import com.example.MockMate2.repository.ConversationMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * CodingInterviewService
 * 
 * This service manages AI-driven coding interview interactions.
 * It handles:
 * 1. Processing user inputs during coding interviews
 * 2. Generating AI responses using Spring AI and ChatClient
 * 3. Maintaining conversation context/history
 * 4. Persisting conversations in the database
 * 
 * The service uses Spring AI to communicate with the AI model (likely GPT or
 * similar)
 * and MongoDB to store conversation history.
 */
@Service
public class CodingInterviewService {

    private final ChatClient chatClient;
    private final ConversationMemoryRepository repo;

    /**
     * Constructor with dependency injection
     * 
     * @param builder ChatClient.Builder for creating the AI client
     * @param repo    Repository for storing conversation history
     */
    @Autowired
    public CodingInterviewService(ChatClient.Builder builder, ConversationMemoryRepository repo) {
        this.chatClient = builder.build();
        this.repo = repo;
    }

    /**
     * Processes user input and generates AI response for coding interviews
     * 
     * @param userId    The unique identifier for the user
     * @param userInput The text input from the user
     * @return AI-generated response to the user's input
     */
    public String chat(String userId, String userInput) {
        // Find existing conversation or create a new one
        var memory = repo.findByUserIdAndType(userId, "coding")
                .orElseGet(() -> {
                    var m = new ConversationMemory();
                    m.setUserId(userId);
                    m.setType("coding");
                    m.setHistory(new ArrayList<>(List.of(
                            // Initial system prompt that defines the AI's role and behavior
                            "You are a friendly and professional technical interviewer for a backend engineering position. "
                                    +
                                    "Start with simple algorithm or data structure questions, then move on to system design or in-depth technical topics. "
                                    +
                                    "Only ask one question at a time, and wait for the candidate's answer before continuing. "
                                    +
                                    "Do not provide explanations unless asked. " +
                                    "Speak naturally, like a human interviewer having a conversation. " +
                                    "Keep your tone encouraging and respectful throughout the session."

                )));
                    return m;
                });

        // Add user's input to conversation history
        memory.getHistory().add("Candidate: " + userInput);

        // Combine all conversation history into a single prompt
        String prompt = String.join("\n", memory.getHistory());

        // Get response from AI model
        String reply = chatClient.prompt(prompt).call().content();

        // Add AI's response to conversation history
        memory.getHistory().add("Interviewer: " + reply);
        repo.save(memory);

        return reply;
    }

    /**
     * Resets the coding interview session for a user
     * 
     * @param userId The unique identifier for the user
     */
    public void reset(String userId) {
        repo.findByUserIdAndType(userId, "coding").ifPresent(repo::delete);
    }
}
