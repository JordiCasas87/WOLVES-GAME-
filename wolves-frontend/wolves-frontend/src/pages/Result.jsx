import { useMemo, useState } from "react";

const wolfWinVideoUrl = new URL("../assets/animaciones/loboGanas.mp4", import.meta.url).href;
const wolfLoseVideoUrl = new URL("../assets/animaciones/loboPierdes.mp4", import.meta.url).href;

function Result({ onRestart, result, mode = "new" }) {
  const [isSoundEnabled, setIsSoundEnabled] = useState(false);

  const videoSrc = useMemo(() => {
    if (!result) return "";
    return result.passed ? wolfWinVideoUrl : wolfLoseVideoUrl;
  }, [result]);

  const rewardLabel = useMemo(() => {
    if (mode === "mistakes") return "Sin recompensa (repaso)";
    return result?.passed ? "Recompensa" : "Recompensa perdida";
  }, [mode, result]);

  const rewardValue = result?.reward;

  const playWithSound = (video) => {
    video.muted = false;
    video.volume = 0.8;
    video.currentTime = 0;
    const attempt = video.play();
    if (attempt && typeof attempt.catch === "function") attempt.catch(() => {});
    setIsSoundEnabled(true);
  };

  return (
    <div className="screen menu-screen">
      <div className="menu-layout stats-layout">
        <h2 className="stats-title">Resultado</h2>

        {!result ? (
          <p className="helper-text">Sin resultado.</p>
        ) : (
          <>
            <div className="result-video-wrap">
              <video
                className="result-video"
                src={videoSrc}
                autoPlay
                muted={!isSoundEnabled}
                playsInline
                preload="auto"
                role="button"
                tabIndex={0}
                aria-label="Reproducir vídeo del resultado con sonido"
                onLoadedMetadata={(e) => {
                  e.currentTarget.volume = 0.8;
                }}
                onClick={(e) => playWithSound(e.currentTarget)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" || e.key === " ") {
                    e.preventDefault();
                    playWithSound(e.currentTarget);
                  }
                }}
              />

              {!isSoundEnabled && (
                <p className="result-video-hint" aria-hidden="true">
                  Clic para activar sonido
                </p>
              )}
            </div>

            <section className="stats-card" aria-label="Resumen de la partida">
              <div className="stats-row">
                <span>Puntuación</span>
                <strong>
                  {result.score ?? 0}/{result.totalQuestions ?? 10}
                </strong>
              </div>
              <div className="stats-row">
                <span>Estado</span>
                <strong>{result.passed ? "APROBADO" : "SUSPENSO"}</strong>
              </div>
              <div className="stats-row">
                <span>{rewardLabel}</span>
                <strong>{rewardValue == null ? "—" : rewardValue}</strong>
              </div>
            </section>

            {result.finalMessage && <p className="helper-text">{result.finalMessage}</p>}
          </>
        )}

        <div className="stats-actions">
          <button className="dungeon-btn" onClick={onRestart} type="button">
            Volver al menú
          </button>
        </div>
      </div>
    </div>
  );
}

export default Result;
