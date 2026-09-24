# Streamix Social JVM Backend

Standalone Kotlin/JVM + Ktor service for AniLab Social.

## Runtime
- JDK 17
- Ktor + Netty
- PostgreSQL
- Google Web OAuth ID-token verification
- HMAC-SHA256 application sessions
- WebSocket realtime chat/watch sync

## Required environment
DATABASE_URL=jdbc:postgresql://host:5432/anilab
DATABASE_USER=postgres
DATABASE_PASSWORD=...
GOOGLE_WEB_CLIENT_ID=...
ANILAB_SESSION_SECRET=...
PORT=8080

## API
POST /auth/google
GET /users/me
GET /social/dashboard
GET /social/friends
POST /social/friends/{uid}
POST /social/friends/{uid}/accept
GET/POST /social/chat
GET /social/leaderboard
GET /social/activity
GET /social/friends/active
POST /social/watch-together
POST /social/watch-together/{id}/join
PUT /social/watch-together/{id}

WebSocket:
- /ws/social?room=global
- /ws/social?room=anime:{animeId}
- /ws/social?room=watch:{roomId}

The service creates its PostgreSQL schema automatically on startup.
