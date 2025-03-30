package com.example.MockMate2.service;

import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * TranscriptionService
 * 
 * This service handles the conversion of audio files to text using Google
 * Cloud's Speech-to-Text API.
 * It supports:
 * 1. Transcription of audio files (primarily in WEBM format)
 * 2. Fallback to mock transcription when Google Cloud integration is
 * unavailable or fails
 * 3. Detailed logging of the transcription process
 * 
 * The service requires Google Cloud credentials to be set via the
 * GOOGLE_APPLICATION_CREDENTIALS
 * environment variable pointing to a valid service account key file.
 */
@Service
public class TranscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(TranscriptionService.class);

    // Flag to enable/disable mock transcription (can be set via
    // application.properties)
    @Value("${mock.transcription:false}")
    private boolean useMockTranscription;

    /**
     * Transcribes an audio file to text
     * 
     * @param audioFile The audio file uploaded by the user
     * @return The transcribed text from the audio
     * @throws IOException If the file cannot be processed
     */
    public String transcribe(MultipartFile audioFile) throws IOException {
        // If mock mode is enabled or if we encounter an error with Google Cloud, use
        // mock implementation
        try {
            if (useMockTranscription) {
                return mockTranscription(audioFile);
            }

            return googleCloudTranscription(audioFile);
        } catch (Exception e) {
            logger.error("Error during transcription with Google Cloud: ", e);
            // Fallback to mock implementation
            logger.info("Falling back to mock transcription");
            return mockTranscription(audioFile);
        }
    }

    /**
     * Transcribes audio using Google Cloud Speech-to-Text API
     * 
     * @param audioFile The audio file to transcribe
     * @return The transcribed text
     * @throws IOException If the file cannot be processed
     */
    private String googleCloudTranscription(MultipartFile audioFile) throws IOException {
        try (SpeechClient speechClient = SpeechClient.create()) {
            logger.info("Audio file size: {} bytes", audioFile.getSize());
            logger.info("Audio file content type: {}", audioFile.getContentType());

            // Configure the speech recognition request
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.WEBM_OPUS)
                    .setLanguageCode("en-US")
                    .build();

            // Create the audio object from the uploaded file bytes
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(ByteString.copyFrom(audioFile.getBytes()))
                    .build();

            // Perform the transcription request
            RecognizeResponse response = speechClient.recognize(config, audio);

            // Extract and join all transcribed text segments
            return response.getResultsList().stream()
                    .map(result -> result.getAlternativesList().get(0).getTranscript())
                    .collect(Collectors.joining(" "));
        }
    }

    /**
     * Provides a mock transcription when Google Cloud is unavailable
     * This is useful for development, testing, or when API credentials aren't
     * configured
     * 
     * @param audioFile The audio file (used only for size-based response selection)
     * @return A predefined mock transcription
     */
    private String mockTranscription(MultipartFile audioFile) {
        // Simple mock implementation that returns a predefined text
        // In a real application, this would be more sophisticated
        logger.info("Using mock transcription with file size: {} bytes", audioFile.getSize());

        // Just to make it a bit more realistic, generate different responses
        String[] mockResponses = {
                "This is a test response from the mock transcription service.",
                "Hello, I'm using the voice recording feature in MockMate.",
                "The audio recording works great, and this is a placeholder response.",
                "I'm speaking into the microphone and this text will appear in the chat.",
                "Testing the voice recording functionality with this mock response."
        };

        // Use a pseudo-random selection based on the file size
        int index = (int) (audioFile.getSize() % mockResponses.length);
        return mockResponses[index];
    }
}
