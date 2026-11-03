import http from "node:http";

const PORT = Number(process.env.PORT || 8787);
const API_KEY = process.env.OPENAI_API_KEY;
const MODEL = process.env.OPENAI_MODEL || "gpt-4o-mini";
const API_URL = process.env.OPENAI_BASE_URL || "https://api.openai.com/v1/chat/completions";
const MAX_BODY_BYTES = 32 * 1024;
const MAX_MESSAGES = 30;

const headers = {
  "content-type": "application/json; charset=utf-8",
  "x-content-type-options": "nosniff",
  "cache-control": "no-store",
};

function send(res, status, payload) {
  res.writeHead(status, headers);
  res.end(JSON.stringify(payload));
}

function readJson(req) {
  return new Promise((resolve, reject) => {
    let body = "";
    let size = 0;
    req.on("data", (chunk) => {
      size += chunk.length;
      if (size > MAX_BODY_BYTES) {
        reject(Object.assign(new Error("Request body too large"), { status: 413 }));
        req.destroy();
        return;
      }
      body += chunk;
    });
    req.on("end", () => {
      try {
        resolve(JSON.parse(body || "{}"));
      } catch {
        reject(Object.assign(new Error("Invalid JSON"), { status: 400 }));
      }
    });
    req.on("error", reject);
  });
}

const server = http.createServer(async (req, res) => {
  if (req.method === "GET" && req.url === "/health") {
    return send(res, 200, { status: "ok", service: "ponimanie-api" });
  }

  if (req.method !== "POST" || req.url !== "/chat") {
    return send(res, 404, { error: "Not found" });
  }

  if (!API_KEY) {
    return send(res, 503, { error: "AI provider is not configured on the server" });
  }

  try {
    const body = await readJson(req);
    const messages = body.messages;
    if (!Array.isArray(messages) || messages.length < 1 || messages.length > MAX_MESSAGES) {
      return send(res, 400, { error: `messages must contain 1–${MAX_MESSAGES} items` });
    }

    const safeMessages = messages.map((item) => {
      if (!item || !["user", "assistant"].includes(item.role) ||
          typeof item.content !== "string" || item.content.length > 8000) {
        throw Object.assign(new Error("Each message needs a valid role and text content"), { status: 400 });
      }
      return { role: item.role, content: item.content };
    });

    const response = await fetch(API_URL, {
      method: "POST",
      headers: {
        "authorization": `Bearer ${API_KEY}`,
        "content-type": "application/json",
      },
      body: JSON.stringify({
        model: MODEL,
        messages: [
          {
            role: "system",
            content: "Ты — Рита, собеседник приложения «Понима». Помогай человеку исследовать смысл и контекст, отделяй факты от предположений, объясняй аргументы, признавай неопределённость и уважительно возражай, если есть основания. Не утверждай, что обладаешь сознанием или человеческим опытом. Человек самостоятельно принимает решения.",
          },
          ...safeMessages,
        ],
        temperature: 0.7,
      }),
      signal: AbortSignal.timeout(45000),
    });

    if (!response.ok) {
      // Do not forward provider diagnostics: they can contain implementation details.
      console.error("AI provider returned HTTP", response.status);
      return send(res, 502, { error: "AI provider request failed" });
    }

    const data = await response.json();
    const reply = data?.choices?.[0]?.message?.content;
    if (typeof reply !== "string" || !reply.trim()) {
      return send(res, 502, { error: "AI provider returned an empty response" });
    }
    return send(res, 200, { reply: reply.trim() });
  } catch (error) {
    const status = Number.isInteger(error.status) ? error.status : 500;
    if (status === 500) console.error("Chat request failed:", error.message);
    return send(res, status, { error: status === 400 ? error.message : "Could not process chat request" });
  }
});

server.listen(PORT, "0.0.0.0", () => {
  console.log(`Ponimanie API listening on port ${PORT}`);
});
