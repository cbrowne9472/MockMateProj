package com.example.MockMate2.repository;

import com.example.MockMate2.models.ConversationMemory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ConversationMemoryRepository extends MongoRepository<ConversationMemory, String> {
    Optional<ConversationMemory> findByUserIdAndType(String userId, String type);
}
