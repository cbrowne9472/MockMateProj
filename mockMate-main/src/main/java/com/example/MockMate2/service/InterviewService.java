//package com.example.MockMate2.service;
//
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//
////NOT USED!!!!!!!!!
//@Service
//public class InterviewService {
//
//    private final ChatClient chatClient;
//    private final List<String> history = new ArrayList<>();
//
//    public InterviewService(ChatClient.Builder builder) {
//        this.chatClient = builder.build();
//        // Add an initial system message to set the tone
//        history.add("You are a technical interviewer for a software engineering position. Ask challenging, relevant questions one at a time.");
//    }
//
//    public String chat(String userInput) {
//        history.add("User: " + userInput);
//
//        StringBuilder promptBuilder = new StringBuilder();
//        for (String line : history) {
//            promptBuilder.append(line).append("\n");
//        }
//
//        String response = chatClient.prompt(promptBuilder.toString()).call().content();
//        history.add("Interviewer: " + response);
//        return response;
//    }
//
//    public void reset() {
//        history.clear();
//        history.add("You are a technical interviewer for a software engineering position. Ask challenging, relevant questions one at a time.");
//    }
//}
