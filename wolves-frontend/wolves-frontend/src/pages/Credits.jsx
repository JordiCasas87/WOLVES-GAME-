import { useEffect, useState } from "react";

const creditsVideoUrl = new URL("../assets/animaciones/creditsVideo.mp4", import.meta.url).href;

function Credits({ onBack }) {
  const [showText, setShowText] = useState(false);

  useEffect(() => {
    const onKeyDown = (e) => {
      if (e.key === "Escape") onBack?.();
    };

    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [onBack]);

  useEffect(() => {
    const id = window.setTimeout(() => setShowText(true), 2000);
    return () => window.clearTimeout(id);
  }, []);

  return (
    <div className="screen login-screen credits-page">
      <div className="login-bg" aria-hidden="true">
        <video
          className="login-bg-video"
          src={creditsVideoUrl}
          autoPlay
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

      <div className="credits-content">
        <div
          className={showText ? "credits-text-card credits-text-card--visible" : "credits-text-card"}
          role="article"
          aria-label="Créditos del proyecto"
        >
          <p className="credits-lead">
            Proyecto académico desarrollado en 2026
            <br />
            en el marco de la IT Academy
          </p>

          <p className="credits-body">
            Diseño, desarrollo backend, lógica de juego,
            <br />
            arquitectura de APIs REST, seguridad
            <br />
            y supervivencia frente al lobo:
          </p>

          <p className="credits-author">Jordi Casas</p>

          <p className="credits-warn-title">Este juego no garantiza:</p>
          <ul className="credits-list">
            <li>La contratación</li>
            <li>La supervivencia</li>
            <li>Ni salir ileso de la entrevista</li>
          </ul>
        </div>

        <button className="credits-close" type="button" onClick={onBack}>
          Volver
        </button>
      </div>
    </div>
  );
}

export default Credits;
