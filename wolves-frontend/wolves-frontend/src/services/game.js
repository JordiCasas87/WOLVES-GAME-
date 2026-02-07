import { apiRequest } from "./api";

export async function createGame(difficulty) {
  return apiRequest(`/game/new?difficulty=${encodeURIComponent(difficulty)}`, { method: "POST" });
}

export async function createMistakesGame() {
  return apiRequest("/game/mistakes", { method: "POST" });
}

export async function nextQuestion(gameId) {
  return apiRequest(`/game/${encodeURIComponent(gameId)}/nextQuestion`);
}

export async function answerQuestion(gameId, selectedAnswer) {
  return apiRequest(`/game/${encodeURIComponent(gameId)}/answer`, {
    method: "POST",
    body: { selectedAnswer },
  });
}

export async function getResult(gameId) {
  return apiRequest(`/game/${encodeURIComponent(gameId)}/result`);
}

