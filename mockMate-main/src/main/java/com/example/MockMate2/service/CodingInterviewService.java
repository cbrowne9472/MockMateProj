package com.example.MockMate2.service;

import com.example.MockMate2.models.ConversationMemory;
import com.example.MockMate2.repository.ConversationMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
     * @param problem   The coding problem details
     * @return AI-generated response to the user's input
     */
    public String chat(String userId, String userInput, Map<String, Object> problem) {
        // Find existing conversation or create a new one
        var memory = repo.findByUserIdAndType(userId, "coding")
                .orElseGet(() -> {
                    var m = new ConversationMemory();
                    m.setUserId(userId);
                    m.setType("coding");
                    m.setHistory(new ArrayList<>(List.of(
                            // Initial system prompt that defines the AI's role and behavior
                            "You are a friendly and professional technical interviewer for a backend engineering position. " +
                            "You are helping the candidate with a coding problem. " +
                            "Provide guidance and hints without giving away the complete solution. " +
                            "Keep your tone encouraging and respectful throughout the session."
                    )));
                    return m;
                });

        // If this is a new problem (START_PROBLEM), reset the conversation and set up context
        if (userInput.equals("START_PROBLEM")) {
            memory.setHistory(new ArrayList<>(List.of(
                "System: You are a technical interviewer helping with the following coding problem:\n\n" +
                "Title: " + problem.get("title") + "\n\n" +
                "Problem Description:\n" + problem.get("description") + "\n\n" +
                "Starting Code:\n" + problem.get("startingCode") + "\n\n" +
                "Your role is to:\n" +
                "1. Help the candidate understand the problem\n" +
                "2. Provide hints and guidance when asked\n" +
                "3. Review their approach and suggest improvements\n" +
                "4. Do not give away complete solutions\n" +
                "5. Keep responses focused and relevant to the current problem\n\n" +
                "Start by introducing yourself and asking if they have any questions about the problem."
            )));
            repo.save(memory);
            return "Hello! I'll be helping you with the " + problem.get("title") + " problem. Take a look at the problem description and let me know if you have any questions. I'm here to provide guidance and hints as you work on the solution.";
        }

        // For normal interactions, include the current code state if available
        String currentCode = problem.containsKey("currentCode") ? (String) problem.get("currentCode") : null;
        
        // Add user's input to conversation history
        memory.getHistory().add("Candidate: " + userInput);
        
        // Create the prompt with current context
        String prompt = String.join("\n", memory.getHistory());
        if (currentCode != null) {
            prompt += "\n\nCurrent code state:\n" + currentCode + "\n\nProvide guidance based on their current code and question.";
        }

        // Get response from AI model
        String reply = chatClient.prompt(prompt).call().content();
        reply = reply.replaceAll("(?i)^(interviewer:\\s*)+", "");

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
