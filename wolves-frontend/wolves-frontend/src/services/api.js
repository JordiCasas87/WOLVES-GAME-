const TOKEN_KEY = "wolves_token";

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

async function safeParseJson(response) {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

function truncateMessage(message, maxLen = 280) {
  const str = String(message ?? "").trim();
  if (!str) return "";
  return str.length > maxLen ? `${str.slice(0, maxLen - 1)}…` : str;
}

function extractErrorMessage(data) {
  if (typeof data === "string") return truncateMessage(data);
  if (!data || typeof data !== "object") return "";

  // Common shapes: { message: "..." } / { error: "..." } / { detail: "..." }
  if (typeof data.message === "string") return truncateMessage(data.message);
  if (Array.isArray(data.message)) {
    const parts = data.message
      .flatMap((item) => (typeof item === "string" ? [item] : []))
      .map((item) => item.trim())
      .filter(Boolean);
    if (parts.length) return truncateMessage(parts.join(" "));
  }
  if (typeof data.error === "string") return truncateMessage(data.error);
  if (typeof data.detail === "string") return truncateMessage(data.detail);
  if (typeof data.title === "string") return truncateMessage(data.title);

  // Validation errors: { errors: [...] } or { errors: { field: "msg" } }
  if (Array.isArray(data.errors)) {
    const parts = data.errors
      .map((item) => {
        if (!item || typeof item !== "object") return "";
        const field = typeof item.field === "string" ? item.field.trim() : "";
        const msg =
          (typeof item.defaultMessage === "string" && item.defaultMessage.trim()) ||
          (typeof item.message === "string" && item.message.trim()) ||
          (typeof item.error === "string" && item.error.trim()) ||
          "";
        return truncateMessage(field && msg ? `${field}: ${msg}` : msg);
      })
      .filter(Boolean);
    if (parts.length) return truncateMessage(parts.join(" "));
  }

  if (data.errors && typeof data.errors === "object" && !Array.isArray(data.errors)) {
    const parts = Object.entries(data.errors)
      .map(([key, value]) => {
        const msg = typeof value === "string" ? value : JSON.stringify(value);
        return truncateMessage(`${key}: ${msg}`);
      })
      .filter(Boolean);
    if (parts.length) return truncateMessage(parts.join(" "));
  }

  // Last resort: stringify a small object without showing "[object Object]".
  try {
    return truncateMessage(JSON.stringify(data));
  } catch {
    return "";
  }
}

export async function apiRequest(
  path,
  { method = "GET", body, headers = {}, auth = true } = {},
) {
  const finalHeaders = { ...headers };

  if (body !== undefined) {
    finalHeaders["Content-Type"] = finalHeaders["Content-Type"] ?? "application/json";
  }

  if (auth) {
    const token = getToken();
    if (token) finalHeaders.Authorization = `Bearer ${token}`;
  }

  const res = await fetch(path, {
    method,
    headers: finalHeaders,
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  const data = await safeParseJson(res);

  if (!res.ok) {
    const backendMessage = extractErrorMessage(data);
    const message = backendMessage || `Request failed (${res.status})`;
    const err = new Error(message);
    err.status = res.status;
    err.data = data;
    throw err;
  }

  return data;
}
