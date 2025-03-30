import React, { useState, useRef, useEffect } from "react";
import axios from "../api/api";

/**
 * CodingInterview Component
 * 
 * This component provides an interactive coding interview experience with:
 * 1. An interview prompt/problem description
 * 2. A code editor for writing solutions
 * 3. Real-time feedback and testing
 * 
 * @param {string} userId - The unique identifier for the current user
 */
function CodingInterview({ userId }) {
    // State management for coding interview
    const [codeInput, setCodeInput] = useState("// Write your code here\n\n");
    const [language, setLanguage] = useState("javascript");
    const [output, setOutput] = useState("");
    const [isRunning, setIsRunning] = useState(false);
    
    // Array of coding problems
    const codingProblems = [
        {
            id: 1,
            title: "Two Sum Problem",
            description: `
Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.

You may assume that each input would have exactly one solution, and you may not use the same element twice.

Example:
Input: nums = [2,7,11,15], target = 9
Output: [0,1]
Explanation: Because nums[0] + nums[1] == 9, we return [0, 1].
            `,
            startingCode: `function twoSum(nums, target) {
    // Your code here
    
}

// Example test case
const nums = [2, 7, 11, 15];
const target = 9;
console.log(twoSum(nums, target)); // Should output [0, 1]`,
            tests: [
                { input: { nums: [2, 7, 11, 15], target: 9 }, expectedOutput: [0, 1] },
                { input: { nums: [3, 2, 4], target: 6 }, expectedOutput: [1, 2] }
            ]
        },
        {
            id: 2,
            title: "Valid Palindrome",
            description: `
A phrase is a palindrome if, after converting all uppercase letters into lowercase letters and removing all non-alphanumeric characters, it reads the same forward and backward.

Given a string s, return true if it is a palindrome, or false otherwise.

Example:
Input: s = "A man, a plan, a canal: Panama"
Output: true
Explanation: "amanaplanacanalpanama" is a palindrome.
            `,
            startingCode: `function isPalindrome(s) {
    // Your code here
    
}

// Example test case
const str = "A man, a plan, a canal: Panama";
console.log(isPalindrome(str)); // Should output true`,
            tests: [
                { input: "A man, a plan, a canal: Panama", expectedOutput: true },
                { input: "race a car", expectedOutput: false }
            ]
        },
        {
            id: 3,
            title: "Reverse Linked List",
            description: `
Given the head of a singly linked list, reverse the list, and return the reversed list.

Example:
Input: head = [1,2,3,4,5]
Output: [5,4,3,2,1]

Note: In this environment, we'll represent the linked list using arrays for simplicity.
            `,
            startingCode: `// Definition for singly-linked list node
class ListNode {
    constructor(val, next = null) {
        this.val = val;
        this.next = next;
    }
}

// Helper function to convert array to linked list
function arrayToList(arr) {
    if (!arr.length) return null;
    let head = new ListNode(arr[0]);
    let current = head;
    for (let i = 1; i < arr.length; i++) {
        current.next = new ListNode(arr[i]);
        current = current.next;
    }
    return head;
}

// Helper function to convert linked list to array
function listToArray(head) {
    const result = [];
    let current = head;
    while (current) {
        result.push(current.val);
        current = current.next;
    }
    return result;
}

function reverseList(head) {
    // Your code here
    
}

// Example test case
const head = arrayToList([1, 2, 3, 4, 5]);
const reversed = reverseList(head);
console.log(listToArray(reversed)); // Should output [5, 4, 3, 2, 1]`,
            tests: [
                { input: [1, 2, 3, 4, 5], expectedOutput: [5, 4, 3, 2, 1] },
                { input: [1, 2], expectedOutput: [2, 1] }
            ]
        },
        {
            id: 4,
            title: "Maximum Subarray",
            description: `
Given an integer array nums, find the contiguous subarray (containing at least one number) which has the largest sum and return its sum.

A subarray is a contiguous part of an array.

Example:
Input: nums = [-2,1,-3,4,-1,2,1,-5,4]
Output: 6
Explanation: [4,-1,2,1] has the largest sum = 6.
            `,
            startingCode: `function maxSubArray(nums) {
    // Your code here
    
}

// Example test case
const nums = [-2, 1, -3, 4, -1, 2, 1, -5, 4];
console.log(maxSubArray(nums)); // Should output 6`,
            tests: [
                { input: [-2, 1, -3, 4, -1, 2, 1, -5, 4], expectedOutput: 6 },
                { input: [1], expectedOutput: 1 },
                { input: [5, 4, -1, 7, 8], expectedOutput: 23 }
            ]
        },
        {
            id: 5,
            title: "Valid Parentheses",
            description: `
Given a string s containing just the characters '(', ')', '{', '}', '[' and ']', determine if the input string is valid.

An input string is valid if:
1. Open brackets must be closed by the same type of brackets.
2. Open brackets must be closed in the correct order.
3. Every close bracket has a corresponding open bracket of the same type.

Example:
Input: s = "()[]{}"
Output: true
            `,
            startingCode: `function isValid(s) {
    // Your code here
    
}

// Example test cases
console.log(isValid("()")); // Should output true
console.log(isValid("()[]{}")); // Should output true
console.log(isValid("(]")); // Should output false`,
            tests: [
                { input: "()", expectedOutput: true },
                { input: "()[]{}", expectedOutput: true },
                { input: "(]", expectedOutput: false },
                { input: "([)]", expectedOutput: false }
            ]
        }
    ];
    
    // Current problem state
    const [currentProblemIndex, setCurrentProblemIndex] = useState(0);
    const currentProblem = codingProblems[currentProblemIndex];
    
    // Chat state for hints and discussion
    const [chat, setChat] = useState([]);
    const [input, setInput] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const messagesEndRef = useRef(null);
    
    // Set initial code when problem changes
    useEffect(() => {
        setCodeInput(currentProblem.startingCode);
        setOutput("");
        setChat([]);
    }, [currentProblemIndex, currentProblem.startingCode]);

    // Auto-scroll chat to bottom
    useEffect(() => {
        scrollToBottom();
    }, [chat]);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };
    
    /**
     * Runs the code written by the user
     */
    const runCode = async () => {
        setIsRunning(true);
        setOutput("Running code...");
        
        try {
            // In a real implementation, this would send the code to a backend service
            // For now, we'll use a mock implementation that evaluates JavaScript code
            
            // Safety precaution - in production this should be executed in a sandbox
            setTimeout(() => {
                try {
                    // Use a safer approach in production!
                    const result = evalWithLogs(codeInput);
                    setOutput(result);
                } catch (error) {
                    setOutput(`Error: ${error.message}`);
                }
                setIsRunning(false);
            }, 1000);
            
        } catch (error) {
            setOutput(`Error: ${error.message}`);
            setIsRunning(false);
        }
    };
    
    /**
     * A simple function to capture console.log output when evaluating code
     * Note: This is for demonstration purposes only and is not secure for production
     */
    const evalWithLogs = (code) => {
        let logs = [];
        const originalConsoleLog = console.log;
        
        // Override console.log to capture output
        console.log = (...args) => {
            logs.push(args.map(arg => 
                typeof arg === 'object' ? JSON.stringify(arg) : arg
            ).join(' '));
        };
        
        try {
            // eslint-disable-next-line no-eval
            eval(code);
            console.log = originalConsoleLog;
            return logs.join('\n');
        } catch (error) {
            console.log = originalConsoleLog;
            throw error;
        }
    };
    
    /**
     * Resets the current coding problem
     */
    const resetCode = () => {
        setCodeInput(currentProblem.startingCode);
        setOutput("");
    };
    
    /**
     * Navigate to a specific problem by index
     */
    const navigateToProblem = (index) => {
        setCurrentProblemIndex(index);
    };
    
    /**
     * Navigate to the next problem
     */
    const nextProblem = () => {
        const nextIndex = (currentProblemIndex + 1) % codingProblems.length;
        setCurrentProblemIndex(nextIndex);
    };
    
    /**
     * Navigate to the previous problem
     */
    const prevProblem = () => {
        const prevIndex = (currentProblemIndex - 1 + codingProblems.length) % codingProblems.length;
        setCurrentProblemIndex(prevIndex);
    };
    
    /**
     * Sends a message to the AI interviewer for hints or questions
     */
    const sendMessage = async () => {
        if (!input.trim()) return;

        setIsLoading(true);
        try {
            // Send message to backend AI service
            const response = await axios.post("/interview/coding-interview", {
                userId,
                text: input,
                problem: currentProblem.title
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
     * Handles keyboard shortcuts for the code editor
     */
    const handleKeyDown = (e) => {
        if (e.key === "Tab") {
            e.preventDefault();
            const { selectionStart, selectionEnd } = e.target;
            const newValue = codeInput.substring(0, selectionStart) + "    " + codeInput.substring(selectionEnd);
            setCodeInput(newValue);
            
            // Set cursor position after the inserted tab
            setTimeout(() => {
                e.target.selectionStart = e.target.selectionEnd = selectionStart + 4;
            }, 0);
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

    return (
        <div className="coding-interview-container">
            {/* Problem navigation bar */}
            <div className="problem-navigation-bar">
                <div className="problem-navigation-controls">
                    <button 
                        className="button secondary" 
                        onClick={prevProblem}
                    >
                        ← Prev
                    </button>
                    
                    <div className="problem-tabs">
                        {codingProblems.map((problem, index) => (
                            <button 
                                key={problem.id}
                                className={`problem-tab ${index === currentProblemIndex ? 'active' : ''}`}
                                onClick={() => navigateToProblem(index)}
                            >
                                {index + 1}
                            </button>
                        ))}
                    </div>
                    
                    <button 
                        className="button secondary" 
                        onClick={nextProblem}
                    >
                        Next →
                    </button>
                </div>
                
                <div className="problem-count">
                    Problem {currentProblemIndex + 1} of {codingProblems.length}: {currentProblem.title}
                </div>
            </div>
            
            {/* Problem statement section */}
            <div className="problem-section">
                <div className="problem-description">
                    <pre>{currentProblem.description}</pre>
                </div>
            </div>
            
            <div className="coding-workspace">
                {/* Code editor section */}
                <div className="code-editor-section">
                    <div className="editor-header">
                        <h3>Code Editor</h3>
                        <div className="editor-controls">
                            <select 
                                value={language} 
                                onChange={(e) => setLanguage(e.target.value)}
                                className="language-selector"
                            >
                                <option value="javascript">JavaScript</option>
                                <option value="python" disabled>Python (Coming Soon)</option>
                                <option value="java" disabled>Java (Coming Soon)</option>
                            </select>
                            <button 
                                className="button run-button" 
                                onClick={runCode}
                                disabled={isRunning}
                            >
                                {isRunning ? "Running..." : "Run Code"}
                            </button>
                            <button 
                                className="button secondary" 
                                onClick={resetCode}
                            >
                                Reset Code
                            </button>
                        </div>
                    </div>
                    
                    <textarea
                        className="code-editor"
                        value={codeInput}
                        onChange={(e) => setCodeInput(e.target.value)}
                        onKeyDown={handleKeyDown}
                        spellCheck="false"
                        wrap="off"
                    />
                    
                    <div className="output-section">
                        <h3>Output</h3>
                        <pre className="output-display">{output}</pre>
                    </div>
                </div>
                
                {/* Chat section for hints and assistance */}
                <div className="chat-section">
                    <h3>Discussion with Interviewer</h3>
                    
                    <div className="chat-messages">
                        {chat.length === 0 && (
                            <div className="empty-chat-message">
                                Ask questions or request hints about the problem.
                            </div>
                        )}
                        
                        {chat.map((msg, i) => (
                            <div
                                key={i}
                                className={`message ${msg.sender === "You" ? "user" : "assistant"}`}
                            >
                                <strong>{msg.sender}:</strong> {msg.text}
                            </div>
                        ))}
                        
                        {isLoading && (
                            <div className="message assistant">
                                <div className="loading-spinner" style={{ width: "1rem", height: "1rem", marginRight: "0.5rem" }} />
                                Thinking...
                            </div>
                        )}
                        
                        <div ref={messagesEndRef} />
                    </div>
                    
                    <div className="input-container">
                        <input
                            className="input"
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            onKeyPress={handleKeyPress}
                            placeholder="Ask a question or request a hint..."
                            disabled={isLoading}
                        />
                        <button
                            className="button"
                            onClick={sendMessage}
                            disabled={isLoading || !input.trim()}
                        >
                            Send
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default CodingInterview;
