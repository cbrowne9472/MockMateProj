package com.example.MockMate2;

import com.example.MockMate2.service.TranscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * TranscriptionController
 * 
 * This controller handles HTTP requests related to audio transcription.
 * It provides a REST API endpoint that:
 * 1. Accepts audio file uploads from the frontend
 * 2. Passes them to the TranscriptionService for processing
 * 3. Returns the transcribed text or an error message
 * 
 * The endpoint is accessible without authentication as configured in
 * SecurityConfig.
 */
@RestController
public class TranscriptionController {

    @Autowired
    private TranscriptionService transcriptionService;

    /**
     * Handles audio file uploads and returns the transcribed text
     * 
     * @param file The audio file uploaded from the frontend
     * @return ResponseEntity containing either the transcribed text or an error
     *         message
     * 
     *         Example usage:
     *         POST /transcribe with a file in multipart/form-data format
     */
    @PostMapping("/transcribe")
    public ResponseEntity<String> handleAudioUpload(@RequestParam("file") MultipartFile file) {
        try {
            // Process the audio file through the transcription service
            String transcript = transcriptionService.transcribe(file);
            return ResponseEntity.ok(transcript);
        } catch (IOException e) {
            // If transcription fails, return a 500 error with the error message
            return ResponseEntity.status(500).body("Transcription failed: " + e.getMessage());
        }
    }
}
