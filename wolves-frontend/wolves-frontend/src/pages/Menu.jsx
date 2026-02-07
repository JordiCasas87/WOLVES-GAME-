import { useEffect, useState } from "react";
import { apiRequest, clearToken } from "../services/api";

const flameGifUrl = new URL("../assets/animaciones/llama.gif", import.meta.url).href;
const wolfGreetingVideoUrl = new URL(
  "../assets/animaciones/lobo saludo.mp4",
  import.meta.url
).href;

function Menu({ onNewGame, onMistakesGame, onStats, onAdmin, onBackToLogin }) {
  const [showDifficulty, setShowDifficulty] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);
  const [meChecked, setMeChecked] = useState(false);

	  const replayGreetingVideo = (video) => {
	    video.muted = false;
	    video.volume = 0.9;
	    video.currentTime = 0;
	    const playAttempt = video.play();
	    if (playAttempt && typeof playAttempt.catch === "function") playAttempt.catch(() => {});
	  };

  useEffect(() => {
    if (!showDifficulty) return undefined;

    const onKeyDown = (e) => {
      if (e.key === "Escape") setShowDifficulty(false);
    };

    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [showDifficulty]);

  useEffect(() => {
    let isActive = true;

    async function loadMe() {
      try {
        const me = await apiRequest("/me");
        if (!isActive) return;
        setIsAdmin(String(me?.role ?? "").toUpperCase() === "ADMIN");
      } catch {
        if (!isActive) return;
        setIsAdmin(false);
      } finally {
        if (isActive) setMeChecked(true);
      }
    }

    loadMe();
    return () => {
      isActive = false;
    };
  }, []);

  const startNewGame = (difficulty) => {
    setShowDifficulty(false);
    onNewGame(difficulty);
  };

  return (
    <div className="screen menu-screen menu-screen--softblur">
      <div className="menu-stack">
        <div className="menu-video-wrap">
          <video
            className="menu-greeting-video"
            src={wolfGreetingVideoUrl}
            autoPlay
            muted
            playsInline
            preload="auto"
            role="button"
            tabIndex={0}
            aria-label="Reproducir saludo del lobo"
            onClick={(e) => replayGreetingVideo(e.currentTarget)}
            onKeyDown={(e) => {
              if (e.key === "Enter" || e.key === " ") {
                e.preventDefault();
                replayGreetingVideo(e.currentTarget);
              }
            }}
            onEnded={(e) => {
              const video = e.currentTarget;
              const duration = video.duration;
              if (!Number.isFinite(duration) || duration <= 0) return;
              video.pause();
              video.currentTime = Math.max(0, duration - 0.05);
            }}
          />
        </div>

        <div className="menu-layout menu-layout--floating">
          <button className="dungeon-btn" onClick={() => setShowDifficulty(true)} type="button">
            Nueva entrevista
          </button>

          <button className="dungeon-btn" onClick={onMistakesGame}>
            Entrevista de repaso
          </button>

          <button className="dungeon-btn" onClick={onStats} type="button">
            Estatus y ranking
          </button>

          <button
            className="dungeon-btn"
            onClick={() => {
              clearToken();
              onBackToLogin();
            }}
            type="button"
          >
            Volver al login
          </button>

          {meChecked && isAdmin && onAdmin && (
            <button className="dungeon-btn menu-admin-btn" onClick={onAdmin} type="button">
              Administración
            </button>
          )}
        </div>
      </div>

      {showDifficulty && (
        <div
          className="menu-modal-backdrop"
          onMouseDown={(e) => {
            if (e.target === e.currentTarget) setShowDifficulty(false);
          }}
          role="presentation"
        >
          <div className="menu-modal-shell">
            <img
              className="menu-side-flame menu-side-flame--left"
              src={flameGifUrl}
              alt=""
              aria-hidden="true"
            />

            <div className="menu-modal" role="dialog" aria-modal="true" aria-label="Dificultad">
              <h2 className="menu-modal-title">Elige dificultad</h2>

              <div className="menu-modal-actions">
                <button
                  className="dungeon-btn"
                  onClick={() => startNewGame("easy")}
                  type="button"
                >
                  Easy
                </button>
                <button
                  className="dungeon-btn"
                  onClick={() => startNewGame("medium")}
                  type="button"
                >
                  Medium
                </button>
                <button
                  className="dungeon-btn"
                  onClick={() => startNewGame("hard")}
                  type="button"
                >
                  Hard
                </button>

                <button
                  className="dungeon-btn"
                  onClick={() => setShowDifficulty(false)}
                  type="button"
                >
                  Cancelar
                </button>
              </div>
            </div>

            <img
              className="menu-side-flame menu-side-flame--right"
              src={flameGifUrl}
              alt=""
              aria-hidden="true"
            />
          </div>
        </div>
      )}
    </div>
  );
}

export default Menu;
