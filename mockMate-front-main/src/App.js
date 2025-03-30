import React from "react";
import { Auth0Provider, useAuth0 } from "@auth0/auth0-react";
import CodingInterview from "./pages/CodingInterview";
import "./styles/global.css";

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
 */
function App() {
    // Destructure auth-related functions and state from Auth0
    const { loginWithRedirect, logout, isAuthenticated, user, isLoading } = useAuth0();

    // Show loading spinner while checking authentication state
    if (isLoading) {
        return (
            <div className="loading">
                <div className="loading-spinner" />
            </div>
        );
    }

    return (
        <div className="container">
            {/* Header with app title and user authentication controls */}
            <header className="header">
                <h1>MockMate</h1>
                {isAuthenticated ? (
                    // Show user info and logout button when authenticated
                    <div className="user-info">
                        <div className="user-avatar">
                            {user?.name?.[0] || user?.email?.[0] || 'U'}
                        </div>
                        <span>{user?.name || user?.email}</span>
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
                        Login with Auth0
                    </button>
                )}
            </header>

            {/* Main content - only show when authenticated */}
            {isAuthenticated && <CodingInterview userId={user.sub} />}
        </div>
    );
}

export default AppWrapper;

