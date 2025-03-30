package com.example.MockMate2;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MockMate2Application {

	@Autowired
	private TranscriptionController transcriptionController;

	@Autowired
	private InterviewController interviewController;

	public static void main(String[] args) {
		SpringApplication.run(MockMate2Application.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(ChatClient.Builder builder) {
		return args -> {
//			ChatClient chatClient = builder.build();
//			String response = chatClient.prompt("Tell me a joke")
//					.call()
//					.content();
//
//			System.out.println(response);


		};
	}

}
