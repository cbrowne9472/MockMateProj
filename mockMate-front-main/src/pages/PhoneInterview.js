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
    const [questionCount, setQuestionCount] = useState(0);
    const [isInterviewComplete, setIsInterviewComplete] = useState(false);
    const messagesEndRef = useRef(null);               // Reference for auto-scrolling

    const MAX_QUESTIONS = 5; // Maximum number of questions in the interview

    // Start the interview when component mounts
    useEffect(() => {
        const startInterview = async () => {
            try {
                setIsLoading(true);
                const response = await axios.post("/interview/phone-interview", {
                    userId,
                    text: "START_INTERVIEW",
                    questionCount: 0,
                    isComplete: false
                });
                setChat([{ sender: "Interviewer", text: response.data }]);
                setQuestionCount(0);
            } catch (error) {
                console.error("Error starting interview:", error);
                setChat([{ 
                    sender: "System", 
                    text: "Sorry, there was an error starting the interview. Please try resetting the conversation." 
                }]);
            } finally {
                setIsLoading(false);
            }
        };

        // Only start if chat is empty
        if (chat.length === 0) {
            startInterview();
        }
    }, []); // Empty dependency array means this runs once on mount

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
            // Clear the local chat history and restart the interview
            setChat([]);
            setQuestionCount(0);
            setIsInterviewComplete(false);
            
            // Start a new interview after reset
            const response = await axios.post("/interview/phone-interview", {
                userId,
                text: "START_INTERVIEW",
                questionCount: 0,
                isComplete: false
            });
            setChat([{ sender: "Interviewer", text: response.data }]);
        } catch (error) {
            console.error("Error resetting conversation:", error);
            setChat([{ 
                sender: "System", 
                text: "Sorry, there was an error resetting the interview. Please try again." 
            }]);
        } finally {
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
            const response = await axios.post("/interview/phone-interview", {
                userId,
                text: input,
                questionCount,
                isComplete: questionCount >= MAX_QUESTIONS - 1
            });

            // Update chat with user's message
            const updatedChat = [...chat, { sender: "You", text: input }];
            
            // If this was the last question, request final critique
            if (questionCount >= MAX_QUESTIONS - 1 && !isInterviewComplete) {
                updatedChat.push(
                    { sender: "Interviewer", text: response.data },
                    { sender: "System", text: "Interview complete. Generating final critique..." }
                );
                
                // Request final critique
                const critiqueResponse = await axios.post("/interview/phone-interview/critique", {
                    userId
                });
                
                updatedChat.push({ 
                    sender: "Interviewer", 
                    text: "Final Critique:\n" + critiqueResponse.data 
                });
                
                setIsInterviewComplete(true);
            } else {
                updatedChat.push({ sender: "Interviewer", text: response.data });
                setQuestionCount(prev => prev + 1);
            }
            
            setChat(updatedChat);
            setInput("");
        } catch (error) {
            console.error("Error sending message:", error);
            setChat([...chat, 
                { sender: "You", text: input }, 
                { sender: "System", text: "Sorry, there was an error processing your request." }
            ]);
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
        try {
            const stream = await navigator.mediaDevices.getUserMedia({
                audio: {
                    sampleRate: 48000,
                    channelCount: 1,
                    echoCancellation: true,
                    noiseSuppression: true
                }
            });
            
            const recorder = new MediaRecorder(stream, {
                mimeType: 'audio/webm;codecs=opus',
                audioBitsPerSecond: 48000
            });

            const chunks = [];
            recorder.ondataavailable = (e) => {
                if (e.data.size > 0) {
                    chunks.push(e.data);
                }
            };

            recorder.onstop = async () => {
                console.log("Recording stopped.");
                
                // Clean up the media stream first
                stream.getTracks().forEach(track => track.stop());
                
                const audioBlob = new Blob(chunks, { type: "audio/webm" });
                console.log("Audio blob size: ", audioBlob.size, "bytes");
                const formData = new FormData();
                formData.append("file", audioBlob, "audio.webm");

                try {
                    setIsLoading(true);
                    // Send audio to backend for transcription
                    const response = await axios.post("/transcribe", formData, {
                        headers: {
                            "Content-Type": "multipart/form-data",
                        },
                    });
                    const transcript = response.data;
                    console.log("Transcription received:", transcript);
                    
                    // Add the transcribed text to chat
                    setChat(current => [...current, { sender: "You", text: transcript }]);

                    // Send the transcribed text to the backend for AI response
                    const aiResponse = await axios.post("/interview/phone-interview", {
                        userId,
                        text: transcript,
                        questionCount,
                        isComplete: questionCount >= MAX_QUESTIONS - 1
                    });

                    // Handle the AI response based on interview state
                    if (questionCount >= MAX_QUESTIONS - 1 && !isInterviewComplete) {
                        // Add AI response and prepare for critique
                        setChat(current => [...current, 
                            { sender: "Interviewer", text: aiResponse.data },
                            { sender: "System", text: "Interview complete. Generating final critique..." }
                        ]);
                        
                        // Request final critique
                        const critiqueResponse = await axios.post("/interview/phone-interview/critique", {
                            userId
                        });
                        
                        setChat(current => [...current, { 
                            sender: "Interviewer", 
                            text: "Final Critique:\n" + critiqueResponse.data 
                        }]);
                        
                        setIsInterviewComplete(true);
                    } else {
                        // Add AI response and increment question counter
                        setChat(current => [...current, { 
                            sender: "Interviewer", 
                            text: aiResponse.data 
                        }]);
                        setQuestionCount(current => current + 1);
                    }
                } catch (error) {
                    console.error("Error processing audio:", error);
                    setChat(current => [...current, { 
                        sender: "System", 
                        text: "Sorry, there was an error processing your recording." 
                    }]);
                } finally {
                    setIsLoading(false);
                }
            };

            // Begin recording
            recorder.start();
            setMediaRecorder(recorder);  // Store the recorder in state
            setIsRecording(true);
        } catch (error) {
            console.error("Error starting recording:", error);
            alert("Could not access microphone. Please ensure you have granted permission.");
        }
    };

    /**
     * Stops the current audio recording
     */
    const stopRecording = () => {
        if (mediaRecorder && mediaRecorder.state === "recording") {
            mediaRecorder.stop();
            mediaRecorder.stream.getTracks().forEach(track => track.stop());
            setMediaRecorder(null);  // Reset the mediaRecorder state
            setIsRecording(false);
        }
    };

    return (
        <div className="chat-container">
            <div className="chat-header">
                <div className="header-content">
                    <h2>Phone Interview Practice</h2>
                    <div className="interview-progress">
                        Question {Math.min(questionCount + 1, MAX_QUESTIONS)} of {MAX_QUESTIONS}
                    </div>
                </div>
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
                        className={`message ${msg.sender === "You" ? "user" : msg.sender === "System" ? "system" : "assistant"}`}
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
                    onKeyPress={(e) => e.key === "Enter" && !e.shiftKey && sendMessage()}
                    placeholder={isInterviewComplete ? "Interview complete. Reset to start a new one." : "Type your answer... (Press Enter to send)"}
                    disabled={isLoading || isInterviewComplete}
                />
                <button
                    className="button"
                    onClick={sendMessage}
                    disabled={isLoading || !input.trim() || isInterviewComplete}
                >
                    Send
                </button>
                <button
                    className="button"
                    onClick={isRecording ? stopRecording : startRecording}
                    disabled={isLoading || isInterviewComplete}
                >
                    {isRecording ? "Stop Recording" : "Start Recording"}
                </button>
            </div>
        </div>
    );
}

export default PhoneInterview; 