# Newhanchat

**Newhanchat** is a robust, real-time messaging backend built with Spring Boot and MongoDB. Currently functioning as a secure chat service, this project serves as the foundation for a future full-scale social media platform.

It is designed to be self-hosted and powers a native Kotlin mobile application.

## 🚀 Features

* **Secure Authentication**: JWT-based registration and login system.
* **Real-Time Messaging**: Instant message delivery using WebSockets and STOMP protocol.
* **Message History**: Persistent chat history stored in MongoDB.
* **Message Status**: Tracking for message delivery (Sent, Delivered, Seen).
* **Editing**: Support for editing messages after they are sent.
* **User Presence**: Real-time user status updates (Online, Offline, Away, Busy).

## 🛠 Tech Stack

* **Language**: Java 21.
* **Framework**: Spring Boot 3.5.7.
* **Database**: MongoDB.
* **Security**: Spring Security, JWT.
* **Real-time**: WebSocket, STOMP.
* **Containerization**: Docker & Docker Compose.
* **Mobile**: Native Android (Kotlin, Jetpack Compose).

## 💻 System Requirements

* **Recommended RAM**: **16GB** is recommended to run the backend services and compile the Android app at the same time.
* **Workflow Tip**: If you have less RAM, you can first compile the Android app and then start the backend.

## 🏃 Getting Started

### Prerequisites
* **Docker & Docker Compose** (for the backend).
* **Android Studio** (for the mobile app).
* **Java 21 SDK** (if running locally without Docker).

### 1. Backend Setup (Docker)
The backend can be started via Docker Compose:
```bash
git clone <your-repo-url>
cd newhanchat
docker-compose up --build