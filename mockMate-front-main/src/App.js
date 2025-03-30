import React, { useState, useEffect } from "react";
import { Auth0Provider, useAuth0 } from "@auth0/auth0-react";
import CodingInterview from "./pages/CodingInterview";
import PhoneInterview from "./pages/PhoneInterview";
import InterviewSelection from "./pages/InterviewSelection";
import "./styles/global.css";
import axios from "axios";

/**
 * AppWrapper Component
 * 
 * This component serves as the entry point of the application.
 * It wraps the main App component with Auth0Provider to enable authentication.
 * 
 * Auth0 is configured with:
 * - domain: The Auth0 domain for the application
 * - clientId: The Auth0 client ID
 * - redirectUri: Where to redirect after authentication
 */
function AppWrapper() {
    return (
        <Auth0Provider
            domain="dev-x36qv2ykoznt8o45.us.auth0.com"
            clientId="OyZehXTUEVfmnXh3TSXHJyxp0L8PXl9E"
            authorizationParams={{
                redirect_uri: window.location.origin,
            }}
        >
            <App />
        </Auth0Provider>
    );
}

/**
 * App Component
 * 
 * The main application component that handles:
 * 1. Authentication state management through Auth0
 * 2. Conditional rendering based on authentication state
 * 3. User profile display and logout functionality
 * 4. Loading state visualization
 * 5. Interview type selection and rendering
 */
function App() {
    // Destructure auth-related functions and state from Auth0
    const { loginWithRedirect, logout, isAuthenticated, user, isLoading } = useAuth0();
    
    // State to track the selected interview type
    const [interviewType, setInterviewType] = useState(null);

    // Set up browser history navigation
    useEffect(() => {
        // Handle browser back button
        const handlePopState = (event) => {
            // Return to selection screen when back button is pressed
            if (interviewType) {
                setInterviewType(null);
            }
        };

        window.addEventListener('popstate', handlePopState);
        
        return () => {
            window.removeEventListener('popstate', handlePopState);
        };
    }, [interviewType]);

    // Handle interview type selection
    const handleInterviewSelect = async (type) => {
        try {
            // Reset the previous interview state if switching types
            if (interviewType && interviewType !== type) {
                await axios.post(`/interview/${interviewType}-interview/reset`, {
                    userId: user.sub
                });
            }
            
            // Push a new entry to browser history when selecting an interview type
            window.history.pushState({ page: type }, '', `/${type}`);
            setInterviewType(type);
        } catch (error) {
            console.error("Error resetting interview state:", error);
        }
    };

    // Show loading spinner while checking authentication state
    if (isLoading) {
        return (
            <div className="loading">
                <div className="loading-spinner" />
                <p>Loading MockMate...</p>
            </div>
        );
    }

    // Render appropriate interview component based on selection
    const renderInterviewComponent = () => {
        if (!interviewType) {
            return <InterviewSelection userId={user.sub} onSelect={handleInterviewSelect} />;
        }

        switch (interviewType) {
            case "phone":
                return <PhoneInterview userId={user.sub} />;
            case "coding":
                return <CodingInterview userId={user.sub} />;
            default:
                return <InterviewSelection userId={user.sub} onSelect={handleInterviewSelect} />;
        }
    };

    return (
        <div className="container">
            {/* Header with app title and user authentication controls */}
            <header className="header">
                <div className="header-left">
                    {isAuthenticated && interviewType && (
                        <span 
                            className="back-navigation" 
                            onClick={() => {
                                window.history.back();
                            }}
                        >
                            ← Back
                        </span>
                    )}
                    <h1>MockMate</h1>
                </div>
                {isAuthenticated ? (
                    // Show user info and logout button when authenticated
                    <div className="user-info">
                        <div className="user-avatar">
                            {user?.name?.[0] || user?.email?.[0] || 'U'}
                        </div>
                        <span>{user?.name || user?.email}</span>
                        {interviewType && (
                            <button
                                className="button secondary"
                                onClick={() => {
                                    window.history.back();
                                    setInterviewType(null);
                                }}
                            >
                                Change Interview
                            </button>
                        )}
                        <button
                            className="button secondary"
                            onClick={() =>
                                logout({ logoutParams: { returnTo: window.location.origin } })
                            }
                        >
                            Logout
                        </button>
                    </div>
                ) : (
                    // Show login button when not authenticated
                    <button className="button" onClick={() => loginWithRedirect()}>
                        Sign In to Start
                    </button>
                )}
            </header>

            {/* Main content - only show when authenticated */}
            {isAuthenticated ? (
                renderInterviewComponent()
            ) : (
                <div className="selection-container">
                    <h2>Prepare for Your Next Interview</h2>
                    <p>
                        MockMate is your AI-powered interview coach that helps you practice and
                        improve your interview skills. Sign in to get started with realistic
                        interview simulations and feedback.
                    </p>
                    <button className="button" onClick={() => loginWithRedirect()}>
                        Sign In to Begin Your Practice
                    </button>
                </div>
            )}
        </div>
    );
}

export default AppWrapper;

