import { useEffect, useState } from "react";
import { clearToken } from "../services/api";

const flameGifUrl = new URL("../assets/animaciones/llama.gif", import.meta.url).href;

function Menu({ onNewGame, onMistakesGame, onStats, onBackToLogin }) {
  const [showDifficulty, setShowDifficulty] = useState(false);

  useEffect(() => {
    if (!showDifficulty) return undefined;

    const onKeyDown = (e) => {
      if (e.key === "Escape") setShowDifficulty(false);
    };

    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [showDifficulty]);

  const startNewGame = (difficulty) => {
    setShowDifficulty(false);
    onNewGame(difficulty);
  };

  return (
    <div className="screen menu-screen">
      <div className="menu-layout">
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
