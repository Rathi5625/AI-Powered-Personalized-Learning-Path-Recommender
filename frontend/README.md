# LearnPath AI Frontend

React + TypeScript + Vite frontend for the AI-Powered Personalized Learning Path Recommender.

## Pages

| Route | Page |
|-------|------|
| `/` | Landing (3D hero scene) |
| `/signup` | Sign up |
| `/login` | Sign in |
| `/onboarding` | Learning preferences wizard |
| `/career-selection` | Pick target career |
| `/dashboard` | Overview & quick links |
| `/skill-gap` | Skill gap analysis + 3D globe |
| `/recommendations` | ML + rule-based course picks |
| `/learning-path` | AI-generated phased path |
| `/progress` | Course progress tracking |
| `/adaptive-learning` | Adaptive path updates |
| `/profile` | Profile & settings |

## Setup

```bash
cd frontend
npm install
npm run dev
```

The dev server runs on `http://localhost:5173` and proxies `/api` to the Spring Boot backend at `http://localhost:8080`.

## Backend

Start the Java backend first:

```bash
cd backend/learning-path-backend
./mvnw spring-boot:run
```

Optional: set `VITE_API_URL` in `.env` if the API is hosted elsewhere.

## Tech stack

- React 19 + TypeScript
- React Router 7
- Tailwind CSS 4
- Three.js via `@react-three/fiber` and `@react-three/drei`
