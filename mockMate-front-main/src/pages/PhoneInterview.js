import React, { useState, useRef, useEffect } from "react";
import axios from "../api/api";

/**
 * PhoneInterview Component
 * 
 * This component provides an interactive phone interview experience with:
 * 1. Text-based chat interface for communicating with an AI interviewer
 * 2. Voice recording capability that transcribes speech to text
 * 
 * @param {string} userId - The unique identifier for the current user
 */
function PhoneInterview({ userId }) {
    // State management for user interactions
    const [input, setInput] = useState("");            // Text input from the user
    const [chat, setChat] = useState([]);              // Chat history array
    const [isLoading, setIsLoading] = useState(false); // Loading state for API calls
    const [isRecording, setIsRecording] = useState(false); // Recording state
    const [mediaRecorder, setMediaRecorder] = useState(null); // MediaRecorder instance
    const messagesEndRef = useRef(null);               // Reference for auto-scrolling

    /**
     * Resets the conversation history with the chatbot
     */
    const resetConversation = async () => {
        try {
            setIsLoading(true);
            // Call the backend reset endpoint
            await axios.post("/interview/phone-interview/reset", {
                userId
            });
            // Clear the local chat history
            setChat([]);
            setIsLoading(false);
        } catch (error) {
            console.error("Error resetting conversation:", error);
            setIsLoading(false);
        }
    };

    /**
     * Auto-scrolls the chat to the latest message
     */
    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    // Scroll to bottom whenever chat updates
    useEffect(() => {
        scrollToBottom();
    }, [chat]);

    /**
     * Sends a text message to the backend for AI processing
     */
    const sendMessage = async () => {
        if (!input.trim()) return;

        setIsLoading(true);
        try {
            // Send message to backend AI service
            const response = await axios.post("/interview/phone-interview", {
                userId,
                text: input,
            });
            // Update chat with both user message and AI response
            setChat([...chat, { sender: "You", text: input }, { sender: "Interviewer", text: response.data }]);
            setInput("");
        } catch (error) {
            console.error("Error sending message:", error);
            setChat([...chat, { sender: "You", text: input }, { sender: "System", text: "Sorry, there was an error processing your request." }]);
        } finally {
            setIsLoading(false);
        }
    };

    /**
     * Handles Enter key press to send messages
     */
    const handleKeyPress = (e) => {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    };

    /**
     * Starts audio recording using the browser's MediaRecorder API
     */
    const startRecording = async () => {
        // Check browser compatibility
        if (!navigator.mediaDevices || !window.MediaRecorder) {
            alert("Your browser does not support audio recording.");
            return;
        }

        console.log("Starting recording...");
        // Request microphone access
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        const recorder = new MediaRecorder(stream);
        setMediaRecorder(recorder);

        // Collect audio data chunks
        const audioChunks = [];
        recorder.ondataavailable = (event) => {
            audioChunks.push(event.data);
            console.log("Audio chunk captured: ", event.data.size, "bytes");
        };

        // When recording stops, process the audio
        recorder.onstop = async () => {
            console.log("Recording stopped.");
            const audioBlob = new Blob(audioChunks, { type: "audio/webm" });
            console.log("Audio blob size: ", audioBlob.size, "bytes");
            const formData = new FormData();
            formData.append("file", audioBlob, "audio.webm");

            try {
                // Send audio to backend for transcription
                const response = await axios.post("/transcribe", formData, {
                    headers: {
                        "Content-Type": "multipart/form-data",
                    },
                });
                const transcript = response.data;
                console.log("Transcription received:", transcript);
                
                // Add the transcribed text to chat and input
                setChat(prev => [...prev, { sender: "You", text: transcript }]);
                
                // Send the transcribed text to the backend
                try {
                    setIsLoading(true);
                    const aiResponse = await axios.post("/interview/phone-interview", {
                        userId,
                        text: transcript,
                    });
                    setChat(prev => [...prev, { sender: "Interviewer", text: aiResponse.data }]);
                } catch (aiError) {
                    console.error("Error getting AI response:", aiError);
                    setChat(prev => [...prev, { sender: "System", text: "Sorry, there was an error processing your request." }]);
                } finally {
                    setIsLoading(false);
                }
            } catch (error) {
                console.error("Error transcribing audio:", error);
                setChat([...chat, { sender: "System", text: "Sorry, there was an error processing your audio." }]);
            }
        };

        // Begin recording
        recorder.start();
        setIsRecording(true);
    };

    /**
     * Stops the current audio recording
     */
    const stopRecording = () => {
        if (mediaRecorder) {
            mediaRecorder.stop();
            setIsRecording(false);
        }
    };

    return (
        <div className="chat-container">
            <div className="chat-header">
                <h2>Phone Interview Practice</h2>
                <button
                    className="button secondary"
                    onClick={resetConversation}
                    disabled={isLoading || chat.length === 0}
                >
                    Reset Conversation
                </button>
            </div>
            
            {/* Chat messages display area */}
            <div className="chat-messages">
                {chat.map((msg, i) => (
                    <div
                        key={i}
                        className={`message ${msg.sender === "You" ? "user" : "assistant"}`}
                    >
                        <strong>{msg.sender}:</strong> {msg.text}
                    </div>
                ))}
                {/* Loading indicator while waiting for AI response */}
                {isLoading && (
                    <div className="message assistant">
                        <div className="loading-spinner" style={{ width: "1rem", height: "1rem", marginRight: "0.5rem" }} />
                        Thinking...
                    </div>
                )}
                <div ref={messagesEndRef} />
            </div>
            {/* User input and controls */}
            <div className="input-container">
                <input
                    className="input"
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    onKeyPress={handleKeyPress}
                    placeholder="Type your answer... (Press Enter to send)"
                    disabled={isLoading}
                />
                <button
                    className="button"
                    onClick={sendMessage}
                    disabled={isLoading || !input.trim()}
                >
                    Send
                </button>
                <button
                    className="button"
                    onClick={isRecording ? stopRecording : startRecording}
                    disabled={isLoading}
                >
                    {isRecording ? "Stop Recording" : "Start Recording"}
                </button>
            </div>
        </div>
    );
}

export default PhoneInterview; 