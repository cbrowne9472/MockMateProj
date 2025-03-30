package com.example.MockMate2.repository;

import com.example.MockMate2.models.AppUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AppUserRepository extends MongoRepository<AppUser, String> {
    Optional<AppUser> findByAuth0Id(String auth0Id);
}