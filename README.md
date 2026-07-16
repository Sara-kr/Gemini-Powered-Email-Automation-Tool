# 📧 Gemini-Powered Email Automation Tool

> An AI-powered email automation system built with **Spring Boot**, **React**, **Google Gemini API**, and a **Chrome Extension** — enabling intelligent, context-aware email drafting directly inside Gmail.

---
## Screenshots

### Chrome Extension — Before AI Reply Generated
![Before Generated](screenshots/Before%20Generated.png)

### Chrome Extension — After AI Reply Generated
![After Generated](screenshots/After%20Generated.png)

### React App — Email Input View
![React Generated](screenshots/React%20generated.png)

### React App — After Reply Generated
![React After Generated](screenshots/React%20After%20Generated.png)

---

## 📌 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [System Architecture](#system-architecture)
- [Backend — Spring Boot](#backend--spring-boot)
  - [Project Structure](#project-structure)
  - [API Endpoints](#api-endpoints)
  - [Gemini API Integration](#gemini-api-integration)
  - [Configuration](#configuration)
- [Frontend — React](#frontend--react)
- [Chrome Extension](#chrome-extension)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
  - [Chrome Extension Setup](#chrome-extension-setup)
- [Environment Variables](#environment-variables)
- [Screenshots](#screenshots)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

The **Gemini-Powered Email Automation Tool** streamlines email communication by integrating Google's Gemini large language model into your email workflow. Users can generate intelligent, tone-aware email replies through a React web app or directly inside Gmail via a Chrome Extension — all powered by a robust Spring Boot backend.

**Key Features:**
- 🤖 AI-generated email replies using Google Gemini API
- 🎭 Tone customization — Professional, Casual, Friendly, Formal
- 🌐 React web interface for manual email drafting
- 🔌 Chrome Extension that injects an AI reply button directly into Gmail
- ⚡ RESTful Spring Boot backend with clean layered architecture
- 🔒 Input validation and structured prompt engineering on the backend

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17+, Spring Boot 3.x, Spring Web, Spring WebFlux |
| AI / LLM | Google Gemini API (`gemini-2.0-flash`) |
| Frontend | React.js, Axios |
| Browser Extension | Chrome Extension (Manifest V3), Vanilla JS |
| Build Tool | Maven |
| Config | `application.properties` / `.env` |

---

## System Architecture

```
┌─────────────────────┐         ┌──────────────────────────┐
│   Chrome Extension  │         │      React Frontend       │
│  (Gmail Injected UI)│         │  (Web-based Email Drafter)│
└────────┬────────────┘         └────────────┬─────────────┘
         │  HTTP POST /api/email/generate     │
         └──────────────────┬─────────────────┘
                            ▼
              ┌─────────────────────────┐
              │   Spring Boot Backend   │
              │                         │
              │  EmailGeneratorController│
              │         │               │
              │  EmailGeneratorService  │
              │         │               │
              │  GeminiApiClient        │
              └─────────┬───────────────┘
                        │ HTTPS POST
                        ▼
              ┌─────────────────────────┐
              │   Google Gemini API     │
              │  (gemini-2.0-flash)     │
              └─────────────────────────┘
```

---

## Backend — Spring Boot

The backend is the core of this project. It handles all prompt construction, Gemini API communication, response processing, and CORS configuration for the Chrome Extension and React frontend.

### Project Structure

```
email-writer-sb/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/email/writer/app/
│       │       ├── EmailGeneratorController.java   # REST Controller — exposes /api/email/generate
│       │       ├── EmailGeneratorService.java      # Business logic — prompt building & AI call
│       │       ├── EmailRequest.java               # Request DTO (emailContent, tone)
│       │       └── EmailWriterSbApplication.java   # Spring Boot entry point
│       └── resources/
│           └── application.properties              # Gemini API URL & Key config
├── pom.xml
```

### API Endpoints

#### `POST /api/email/generate`

Generates an AI-powered email reply based on the provided email content and optional tone.

**Request Body:**
```json
{
  "emailContent": "Hi, I wanted to follow up on our meeting last week regarding the project timeline...",
  "tone": "professional"
}
```

**Response:**
```
Dear [Name],

Thank you for reaching out. I wanted to follow up on our discussion...
```
> Returns a plain-text string of the AI-generated email reply.

**Supported Tones:**
- `professional`
- `casual`
- `friendly`
- `formal`

---

### Gemini API Integration

The `EmailGeneratorService` constructs a structured prompt and calls the Gemini REST API via Spring's `RestTemplate` or `WebClient`.

**Prompt Construction Logic (`EmailGeneratorService.java`):**
```java
private String buildPrompt(EmailRequest emailRequest) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("Generate a professional email reply for the following email content.\n");
    prompt.append("Please don't add a subject line.\n\n");
    if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
        prompt.append("Use a ").append(emailRequest.getTone()).append(" tone.\n");
    }
    prompt.append("Original email:\n").append(emailRequest.getEmailContent());
    return prompt.toString();
}
```

**Gemini API Call:**
```java
public String generateEmailReply(EmailRequest emailRequest) {
    // Build request body
    Map<String, Object> requestBody = new HashMap<>();
    Map<String, Object> textPart = Map.of("text", buildPrompt(emailRequest));
    Map<String, Object> content = Map.of("parts", List.of(textPart));
    requestBody.put("contents", List.of(content));

    // POST to Gemini endpoint
    String response = restTemplate.postForObject(
        geminiApiUrl + "?key=" + geminiApiKey,
        requestBody,
        String.class
    );

    // Extract and return the text content
    return extractResponseContent(response);
}
```

**Endpoint called:**
```
POST https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=YOUR_API_KEY
```

---

### Configuration

**`src/main/resources/application.properties`:**
```properties
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent
gemini.api.key=YOUR_GEMINI_API_KEY
spring.application.name=email-writer-sb
server.port=8080
```

**CORS Configuration** is configured to allow requests from `localhost:3000` (React frontend) and the Chrome Extension origin.

---

## Frontend — React

The React app provides a web-based interface where users can:
- Paste an existing email thread
- Select a response tone
- Click **Generate Reply** to invoke the backend API
- Copy the generated reply

**Located at:** `email-writer-react/`

The frontend calls:
```
POST http://localhost:8080/api/email/generate
```
with the email content and tone from the form.

---

## Chrome Extension

The Chrome Extension injects an **"AI Reply"** button directly into Gmail's compose and reply windows.

**How it works:**
1. User opens Gmail and clicks **Reply** on any email
2. The extension detects the email thread content
3. An **"AI Reply"** button appears in the compose toolbar
4. Clicking it sends the email content to the Spring Boot backend
5. The generated reply is inserted into the Gmail compose box

**Located at:** `email-writer-ext/`

**Key files:**
```
email-writer-ext/
├── manifest.json      # Chrome Extension Manifest V3
├── content.js         # DOM injection & Gmail integration
└── background.js      # Background service worker
```

---

## Getting Started

### Prerequisites

- Java 17+
- Node.js 18+ and npm
- Maven 3.8+
- Google Gemini API Key → [Get one here](https://aistudio.google.com/app/apikey)
- Chrome Browser (for the extension)

---

### Backend Setup

```bash
# 1. Navigate to the Spring Boot project
cd "Gemini-Powered Email Automation Tool/email-writer-sb"

# 2. Add your Gemini API key to application.properties
#    gemini.api.key=YOUR_API_KEY_HERE

# 3. Build and run
mvn spring-boot:run
```

The backend starts at `http://localhost:8080`.

**Test the API:**
```bash
curl -X POST http://localhost:8080/api/email/generate \
  -H "Content-Type: application/json" \
  -d '{
    "emailContent": "Hi, I am following up on our last meeting.",
    "tone": "professional"
  }'
```

---

### Frontend Setup

```bash
# 1. Navigate to the React project
cd "Gemini-Powered Email Automation Tool/email-writer-react"

# 2. Install dependencies
npm install

# 3. Start the development server
npm start
```

The React app starts at `http://localhost:3000`.

---

### Chrome Extension Setup

1. Open Chrome and navigate to `chrome://extensions/`
2. Enable **Developer Mode** (toggle in the top-right)
3. Click **"Load unpacked"**
4. Select the `email-writer-ext/` folder
5. Open **Gmail** — you will see the **AI Reply** button inside compose windows

> ⚠️ Make sure the Spring Boot backend is running at `http://localhost:8080` before using the extension.

---

## Environment Variables

| Variable | Description | Example |
|---|---|---|
| `gemini.api.url` | Gemini API endpoint URL | `https://generativelanguage.googleapis.com/...` |
| `gemini.api.key` | Your Google Gemini API key | `AIzaSy...` |
| `server.port` | Spring Boot server port | `8080` |

> 🔒 **Never commit your API key.** Add `application.properties` to `.gitignore` or use environment variable injection.

---


## Contribution

Frontend : Venkataraman K R | GitHub: https://github.com/VenkataramanaKR | LinkedIn: https://www.linkedin.com/in/venkataramana-k-r-a4a013252/

Backend : Saravanan K R | GitHub: https://github.com/Sara-kr | LinkedIn: https://www.linkedin.com/in/saravanan-k-r/

---

## Author

Saravanan

Java Backend Developer

GitHub: https://github.com/Sara-kr

---

<div align="center">

Built with ☕ Java + Spring Boot &nbsp;|&nbsp; ⚛️ React &nbsp;|&nbsp; 🤖 Google Gemini &nbsp;|&nbsp; 🔌 Chrome Extension

</div>
