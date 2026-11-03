# Ponimanie API

Small backend for the Ponimanie Android app. The OpenAI API key stays on the server and must never be placed in the Android package or committed to Git.

## Configuration

Copy `.env.example` to `.env` and set `OPENAI_API_KEY`, `APP_TOKEN`, and optionally `OPENAI_MODEL`.

## Run

Run `npm install`, then `npm start`. The health endpoint is `GET /health`.

Send `POST /v1/chat` with header `x-app-token` and JSON body containing a `messages` array. Each item has `role` (`user` or `assistant`) and `content`.

## Security note

The shared app token is only a basic gate and can be extracted from a distributed APK. Before public release, add per-user authentication, quotas, monitoring, and abuse prevention. Never expose an unrestricted endpoint that can spend the owner's API balance.
