package com.example.MockMate2.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("memory")

public class ConversationMemory {

    @Id
    private String id;
    private String userId;
    private String type; // "phone" or "coding"
    private List<String> history;

    public ConversationMemory(String userId, String type, List<String> history) {
        this.userId = userId;
        this.type = type;
        this.history = history;
    }

    public ConversationMemory(){
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getHistory() {
        return history;
    }

    public void setHistory(List<String> history) {
        this.history = history;
    }
}
