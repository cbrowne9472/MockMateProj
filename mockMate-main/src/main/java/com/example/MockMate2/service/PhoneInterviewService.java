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

    public String chat(String userId, String userInput, Integer questionCount, Boolean isComplete) {
        var memory = repo.findByUserIdAndType(userId, "phone")
                .orElseGet(() -> {
                    var m = new ConversationMemory();
                    m.setUserId(userId);
                    m.setType("phone");
                    m.setHistory(new ArrayList<>(List.of(
                            "System: You are conducting a technical interview. Your role is to:\n" +
                                    "1. Ask exactly ONE question, then STOP.\n" +
                                    "2. Wait for the candidate's response.\n" +
                                    "3. After receiving their response, ask your next question.\n" +
                                    "4. Never continue the conversation without user input.\n" +
                                    "5. Never simulate or include candidate responses.\n" +
                                    "6. Keep track of question count (max 5 questions).\n" +
                                    "7. Each response should be ONE turn only.\n\n" +
                                    "Format: Ask your question, then stop. Wait for input."
                    )));
                    return m;
                });

        // Handle the special START_INTERVIEW message
        if (userInput.equals("START_INTERVIEW")) {
            String prompt = String.join("\n", memory.getHistory()) +
                    "\n\nAsk your first question to start the interview. Keep it friendly and welcoming.";
            
            String reply = chatClient.prompt(prompt).call().content();
            reply = reply.replaceAll("(?i)^(interviewer:\\s*)+", "");
            
            memory.getHistory().add("Interviewer: " + reply);
            repo.save(memory);
            
            return reply;
        }

        // Get only the last few messages for context
        List<String> recentHistory = memory.getHistory();
        List<String> contextHistory = new ArrayList<>();
        
        // Always include the system prompt (first message)
        contextHistory.add(recentHistory.get(0));
        
        // Add recent messages if they exist
        if (recentHistory.size() > 1) {
            int startIndex = Math.max(1, recentHistory.size() - 4); // Get last 2 turns (4 messages), starting after system prompt
            contextHistory.addAll(recentHistory.subList(startIndex, recentHistory.size()));
        }
        
        // Add user's input to history
        memory.getHistory().add("Candidate: " + userInput);

        try {
            String prompt = String.join("\n", contextHistory) +
                    "\nCandidate: " + userInput + "\n\n" +
                    "Ask your next question based on the candidate's response. Remember: ONE question only, then stop.";
                    
            if (isComplete != null && isComplete) {
                prompt += "\nThis is the final question. After this, wrap up politely.";
            }

            String reply = chatClient.prompt(prompt).call().content();
            reply = reply.replaceAll("(?i)^(interviewer:\\s*)+", "");
            
            // Add the AI's response to history
            memory.getHistory().add("Interviewer: " + reply);
            repo.save(memory);

            return reply;
        } catch (Exception e) {
            e.printStackTrace();
            return "I apologize, but I encountered an error. Could you please try again or rephrase your response?";
        }
    }

    public String generateCritique(String userId) {
        var memory = repo.findByUserIdAndType(userId, "phone")
                .orElseThrow(() -> new IllegalStateException("No interview history found for user " + userId));

        String prompt = String.join("\n", memory.getHistory()) +
                "\n\nProvide a brief, focused critique of the interview. Keep it concise and actionable:" +
                "\n1. Overall Performance (1-2 sentences)" +
                "\n2. Key Strengths (bullet points, max 2)" +
                "\n3. Areas to Improve (bullet points, max 3)" +
                "\n4. One Specific Action Item" +
                "\nKeep the entire critique under 200 words. Use clear formatting with line breaks between sections." +
                "\nBe constructive but direct. Focus on the most important points only.";

        String critique = chatClient.prompt(prompt).call().content();
        return critique.replaceAll("(?i)^(interviewer:|system:|assistant:|critique:|response:)\\s*", "");
    }

    public void reset(String userId) {
        repo.findByUserIdAndType(userId, "phone").ifPresent(repo::delete);
    }
}
