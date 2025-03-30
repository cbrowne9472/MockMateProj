# MockMate

A full-stack application for conducting AI-powered mock interviews, featuring:

- Speech-to-text transcription using Google Cloud
- AI-driven interview simulation
- Modern React frontend with Auth0 authentication
- Spring Boot backend with MongoDB storage

## Project Structure

This repository contains:

- **Frontend**: React application in the `mockMate-front-main` directory
- **Backend**: Spring Boot application in the `mockMate-main` directory

## Features

- Text-based chat interface for interviewing
- Voice recording with transcription
- User authentication via Auth0
- Conversation history persistence
- Responsive, modern UI design

## Technology Stack

### Frontend
- React
- Auth0 for authentication
- Axios for API requests

### Backend
- Spring Boot
- Spring AI for AI integration
- MongoDB for data storage
- Google Cloud Speech-to-Text API

## Getting Started

### Prerequisites
- Node.js and npm
- Java 21
- Maven
- MongoDB instance
- Google Cloud account with Speech-to-Text API enabled

### Running the Backend
```bash
cd mockMate-main
export GOOGLE_APPLICATION_CREDENTIALS="path/to/your/credentials.json"
./mvnw spring-boot:run
```

### Running the Frontend
```bash
cd mockMate-front-main
npm install
npm start
```

The frontend will be available at http://localhost:3000 and will connect to the backend at http://localhost:8080.

## License
This project is open source and available under the MIT License.