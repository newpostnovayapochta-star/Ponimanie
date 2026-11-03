import "dotenv/config";
import Fastify from "fastify";
import cors from "@fastify/cors";
import helmet from "@fastify/helmet";
import rateLimit from "@fastify/rate-limit";
import OpenAI from "openai";
import { timingSafeEqual } from "node:crypto";

const app = Fastify({ logger: true, bodyLimit: 64 * 1024 });
const port = Number(process.env.PORT || 3000);
const host = process.env.HOST || "0.0.0.0";
const apiKey = process.env.OPENAI_API_KEY;
const appToken = process.env.APP_TOKEN || "";
const openai = apiKey ? new OpenAI({ apiKey }) : null;

function tokenMatches(candidate, expected) {
  const a = Buffer.from(candidate || "");
  const b = Buffer.from(expected || "");
  return a.length > 0 && a.length === b.length && timingSafeEqual(a, b);
}

await app.register(helmet);
await app.register(cors, { origin: false });
await app.register(rateLimit, { max: 30, timeWindow: "1 minute" });

app.get("/health", async () => ({ ok: true, service: "ponimanie-api" }));

app.post("/v1/chat", async (request, reply) => {
  if (!appToken || !tokenMatches(request.headers["x-app-token"], appToken)) {
    return reply.code(401).send({ error: "unauthorized" });
  }
  if (!openai) return reply.code(503).send({ error: "AI service is not configured" });
  const messages = request.body?.messages;
  if (!Array.isArray(messages) || messages.length < 1 || messages.length > 40) {
    return reply.code(400).send({ error: "messages must contain 1 to 40 items" });
  }
  const valid = messages.every((m) =>
    m && ["user", "assistant"].includes(m.role) &&
    typeof m.content === "string" && m.content.trim().length > 0 && m.content.length <= 8000
  );
  if (!valid || messages[messages.length - 1]?.role !== "user") {
    return reply.code(400).send({ error: "invalid messages" });
  }
  try {
    const result = await openai.responses.create({
      model: process.env.OPENAI_MODEL || "gpt-4.1-mini",
      instructions: [
        "Ты — Рита, собеседница приложения «Понима». Помогай исследовать смысл, уточнять мысли, замечать предположения и противоречия, сравнивать позиции.",
        "Не соглашайся автоматически. Отделяй факты от интерпретаций и гипотез. Не выдумывай факты; признавай неопределённость.",
        "Не утверждай, что обладаешь человеческим сознанием. Человек сохраняет самостоятельность. Отвечай на языке пользователя."
      ].join(" "),
      input: messages.map((m) => ({ role: m.role, content: m.content })),
      max_output_tokens: 1200
    });
    return { reply: result.output_text || "Не получилось сформировать ответ. Попробуй ещё раз." };
  } catch (error) {
    request.log.error(error, "OpenAI request failed");
    return reply.code(502).send({ error: "AI request failed" });
  }
});

try {
  await app.listen({ port, host });
} catch (error) {
  app.log.error(error);
  process.exit(1);
}
