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
    // Try to surface a helpful message from the backend.
    const message =
      (data && typeof data === "object" && (data.message || data.error)) ||
      (typeof data === "string" && data) ||
      `Request failed (${res.status})`;
    throw new Error(message);
  }

  return data;
}

