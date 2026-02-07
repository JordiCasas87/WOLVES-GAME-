import { useEffect, useMemo, useState } from "react";
import {
  answerQuestion,
  createGame,
  createMistakesGame,
  getResult,
  nextQuestion,
} from "../services/game";

const TOTAL_QUESTIONS = 10;

function normalizeDifficulty(difficulty) {
  const value = String(difficulty ?? "easy").trim().toLowerCase();
  if (value === "hard") return "HARD";
  if (value === "medium") return "MEDIUM";
  return "EASY";
}

function difficultyLabel(mode, difficulty) {
  if (mode === "mistakes") return "REPASO";
  return normalizeDifficulty(difficulty);
}

function Game({ onFinish, onBack, difficulty, mode = "new" }) {
  const difficultyEnum = useMemo(() => normalizeDifficulty(difficulty), [difficulty]);
  const titlePill = useMemo(
    () => difficultyLabel(mode, difficultyEnum),
    [mode, difficultyEnum],
  );

  const [gameId, setGameId] = useState("");
  const [gameMessage, setGameMessage] = useState("");

  const [question, setQuestion] = useState(null);
  const [selectedAnswerIndex, setSelectedAnswerIndex] = useState(null);
  const [wolfResponse, setWolfResponse] = useState(null);

  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let isActive = true;

    async function start() {
      setIsLoading(true);
      setError("");
      setGameId("");
      setGameMessage("");
      setQuestion(null);
      setSelectedAnswerIndex(null);
      setWolfResponse(null);

      try {
        const game =
          mode === "mistakes" ? await createMistakesGame() : await createGame(difficultyEnum);
        if (!isActive) return;

        setGameId(game?.id ?? "");
        setGameMessage(game?.message ?? "");

        const first = await nextQuestion(game.id);
        if (!isActive) return;
        setQuestion(first);
      } catch (err) {
        if (!isActive) return;
        setError(err instanceof Error ? err.message : "Error al iniciar la partida.");
      } finally {
        if (isActive) setIsLoading(false);
      }
    }

    start();
    return () => {
      isActive = false;
    };
  }, [mode, difficultyEnum]);

  const submitAnswer = async (index) => {
    if (!gameId || !question || isSubmitting || wolfResponse) return;
    setError("");
    setIsSubmitting(true);
    setSelectedAnswerIndex(index);

    try {
      const res = await answerQuestion(gameId, index);
      setWolfResponse(res);
    } catch (err) {
      setSelectedAnswerIndex(null);
      setError(err instanceof Error ? err.message : "Error al enviar la respuesta.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const goNext = async () => {
    if (!gameId || !question || isLoading) return;

    setError("");

    if (question.numberQuestion >= TOTAL_QUESTIONS) {
      setIsLoading(true);
      try {
        const result = await getResult(gameId);
        onFinish?.(result);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Error al cargar el resultado.");
      } finally {
        setIsLoading(false);
      }
      return;
    }

    setWolfResponse(null);
    setSelectedAnswerIndex(null);
    setIsLoading(true);
    try {
      const next = await nextQuestion(gameId);
      setQuestion(next);
    } catch (err) {
      const message = err instanceof Error ? err.message : "Error al cargar la siguiente pregunta.";
      setError(message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="screen game-screen">
      <div className="game-layout">
        <div className="game-header">
          <p className="game-difficulty-pill">{titlePill}</p>
          {question && (
            <p className="game-progress-pill">
              Pregunta {question.numberQuestion}/{TOTAL_QUESTIONS}
            </p>
          )}
        </div>

        {isLoading && <p className="helper-text">Cargando...</p>}
        {error && <p className="error-text">{error}</p>}

        {!isLoading && !question && !error && (
          <p className="helper-text">No hay pregunta disponible.</p>
        )}

        {!isLoading && question && (
          <>
            {gameMessage && question.numberQuestion === 1 && !wolfResponse && (
              <p className="wolf-message" role="status" aria-live="polite">
                {gameMessage}
              </p>
            )}

            {question.intro && <p className="game-intro">{question.intro}</p>}

            <h2 className="question-text">{question.text}</h2>

            {wolfResponse && (
              <p
                className={
                  wolfResponse.correct
                    ? "wolf-message wolf-message--correct"
                    : "wolf-message wolf-message--wrong"
                }
                role="status"
                aria-live="polite"
              >
                {wolfResponse.wolfMessage}
              </p>
            )}

            <div className="answers-container" aria-label="Respuestas">
              {question.answers.map((ans, idx) => {
                const isSelected = selectedAnswerIndex === idx;
                const isDisabled = isSubmitting || wolfResponse != null;
                const buttonClassName = isSelected
                  ? "dungeon-btn answer-btn answer-btn--selected"
                  : "dungeon-btn answer-btn";

                return (
                  <button
                    key={`${question.numberQuestion}-${idx}`}
                    className={buttonClassName}
                    type="button"
                    onClick={() => submitAnswer(idx)}
                    disabled={isDisabled}
                    aria-pressed={isSelected}
                  >
                    {ans}
                  </button>
                );
              })}
            </div>

            {wolfResponse && (
              <button className="dungeon-btn" onClick={goNext} type="button" disabled={isLoading}>
                {question.numberQuestion >= TOTAL_QUESTIONS ? "Ver resultado" : "Siguiente"}
              </button>
            )}
          </>
        )}

        {onBack && (
          <button className="secondary-button" type="button" onClick={onBack} disabled={isLoading}>
            Volver
          </button>
        )}
      </div>
    </div>
  );
}

export default Game;
