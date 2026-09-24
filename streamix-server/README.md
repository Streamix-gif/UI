# AniLab JVM Backend

Kotlin/JVM + Ktor backend for AniLab account and Social features.

## Runtime

- JDK 17
- Ktor 3.3.1 / Netty
- PostgreSQL
- Google ID-token verification against the configured Web OAuth client ID
- HMAC-SHA256 application session tokens
- WebSocket rooms for real-time Social chat

The Android client sends its Google/Firebase-authenticated ID token to \`POST /auth/google\`. The backend verifies the token audience against \`GOOGLE_WEB_CLIENT_ID\`, creates/updates the AniLab user, and returns an application session token.

## Required environment

\`\`\`
DATABASE_URL=jdbc:postgresql://host:5432/anilab
DATABASE_USER=postgres
DATABASE_PASSWORD=...
GOOGLE_WEB_CLIENT_ID=...
ANILAB_SESSION_SECRET=long-random-secret
PORT=8080
\`\`\`

Do not commit database passwords, service-account JSON, OAuth secrets, or session secrets.

## Social API

REST:
- \`GET /health\`
- \`POST /auth/google\`
- \`GET /users/me\`
- \`PUT /users/me/profile\`
- \`GET /social/dashboard\`
- \`GET /social/friends\`
- \`POST /social/friends/{uid}\`
- \`POST /social/friends/{uid}/accept\`
- \`GET /social/chat?roomType=global|anime|watch&roomId=...\`
- \`POST /social/chat\`
- \`GET /social/leaderboard\`
- \`GET /social/activity\`
- \`GET /social/friends/active\`
- \`POST /social/watch-together\`
- \`POST /social/watch-together/{id}/join\`
- \`PUT /social/watch-together/{id}\`

Real-time:
- \`WS /ws/social?room=global\`
- \`WS /ws/social?room=anime:{animeId}\`
- \`WS /ws/social?room=watch:{roomId}\`

All routes except \`/health\` and \`/auth/google\` require \`Authorization: Bearer <session-token>\`.

The PostgreSQL schema is also checked into \`src/main/resources/social-schema.sql\`; startup runs the same idempotent schema so a clean database can bootstrap automatically.
