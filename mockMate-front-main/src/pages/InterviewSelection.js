import React from "react";

/**
 * InterviewSelection Component
 * 
 * This component displays a selection screen for users to choose
 * between different interview types (Phone or Coding).
 * 
 * @param {string} userId - The unique identifier for the current user
 * @param {function} onSelect - Callback function when an interview type is selected
 */
function InterviewSelection({ userId, onSelect }) {
    return (
        <div className="selection-container">
            <h2>Welcome to MockMate</h2>
            <p>
                Your personal interview coach that helps you prepare for job interviews.
                Select an interview type below to start practicing and improve your skills.
            </p>
            
            <div className="interview-options">
                <div 
                    className="interview-option"
                    onClick={() => onSelect("phone")}
                >
                    <div className="icon-container icon-phone">
                        <div className="screen"></div>
                        <div className="button"></div>
                        <div className="speaker"></div>
                    </div>
                    <h3>Phone Interview</h3>
                    <p>
                        Practice a realistic phone screening interview with general questions, 
                        behavioral scenarios, and receive real-time feedback. Perfect for 
                        improving your communication skills.
                    </p>
                    <button className="button">Start Now</button>
                </div>
                
                <div 
                    className="interview-option"
                    onClick={() => onSelect("coding")}
                >
                    <div className="icon-container icon-coding">
                        <div className="dots">
                            <span className="dot-middle"></span>
                        </div>
                        <div className="syntax">&lt;/&gt;</div>
                        <div className="brackets">{"{}"}</div>
                    </div>
                    <h3>Coding Interview</h3>
                    <p>
                        Practice technical coding interviews with algorithm challenges, 
                        problem-solving questions, and code reviews. Sharpen your technical 
                        skills for software engineering roles.
                    </p>
                    <button className="button">Start Now</button>
                </div>
            </div>
        </div>
    );
}

export default InterviewSelection; 