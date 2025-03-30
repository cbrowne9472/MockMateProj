import axios from 'axios';

/**
 * API Configuration
 * 
 * This file sets up the Axios instance for making HTTP requests to the backend.
 * It configures:
 * - Base URL for all API requests (pointing to the Spring Boot backend)
 * - Default headers for content type
 * - Any other global axios configuration needed
 * 
 * Usage example:
 * import api from '../api/api';
 * const response = await api.post('/endpoint', data);
 */

// Create an axios instance with custom configuration
const api = axios.create({
    // Base URL for all API requests - should match the backend server address
    baseURL: 'http://localhost:8080',
    
    // Default headers sent with every request
    headers: {
        'Content-Type': 'application/json',
    },
    
    // You can add more axios config options here as needed:
    // - timeout
    // - withCredentials
    // - responseType
    // - etc.
});

// Export the configured axios instance
export default api; 