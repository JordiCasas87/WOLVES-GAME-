import { useEffect, useMemo, useRef, useState } from "react";
import {
  answerQuestion,
  createGame,
  createMistakesGame,
  getResult,
  nextQuestion,
} from "../services/game";

const TOTAL_QUESTIONS = 10;
const loboDanceUrl = new URL("../assets/animaciones/lobodance.mp4", import.meta.url).href;

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
  const [loboDancePhase, setLoboDancePhase] = useState(() =>
    mode === "mistakes" ? "off" : "enter",
  ); // enter | center | exit | off
  const loboDanceRef = useRef(null);

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
      setLoboDancePhase(mode === "mistakes" ? "off" : "enter");

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

  useEffect(() => {
    // Prefer respecting OS accessibility preference.
    const mq = window.matchMedia?.("(prefers-reduced-motion: reduce)");
    if (mq?.matches) {
      setLoboDancePhase("off");
      return undefined;
    }

    if (mode === "mistakes") {
      setLoboDancePhase("off");
      return undefined;
    }

    return undefined;
  }, [mode]);

  useEffect(() => {
    const video = loboDanceRef.current;
    if (!video) return undefined;

    if (loboDancePhase === "off") {
      video.pause?.();
      return undefined;
    }

    if (loboDancePhase === "enter") {
      try {
        video.pause?.();
        video.loop = false;
        video.currentTime = 0;
      } catch {
        // ignore
      }
      return undefined;
    }

    if (loboDancePhase === "exit") {
      video.pause?.();
      video.loop = false;
      return undefined;
    }

    if (loboDancePhase !== "center") return undefined;

    let fallbackId = null;

    const start = async () => {
      video.playbackRate = 1;
      video.loop = false;

      try {
        video.currentTime = 0;
      } catch {
        // ignore
      }

      const attempt = video.play?.();
      if (attempt && typeof attempt.catch === "function") attempt.catch(() => {});

      const duration = video.duration;
      if (Number.isFinite(duration) && duration > 0) {
        fallbackId = window.setTimeout(() => setLoboDancePhase("exit"), duration * 1000 + 750);
      }
    };

    if (video.readyState >= 1) start();
    else video.addEventListener("loadedmetadata", start, { once: true });

    return () => {
      if (fallbackId) window.clearTimeout(fallbackId);
      video.removeEventListener("loadedmetadata", start);
    };
  }, [loboDancePhase]);

  const dismissLoboDance = () => {
    if (loboDancePhase === "off") return;

    const video = loboDanceRef.current;
    video?.pause?.();

    // Avoid jarring jumps if the user dismisses mid-enter/mid-exit.
    if (loboDancePhase === "center") setLoboDancePhase("exit");
    else setLoboDancePhase("off");
  };

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
      {loboDancePhase !== "off" && (
        <video
          ref={loboDanceRef}
          className={`lobo-dance-overlay lobo-dance-overlay--${loboDancePhase}`}
          src={loboDanceUrl}
          loop={false}
          muted
          playsInline
          preload="auto"
          role="button"
          tabIndex={0}
          aria-label="Cerrar animación del lobo"
          onEnded={() => setLoboDancePhase("exit")}
          onError={() => setLoboDancePhase("off")}
          onClick={dismissLoboDance}
          onKeyDown={(e) => {
            if (e.key === "Enter" || e.key === " " || e.key === "Escape") {
              e.preventDefault();
              dismissLoboDance();
            }
          }}
          onAnimationEnd={() => {
            if (loboDancePhase === "enter") setLoboDancePhase("center");
            if (loboDancePhase === "exit") setLoboDancePhase("off");
          }}
        />
      )}

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
            <div
              className={
                wolfResponse
                  ? wolfResponse.correct
                    ? "question-card question-card--correct"
                    : "question-card question-card--wrong"
                  : "question-card"
              }
            >
              {wolfResponse ? (
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
              ) : gameMessage && question.numberQuestion === 1 ? (
                <p className="wolf-message" role="status" aria-live="polite">
                  {gameMessage}
                </p>
              ) : null}

              {question.intro && <p className="game-intro">{question.intro}</p>}

              <h2 className="question-text">{question.text}</h2>
            </div>

            <div className="answers-container" aria-label="Respuestas">
              {question.answers.map((ans, idx) => {
                const isSelected = selectedAnswerIndex === idx;
                const isDisabled = isSubmitting || wolfResponse != null;
                const resultClassName =
                  wolfResponse && isSelected
                    ? wolfResponse.correct
                      ? "answer-btn--correct"
                      : "answer-btn--wrong"
                    : "";
                const buttonClassName = isSelected
                  ? `dungeon-btn answer-btn answer-btn--selected ${resultClassName}`.trim()
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
            Ataque de ansiedad
            <br />
            salir corriendo
          </button>
        )}
      </div>
    </div>
  );
}

export default Game;
