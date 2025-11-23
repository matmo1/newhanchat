# Newhanchat

**Newhanchat** is a robust, real-time messaging backend built with Spring Boot and MongoDB. Currently functioning as a secure chat service, this project serves as the foundation for a future full-scale social media platform.

It is designed to be self-hosted and will soon power a native Kotlin mobile application.

## 🚀 Features

* **Secure Authentication**: JWT-based registration and login system.
* **Real-Time Messaging**: Instant message delivery using WebSockets and STOMP protocol.
* **Message History**: Persistent chat history stored in MongoDB.
* **Message Status**: Tracking for message delivery (Sent, Delivered, Seen).
* **Editing**: Support for editing messages after they are sent.
* **User Presence**: Real-time user status updates (Online, Offline, Away, Busy).

## 🛠️ Tech Stack

* **Language**: Java 21
* **Framework**: Spring Boot 3.5.7
* **Database**: MongoDB
* **Security**: Spring Security, JWT
* **Real-time**: WebSocket, STOMP
* **Containerization**: Docker & Docker Compose

## 📦 Getting Started

### Prerequisites
* Docker & Docker Compose
* Java 21 SDK (if running locally without Docker)

### Running with Docker (Recommended)

1.  **Clone the repository**:
    ```bash
    git clone <your-repo-url>
    cd newhanchat
    ```

2.  **Start the services**:
    This will spin up both the backend and the MongoDB database.
    ```bash
    docker-compose up --build
    ```

The API will be available at `http://localhost:8080`.

## 🔌 API Endpoints

### Authentication
* `POST /api/users/register` - Register a new user
* `POST /api/users/login` - Login and receive JWT token

### Chat & History
* `GET /api/messagages/history?senderId=...&recipientId=...` - Fetch chat history
* **WebSocket Endpoint**: `/ws-chat`
    * **Subscribe**: `/user/queue/messages` (Private messages)
    * **Subscribe**: `/user/queue/notifications` (Delivery updates)
    * **Send Destination**: `/app/chat`

## 🔮 Roadmap

* [x] Core Messaging Backend
* [ ] **Mobile App**: Native Android application built with Kotlin (In Progress).
* [ ] **Social Features**: Feed, user profiles, and media sharing.
* [ ] **Encryption**: End-to-End encryption for private chats.