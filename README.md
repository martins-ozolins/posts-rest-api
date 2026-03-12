# Posts REST API

A Spring Boot REST API with JWT cookie-based authentication, refresh tokens, and PostgreSQL.

## Prerequisites

- Java 17+
- Maven
- Docker & Docker Compose

## Getting Started

### 1. Set up environment variables

Copy `.env.example` to `.env` and fill in your values:

```bash
cp .env.example .env
```

```env
DB_URL=jdbc:postgresql://localhost:5433/postsdb
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
JWT_SECRET=your_jwt_secret_here
```

### 2. Start the database

```bash
docker compose up -d
```

This starts a PostgreSQL 16 instance on port `5433`.

### 3. Run the application

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8081`.

---

## API Endpoints

### Auth

| Method | Endpoint         | Auth required | Description          |
|--------|------------------|---------------|----------------------|
| POST   | `/auth/register` | No            | Register a new user  |
| POST   | `/auth/login`    | No            | Log in               |
| POST   | `/auth/logout`   | No            | Revoke refresh token and clear cookies   |
| POST   | `/auth/refresh`  | No            | Issue new token pair using refresh token |
| GET    | `/auth/me`       | Yes           | Get current user                         |

### Public Posts

| Method | Endpoint      | Auth required | Description       |
|--------|---------------|---------------|-------------------|
| GET    | `/posts`      | No            | List all posts    |
| GET    | `/posts/{id}` | No            | Get post by ID    |

### My Posts (authenticated)

| Method | Endpoint          | Auth required | Description             |
|--------|-------------------|---------------|-------------------------|
| GET    | `/me/posts`       | Yes           | List your posts         |
| GET    | `/me/posts/{id}`  | Yes           | Get your post by ID     |
| POST   | `/me/posts`       | Yes           | Create a post           |
| PUT    | `/me/posts/{id}`  | Yes           | Update a post           |

Authentication is handled via `HttpOnly` cookies set automatically on login. The `access_token` is short-lived (15 min). The `refresh_token` is long-lived (15 days), stored in the database, rotated on each refresh, and revoked on logout.
