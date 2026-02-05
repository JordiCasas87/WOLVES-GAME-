import { apiRequest, setToken } from "./api";

export async function login({ name, password }) {
  const data = await apiRequest("/api/auth/login", {
    method: "POST",
    body: { name, password },
    auth: false,
  });

  if (data?.token) setToken(data.token);
  return data;
}

export async function register({ name, password, age }) {
  const data = await apiRequest("/api/auth/register", {
    method: "POST",
    body: { name, password, age },
    auth: false,
  });

  if (data?.token) setToken(data.token);
  return data;
}

