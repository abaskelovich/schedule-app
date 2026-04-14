# Schedule App

Mobile application for creating, viewing, editing, and deleting daily schedules with AI-powered natural language processing.

## Project Structure

- **backend/** - Node.js serverless backend (Vercel-compatible) with JSON file-based storage and OpenAI API integration.
- **android/** - Kotlin Android application with MVVM architecture, Retrofit, and Material Design UI.

## Features

- **List View:** View all scheduled events.
- **Day View:** Visual timeline of events for the current day (08:00 to 22:00).
- **Quick Input (AI):** Add events using natural language (e.g., "Coffee tomorrow at 10am"). Powered by OpenAI `gpt-4o-mini`.

## Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Create a `.env` file in the `backend/` directory and add your OpenAI API key:
   ```env
   OPENAI_API_KEY=your_api_key_here
   ```
4. Start the local server:
   ```bash
   node server.js
   ```
   *The API will be available at `http://localhost:3000`.*

## Android App Setup

1. Open the `android` folder in Android Studio.
2. Ensure you have an emulator running (or a physical device connected).
3. Build and run the app. 
   *Note: By default, the app is configured to connect to `http://10.0.2.2:3000/` (the Android emulator's alias for your machine's localhost).*

## API Endpoints

**Schedule Management:**
- `GET /api/schedules` - Get all schedules
- `GET /api/schedules/{id}` - Get schedule by ID
- `POST /api/schedules` - Create new schedule
- `PUT /api/schedules/{id}` - Update schedule
- `DELETE /api/schedules/{id}` - Delete schedule

**AI Integration:**
- `POST /api/parse-intent` - Parse natural language into a structured schedule object. Requires `{"input": "text"}` in the body.

## Deploy to Vercel

```bash
cd backend
vercel deploy
```
*Don't forget to configure the `OPENAI_API_KEY` environment variable in your Vercel project settings.*
