# Schedule App

Mobile application for creating, viewing, editing and deleting daily schedules.

## Project Structure

- **backend/** - Vercel serverless backend with SQLite database
- **android/** - Kotlin Android application

## Backend Setup

```bash
cd backend
npm install
npm run dev
```

The API will be available at `http://localhost:3000/api/schedules`

## Android App Setup

1. Open the `android` folder in Android Studio
2. Build and run the app
3. The app connects to `http://10.0.2.2:3000/` (Android emulator localhost)

## API Endpoints

- `GET /api/schedules` - Get all schedules
- `GET /api/schedules/{id}` - Get schedule by ID
- `POST /api/schedules` - Create new schedule
- `PUT /api/schedules/{id}` - Update schedule
- `DELETE /api/schedules/{id}` - Delete schedule

## Deploy to Vercel

```bash
cd backend
vercel deploy
```
