import { useEffect, useRef, useState } from "react";

const travelingVideoUrl = new URL(
  "../assets/animaciones/travel1.mp4",
  import.meta.url,
).href;

function GameInfo({ onBack }) {
  const videoRef = useRef(null);
  const [showUi, setShowUi] = useState(true);

  useEffect(() => {
    const onKeyDown = (e) => {
      if (e.key === "Escape") onBack?.();
    };

    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [onBack]);

  useEffect(() => {
    const video = videoRef.current;
    if (!video) return undefined;

    const start = () => {
      video.playbackRate = 1.2;
      try {
        video.currentTime = 0;
      } catch {
        // Some browsers may block seeks before metadata is ready.
      }
      const attempt = video.play();
      if (attempt && typeof attempt.catch === "function") attempt.catch(() => {});
    };

    if (video.readyState >= 1) start();
    else video.addEventListener("loadedmetadata", start, { once: true });

    return () => {
      video.removeEventListener("loadedmetadata", start);
    };
  }, []);

  return (
    <div className="screen login-screen gameinfo-page">
      <div className="login-bg" aria-hidden="true">
        <video
          ref={videoRef}
          className="login-bg-video"
          src={travelingVideoUrl}
          muted
          playsInline
          preload="auto"
          onEnded={(e) => {
            const video = e.currentTarget;
            const duration = video.duration;
            if (!Number.isFinite(duration) || duration <= 0) return;
            video.pause();
            video.currentTime = Math.max(0, duration - 0.05);
          }}
        >
          Tu navegador no soporta el video.
        </video>
      </div>

      {showUi && (
        <div className="gameinfo-content">
          <div className="gameinfo-text-card" role="article" aria-label="Información del juego">
            <h3 className="gameinfo-heading">En WOLVES – The Interview Game</h3>

            <p className="gameinfo-text">
              Cada partida representa una entrevista técnica donde solo hay una regla: responder o
              ser juzgado.
            </p>

            <p className="gameinfo-text gameinfo-text--em">
              Diez preguntas, una única respuesta correcta en cada una
            </p>

            <p className="gameinfo-text">
              Para superar la entrevista deberás acertar al menos seis preguntas; de lo contrario,
              recibirás un amable “ya te llamaremos”.
            </p>

            <p className="gameinfo-text">
              Si fallas, el sistema lo recordará, y esas preguntas volverán en las entrevistas de
              repaso, donde no hay recompensas, solo memoria y aprendizaje. El lobo observa, el
              tiempo avanza y la entrevista no se detiene…
            </p>
          </div>
          <button className="credits-close" type="button" onClick={onBack}>
            Volver
          </button>
        </div>
      )}
    </div>
  );
}

export default GameInfo;
