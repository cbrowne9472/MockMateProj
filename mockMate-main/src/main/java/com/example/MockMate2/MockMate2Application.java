package com.example.MockMate2;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;

@SpringBootApplication
public class MockMate2Application {

    private static final Logger logger = LoggerFactory.getLogger(MockMate2Application.class);

    @Value("${GOOGLE_APPLICATION_CREDENTIALS}")
    private String googleCredentialsPath;

    @Autowired
    private TranscriptionController transcriptionController;

    @Autowired
    private InterviewController interviewController;

    /**
     * Sets the Google Cloud credentials environment variable programmatically
     */
    @PostConstruct
    public void init() {
        if (googleCredentialsPath != null && !googleCredentialsPath.isEmpty()) {
            logger.info("Setting GOOGLE_APPLICATION_CREDENTIALS environment variable to: {}", googleCredentialsPath);

            // Set both the system property and environment variable
            System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", googleCredentialsPath);

            // Use reflection to modify the environment variable map
            try {
                Map<String, String> env = System.getenv();
                Field field = env.getClass().getDeclaredField("m");
                field.setAccessible(true);

                @SuppressWarnings("unchecked")
                Map<String, String> writableEnv = (Map<String, String>) field.get(env);
                writableEnv.put("GOOGLE_APPLICATION_CREDENTIALS", googleCredentialsPath);

                logger.info("Successfully set GOOGLE_APPLICATION_CREDENTIALS environment variable");
            } catch (Exception e) {
                logger.error("Failed to set environment variable directly, using credential path: {}",
                        googleCredentialsPath, e);
            }

            // Add detailed logging to help with troubleshooting
            logger.info("Google Cloud credential path: {}", googleCredentialsPath);
            File file = new File(googleCredentialsPath);
            logger.info("Credentials file exists: {}, readable: {}, size: {}",
                    file.exists(), file.canRead(), file.exists() ? file.length() : 0);
        } else {
            logger.warn("GOOGLE_APPLICATION_CREDENTIALS is not set in application.properties");
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(MockMate2Application.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(ChatClient.Builder builder) {
        return args -> {
            // ChatClient chatClient = builder.build();
            // String response = chatClient.prompt("Tell me a joke")
            // .call()
            // .content();
            //
            // System.out.println(response);

        };
    }

}
