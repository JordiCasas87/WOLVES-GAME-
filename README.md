# 🐺 Wolves API

<p align="center">
  <img src="docs/Portada final.png" width="280" />
  <img src="docs/posterfinal1.png" width="280" />
</p>

Backend REST API for **Wolves**, a satirical technical interview game built with **Spring Boot**.

Wolves is a quiz-based game where players face absurd technical interviews conducted by “recruiter wolves”.  
The goal is to practice technical knowledge (starting with Java) in a humorous, game-like environment.

This project is currently **in development** and focuses on backend architecture and security concepts.

---

## 🎯 Project Concept

- Players create an avatar (represented as an animal).
- They face technical interview questions in the form of a quiz.
- Each game session simulates a job interview.
- Based on performance, players:
  - Pass or fail interviews
  - Earn fictional salaries
  - Climb the ranking

The tone of the game is **satirical**, inspired by the sometimes absurd nature of technical interviews.

---

## 🛠️ Tech Stack

- **Java 21**
- **Spring Boot**
- **Maven**
- **MongoDB**
- **Spring Security**
- **JWT (planned)**
- **Swagger / OpenAPI**

---

## 🧩 Backend Responsibilities

The frontend (web / mobile) will consume this API and is not part of this repository.

---

## 📦 Domain Overview

### Player
- Create player profiles
- Store scores and statistics
- Track failed questions
- Play normal or review sessions

### Question
- Manage technical questions
- Each question has:
  - Multiple answers
  - One correct option
- Questions are served randomly per game

### Game
- Start game sessions
- Handle scoring logic
- Save results and progress
- Generate interview outcomes

### Ranking
- Global ranking based on:
  - Interviews passed
  - Total fictional salary earned

### Security
- Authentication with JWT (in progress)
- Role-based access:
  - PLAYER
  - ADMIN (Wolf Boss)

---

## 👥 Roles

### 🐰 Player
- Create and manage their avatar
- Play interview sessions
- Review failed questions
- View rankings

### 🐺 Admin (Wolf Boss)
- Create, edit and delete questions
- Manage players
- Reset player statistics

---

## 🚧 Project Status

### Implemented / In Progress
- Project setup
- Architecture definition
- Domain modeling
- MongoDB integration
- Swagger documentation

### Planned
- JWT authentication
- Role-based authorization
- Admin endpoints
- Game logic refinement
- Frontend integration

---

## 📖 API Documentation

Once the application is running, Swagger UI will be available at: