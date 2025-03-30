package com.example.MockMate2.service;

import com.example.MockMate2.models.ConversationMemory;
import com.example.MockMate2.repository.ConversationMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConversationService {

    @Autowired
    private ConversationMemoryRepository memoryRepository;

    public void saveInteraction(String userId, String type, String userPrompt, String geminiResponse) {
        ConversationMemory memory = memoryRepository
                .findByUserIdAndType(userId, type)
                .orElseGet(() -> new ConversationMemory(userId, type, new ArrayList<>()));

        List<String> history = memory.getHistory();
        history.add("User: " + userPrompt);
        history.add("Gemini: " + geminiResponse);

        memory.setHistory(history);
        memoryRepository.save(memory);
    }

    public List<String> getHistory(String userId, String type) {
        return memoryRepository
                .findByUserIdAndType(userId, type)
                .map(ConversationMemory::getHistory)
                .orElse(List.of());
    }
}
