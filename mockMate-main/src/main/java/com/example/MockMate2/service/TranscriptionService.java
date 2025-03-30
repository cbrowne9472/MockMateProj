package com.example.MockMate2.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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

    // Flag to enable/disable mock transcription (set to false to force Google Cloud
    // usage)
    @Value("${mock.transcription:false}")
    private boolean useMockTranscription;

    @Value("${GOOGLE_APPLICATION_CREDENTIALS}")
    private String credentialsPath;

    /**
     * Transcribes an audio file to text
     * 
     * @param audioFile The audio file uploaded by the user
     * @return The transcribed text from the audio
     * @throws IOException If the file cannot be processed
     */
    public String transcribe(MultipartFile audioFile) throws IOException {
        try {
            // Force use of Google Cloud transcription
            String result = googleCloudTranscription(audioFile);
            logger.info("Successfully transcribed audio using Google Cloud Speech-to-Text");
            return result;
        } catch (Exception e) {
            logger.error("Error during transcription with Google Cloud: {} - {}", e.getClass().getName(),
                    e.getMessage());
            if (e.getCause() != null) {
                logger.error("Caused by: {} - {}", e.getCause().getClass().getName(), e.getCause().getMessage());
            }

            // Log stack trace for detailed debugging
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.debug("Exception stack trace: {}", sw.toString());

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
        logger.info("Audio file size: {} bytes", audioFile.getSize());
        logger.info("Audio file content type: {}", audioFile.getContentType());

        // Explicitly load credentials from file
        GoogleCredentials credentials;
        try (FileInputStream credentialsStream = new FileInputStream(credentialsPath)) {
            logger.info("Loading Google credentials from: {}", credentialsPath);
            credentials = GoogleCredentials.fromStream(credentialsStream);
            logger.info("Successfully loaded Google credentials");
        }

        // Configure the speech client with explicit credentials
        SpeechSettings settings = SpeechSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();

        // Create client with explicit credentials
        try (SpeechClient speechClient = SpeechClient.create(settings)) {
            // Determine the encoding based on the content type
            RecognitionConfig.AudioEncoding encoding;
            if (audioFile.getContentType() != null && audioFile.getContentType().contains("webm")) {
                encoding = RecognitionConfig.AudioEncoding.WEBM_OPUS;
                logger.info("Using WEBM_OPUS encoding for webm audio");
            } else {
                encoding = RecognitionConfig.AudioEncoding.LINEAR16;
                logger.info("Using LINEAR16 encoding for non-webm audio");
            }

            // Configure the speech recognition request
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(encoding)
                    .setLanguageCode("en-US")
                    .setSampleRateHertz(48000)  // Explicitly set sample rate
                    .setAudioChannelCount(1)    // Mono audio
                    .setEnableAutomaticPunctuation(true)
                    .setModel("default")
                    .build();

            // Create the audio object from the uploaded file bytes
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(ByteString.copyFrom(audioFile.getBytes()))
                    .build();

            logger.info("Sending request to Google Cloud Speech-to-Text API with sample rate: 48000Hz");

            // Perform the transcription request
            RecognizeResponse response = speechClient.recognize(config, audio);

            // Check if results were returned
            if (response.getResultsList().isEmpty()) {
                logger.warn("No transcription results returned from Google Cloud");
                throw new IOException("No transcription results returned");
            }

            // Extract and join all transcribed text segments
            String transcription = response.getResultsList().stream()
                    .map(result -> result.getAlternativesList().get(0).getTranscript())
                    .collect(Collectors.joining(" "));

            logger.info("Successfully transcribed: {}", transcription);
            return transcription;
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