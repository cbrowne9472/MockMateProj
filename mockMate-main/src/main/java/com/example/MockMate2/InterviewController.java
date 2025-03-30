package com.example.MockMate2;

import com.example.MockMate2.service.CodingInterviewService;
import com.example.MockMate2.service.PhoneInterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * InterviewController
 * 
 * This controller manages the interview-related API endpoints.
 * It provides interfaces for:
 * 1. Conducting coding interviews with AI assistance
 * 2. Conducting phone interviews with AI assistance
 * 3. Resetting interview sessions
 * 
 * Each endpoint accepts a user ID and text input, processes it through the
 * appropriate
 * service, and returns AI-generated responses.
 */
@RestController
@RequestMapping("/interview")
public class InterviewController {

    @Autowired
    private PhoneInterviewService phoneInterviewService;

    @Autowired
    private CodingInterviewService codingInterviewService;

    /**
     * Handles phone interview interactions
     * 
     * @param request Map containing userId and text fields
     * @return AI-generated response to the user's input
     */
    @PostMapping("/phone-interview")
    public String phone(@RequestBody Map<String, String> request) {
        return phoneInterviewService.chat(request.get("userId"), request.get("text"));
    }

    /**
     * Resets a phone interview session
     * 
     * @param request Map containing userId field
     * @return Confirmation message
     */
    @PostMapping("/phone-interview/reset")
    public String resetPhone(@RequestBody Map<String, String> request) {
        phoneInterviewService.reset(request.get("userId"));
        return "Phone interview reset.";
    }

    /**
     * Handles coding interview interactions
     * 
     * @param request Map containing userId and text fields
     * @return AI-generated response to the user's input
     */
    @PostMapping("/coding-interview")
    public String coding(@RequestBody Map<String, String> request) {
        return codingInterviewService.chat(request.get("userId"), request.get("text"));
    }

    /**
     * Resets a coding interview session
     * 
     * @param request Map containing userId field
     * @return Confirmation message
     */
    @PostMapping("/coding-interview/reset")
    public String resetCoding(@RequestBody Map<String, String> request) {
        codingInterviewService.reset(request.get("userId"));
        return "Coding interview reset.";
    }
}
