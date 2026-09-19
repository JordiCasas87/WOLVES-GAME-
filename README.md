
# 🐺 WOLVES – The Interview Game

<p align="center">
  <img src="docs/Portada final.png" width="280" />
  <img src="docs/posterfinal1.png" width="280" />
</p>

Backend & Frontend application for **Wolves**, a satirical technical interview game built with **Spring Boot**, **React** and **Vite**.

**Wolves** is a quiz-based game where players face absurd technical interviews conducted by “recruiter wolves”.  
The goal is to practice technical knowledge (starting with Java) in a humorous, narrative-driven, game-like environment.

This project has been developed as an **academic project** within the **IT Academy (2026)**.

---
<p align="center">
  <img src="docs/login.png" width="280" />
  <img src="docs/portada.png" width="280" />
</p>

## 🎯 Project Concept

- Players create an avatar (represented as an animal).
- Each game session simulates a technical job interview.
- Interviews consist of multiple-choice technical questions.
- Based on performance, players:
  - Pass or fail interviews
  - Earn fictional salaries
  - Accumulate results and statistics
  - Climb a global ranking

The tone of the game is **satirical**, inspired by the sometimes absurd nature of real technical interviews.

<p align="center">
  <img src="docs/partida.png" width="280" />
  <img src="docs/personaje.png" width="280" />
</p>


---

## 🛠️ Tech Stack

### Backend
- **Java 21**
- **Spring Boot**
- **Maven**
- **MongoDB**
- **Spring Security**
- **JWT Authentication**
- **Swagger / OpenAPI**

### Frontend
- **React**
- **Vite**
- **JavaScript**
- **CSS**

---

## 🧩 Domain Overview

### Player
- User registration & login
- Stores statistics:
  - Games played
  - Games passed
  - Total fictional salary earned
  - Failed questions history
- Can play:
  - Normal interview sessions
  - Review sessions (only failed questions)

### Question
- Technical questions (Java-focused)
- Each question includes:
  - Multiple answers
  - One correct option
- Questions are served randomly per game

### Game
- Represents an interview session
- Manages:
  - Question flow
  - Scoring
  - Progress
  - Final result
- Produces:
  - Pass / fail outcome
  - Salary reward
  - Final “wolf message”

### Ranking
- Global ranking based on:
  - Total fictional salary earned

---

## 👥 Roles & Security

### 🐰 Player (ROLE_USER)
- Register & login
- Play interview games
- Play review games (mistakes mode)
- View rankings

### 🐺 Admin – “Wolf Boss” (ROLE_ADMIN)
- Manage players
- Manage questions
- Access administrative endpoints

Role-based authorization is enforced using **Spring Security + JWT**.  
The frontend UI adapts automatically based on the authenticated user role.

<p align="center">
  <img src="admin.png" width="280" />
  <img src="admin1.png" width="280" />
</p>

### 🏗️ Architecture

The backend is organized using a domain-based (feature-based) architecture, where the code is grouped by business domains instead of technical layers. Each domain contains its own controllers, services, repositories, DTOs, mappers and models.

Conceptually, the project follows the MVC pattern: domain entities represent the Model, REST controllers act as the Controller, and the View is implemented separately in a React frontend. MVC is applied logically rather than through a strict folder structure.

The architecture is inspired by Domain-Driven Design (DDD) principles, but keeps a pragmatic and simplified approach suitable for an academic project, focusing on clarity, maintainability and scalability without unnecessary complexity.

---

## 🖥️ Frontend Details

The frontend is **included in this repository** and built with **React + Vite**.

It is responsible for:
- Game flow visualization
- User interaction
- Animations and transitions
- API communication with the backend

---

## 🎨 Visuals, Audio & AI Usage

- Visual assets, backgrounds and animations were generated using **generative AI tools**.
- Some music tracks and sound effects are used **only as examples**.
- No commercial use is intended.
- The project has **no economic remuneration**.
- All assets are used strictly for **academic and demonstrative purposes**.

---

## 🧪 Testing strategy and retrospective review

The minimum viable version of Wolves was originally completed as a three-week
academic project. That delivery prioritized the REST API, game logic, security
and connection with the frontend, and it did not include the level of automated
test coverage that the backend deserved.

In **September 2026**, some time after closing that initial version, the project
was revisited as a separate learning and quality-improvement exercise. This
review followed several later projects and exercises through which new testing
practices had been learned. Returning to Wolves made it possible to apply that
knowledge to an existing application, assess earlier decisions honestly and
turn the repository into a clearer academic example of a backend testing
pyramid.

The work was developed incrementally and remains visible in the Git history:

- `testing-pyramid` introduced the unit, mapper, controller, security and first
  integration tests by domain and behavior.
- `integration-tests` expanded representative flows across controllers,
  services, mappers, security and repository boundaries.
- `e2e-tests` completed the top of the pyramid with a deliberately small set of
  critical backend journeys using the complete application and an isolated
  MongoDB instance.

The resulting pyramid is organized as follows:

- **Unit tests** form the broad base and cover services, business rules, JWT
  handling and DTO mappers with JUnit 5 and Mockito.
- **Web slice tests** cover controllers, request validation, JSON contracts,
  HTTP status codes and exception translation with MockMvc.
- **Security tests** verify JWT filtering and role-based endpoint authorization.
- **Integration tests** verify collaboration between the main application
  layers while isolating external infrastructure where appropriate.
- **End-to-end tests** start the backend on a real HTTP port and exercise JWT
  security and persistence against MongoDB 7 through Testcontainers.

This later review was not intended to disguise the scope of the original MVP.
Its purpose was to document progress, correct an important omission and use a
finished project to practise how a maintainable test package can be designed at
an academic scale. The tests were added incrementally so that their evolution
remains reviewable rather than appearing as a single unexplained change.

At the end of this review, the backend suite contains **132 passing tests**
across unit, mapper, web slice, security, integration and end-to-end levels.
Docker must be running for the Testcontainers-based E2E tests.

The complete suite can be run from the backend directory with:

```bash
cd wolves-api
./mvnw test
```

Codex was used during this later phase as an AI assistant for planning,
drafting and documenting parts of the test suite, as well as for technical
consultation throughout the learning process. Every proposed test was reviewed,
executed and validated by the author, who remains responsible for the testing
strategy, implementation decisions and final result.

---

## 📌 Known technical debt

Wolves remains an academic MVP rather than a production-ready service. The
following limitations are known and have been left as possible future work if
the application continues to evolve:

- Enforce ownership checks on every game operation so that an authenticated
  player can only read or modify their own games.
- Add rate limiting to authentication endpoints and strengthen request
  validation for registration, login and user-provided content.
- Introduce a more complete JWT lifecycle with shorter-lived access tokens,
  refresh tokens and an explicit revocation strategy.
- Add continuous integration to run the test suite and automated dependency
  security checks on every relevant change.
- Add structured observability and performance testing to evaluate the service
  under realistic operational conditions.

These points are documented deliberately rather than presented as completed
features. The original project fulfilled the scope of a three-week final course
assignment, and the later reviews reflect an ongoing student learning process:
revisiting earlier work, applying knowledge gained through subsequent exercises
and identifying the next engineering decisions that a real evolution of the
application would require.

---

## 🚀 How to Run the Project Locally

The project is split into **backend** and **frontend**, both included in this repository.

---

### 1️⃣ Backend (Spring Boot API)

**Requirements:**
- Java 21
- Maven
- MongoDB running locally
- Docker running when executing the E2E test suite

From the backend root folder:

```bash
cd wolves-api
mvn spring-boot:run
```

The backend will start on:
`http://localhost:8080`

Swagger UI:
`http://localhost:8080/swagger-ui.html`


### 2️⃣ Frontend (React + Vite)

Requirements:
	•	Node.js (v18+ recommended)
	•	npm

From the frontend root folder:

```bash
cd wolves-frontend
npm install
npm run dev
```

The frontend will be available at:
http://localhost:5173

### 🧠 Academic Context & Learning Goals

This project was developed in 2026 as part of the IT Academy program.

The focus of the project is:
	•	Backend architecture and clean design
	•	REST API development
	•	Security with JWT & roles
	•	Game logic modeling
	•	AI-assisted frontend generation
	•	Understanding, analyzing and adapting AI-generated code

The goal is learning and comprehension, not production deployment.


⸻

👤 Author

Jordi Casas
IT Academy – 2026

Design, backend development, game logic, API architecture, security
and survival against the wolf.


⸻

📜 License & Usage Notice

This project (Wolves – The Interview Game) is an academic and creative work developed by Jordi Casas.

All source code, game mechanics, narrative concept, characters, dialogues, and audiovisual ideas are original and are protected by copyright.

Usage conditions
	•	❌ Commercial use is NOT allowed
	•	❌ Redistribution of the project or its concept is NOT allowed
	•	❌ Copying the game idea, mechanics, narrative, or structure for another project is NOT allowed
	•	❌ Using this project (or parts of it) as the basis for a commercial or public product is NOT allowed

What is allowed
	•	✅ Viewing the code for educational purposes
	•	✅ Running the project locally to explore its architecture and functionality
	•	✅ Studying the implementation as a learning reference

Any use beyond educational or personal learning purposes requires explicit written permission from the author.

⸻

Intellectual Property

This repository contains:
	•	Original game concept and mechanics
	•	Custom narrative content and dialogues
	•	Backend and frontend architecture
	•	Generated audiovisual assets (used for academic/demo purposes only)

The project is not intended for commercial use at this stage.
Some audiovisual assets and audio tracks are used as examples only and remain the property of their respective owners.

⸻

© 2026 — Jordi Casas
Academic project developed at IT Academy
