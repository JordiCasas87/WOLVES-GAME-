import { useEffect, useRef, useState } from "react";
import { login, register } from "../services/auth";

const logoVideoUrl = new URL(
  "../assets/videos/logovideo.mov",
  import.meta.url,
).href;

const cabraAnimationUrl = new URL(
  "../assets/animaciones/cabraAnimation.mov",
  import.meta.url,
).href;

const catAnimationUrl = new URL(
  "../assets/animaciones/catAnimation.mov",
  import.meta.url,
).href;

const conejoAnimationUrl = new URL(
  "../assets/animaciones/ConejoAnimation.mov",
  import.meta.url,
).href;

const hamsterAnimationUrl = new URL(
  "../assets/animaciones/HamsterAnimation.mov",
  import.meta.url,
).href;

const ovejaAnimationUrl = new URL(
  "../assets/animaciones/ovejaAnimation.mp4",
  import.meta.url,
).href;

const CHARACTER_LABELS = {
  cat: "Cat Roller",
  oveja: "Hack Sheep",
  cabra: "Mr. Goat",
  hamster: "Hamstress",
  conejo: "Rabitter",
};

const CHARACTER_ORDER = ["cat", "oveja", "cabra", "hamster", "conejo"];

const portraits = import.meta.glob("../assets/images/fotosPlayersCarnet/*.{png,jpg,jpeg,webp}", {
  eager: true,
  import: "default",
});

const cvs = import.meta.glob("../assets/images/CV/*.{png,jpg,jpeg,webp}", {
  eager: true,
  import: "default",
});

function basenameNoExt(path) {
  const file = path.split("/").pop() ?? path;
  const dot = file.lastIndexOf(".");
  return (dot >= 0 ? file.slice(0, dot) : file).trim();
}

function normalizeKey(name) {
  return name
    .trim()
    .toLowerCase()
    .replace(/\s+/g, "")
    .replace(/cv$/i, "");
}

const portraitsByKey = Object.fromEntries(
  Object.entries(portraits).map(([path, url]) => [normalizeKey(basenameNoExt(path)), url]),
);

const cvByKey = Object.fromEntries(
  Object.entries(cvs).map(([path, url]) => [normalizeKey(basenameNoExt(path)), url]),
);

const characters = CHARACTER_ORDER.filter(
  (key) => portraitsByKey[key] && cvByKey[key],
).map((key) => ({
  key,
  name: CHARACTER_LABELS[key] ?? key,
  portraitSrc: portraitsByKey[key],
  cvBgSrc: cvByKey[key],
}));

function wrapOffset(index, activeIndex, length) {
  let diff = index - activeIndex;
  const half = Math.floor(length / 2);
  if (diff > half) diff -= length;
  if (diff < -half) diff += length;
  return diff;
}

function Login({ onLoginSuccess }) {
  const [mode, setMode] = useState("choose"); // choose | login | character | signup
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [age, setAge] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [selectedCharacterKey, setSelectedCharacterKey] = useState(null);
  const [carouselIndex, setCarouselIndex] = useState(0);
  const dragStartXRef = useRef(null);
  const [isCoverflowPaused, setIsCoverflowPaused] = useState(false);
  const resumeTimeoutRef = useRef(null);
  const characterCount = characters.length;

  const clearResumeTimeout = () => {
    if (resumeTimeoutRef.current) {
      window.clearTimeout(resumeTimeoutRef.current);
      resumeTimeoutRef.current = null;
    }
  };

  const pauseCoverflowFor = (ms) => {
    setIsCoverflowPaused(true);
    clearResumeTimeout();
    resumeTimeoutRef.current = window.setTimeout(() => {
      setIsCoverflowPaused(false);
      resumeTimeoutRef.current = null;
    }, ms);
  };

  const resetForm = () => {
    setUsername("");
    setPassword("");
    setConfirmPassword("");
    setAge("");
    setError("");
  };

  const goChoose = () => {
    resetForm();
    setSelectedCharacterKey(null);
    setMode("choose");
  };

  const selectedCharacter =
    (selectedCharacterKey && characters.find((c) => c.key === selectedCharacterKey)) || null;

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");

    if (!username.trim() || !password.trim()) {
      setError("Rellena usuario y contrasena.");
      return;
    }

    try {
      setIsSubmitting(true);
      await login({ name: username.trim(), password });
      onLoginSuccess();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error al iniciar sesion.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleSignup = async (e) => {
    e.preventDefault();
    setError("");

    if (!selectedCharacter) {
      setError("Selecciona un personaje antes de registrarte.");
      return;
    }

    if (!username.trim() || !password.trim() || !confirmPassword.trim() || !age.trim()) {
      setError("Rellena todos los campos.");
      return;
    }

    const ageNumber = Number(age);
    if (!Number.isFinite(ageNumber) || ageNumber < 1) {
      setError("Edad no valida.");
      return;
    }

    if (password !== confirmPassword) {
      setError("Las contrasenas no coinciden.");
      return;
    }

    try {
      setIsSubmitting(true);
      await register({ name: username.trim(), password, age: ageNumber });
      onLoginSuccess();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error al crear el usuario.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const goPrevCharacter = () => {
    if (characterCount === 0) return;
    setCarouselIndex((i) => (i - 1 + characterCount) % characterCount);
  };

  const goNextCharacter = () => {
    if (characterCount === 0) return;
    setCarouselIndex((i) => (i + 1) % characterCount);
  };

  useEffect(() => () => clearResumeTimeout(), []);

  useEffect(() => {
    if (mode !== "character") return undefined;
    if (characterCount <= 1) return undefined;
    if (isCoverflowPaused) return undefined;

    // Respect OS accessibility preference.
    const mq = window.matchMedia?.("(prefers-reduced-motion: reduce)");
    if (mq?.matches) return undefined;

    const id = window.setInterval(() => {
      setCarouselIndex((i) => (i + 1) % characterCount);
    }, 3000);

    return () => window.clearInterval(id);
  }, [mode, isCoverflowPaused, characterCount]);

  return (
    <div className="screen login-screen">
      {mode !== "character" && mode !== "signup" && (
        <div className="auth-layout">
          <video
            className="logo-video"
            src={logoVideoUrl}
            autoPlay
            loop
            muted
            playsInline
            preload="auto"
            aria-hidden="true"
          >
            Tu navegador no soporta el video.
          </video>

          {mode === "choose" && (
            <>
              <p className="helper-text">Elige tu destino.</p>

              <button
                className="dungeon-btn"
                onClick={() => {
                  resetForm();
                  setMode("login");
                }}
                type="button"
                disabled={isSubmitting}
              >
                Iniciar sesion
              </button>

              <button
                className="dungeon-btn"
                onClick={() => {
                  resetForm();
                  setSelectedCharacterKey(null);
                  const conejoIndex = characters.findIndex((c) => c.key === "conejo");
                  setCarouselIndex(conejoIndex >= 0 ? conejoIndex : 0);
                  setMode("character");
                }}
                type="button"
                disabled={isSubmitting}
              >
                Crear usuario
              </button>
            </>
          )}

          {mode === "login" && (
            <form className="auth-form" onSubmit={handleLogin}>
              <h2 className="auth-title">Iniciar sesion</h2>

              <input
                className="text-input"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Usuario"
                autoComplete="username"
                disabled={isSubmitting}
              />

              <input
                className="text-input"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Contrasena"
                type="password"
                autoComplete="current-password"
                disabled={isSubmitting}
              />

              {error && <p className="error-text">{error}</p>}

              <button className="dungeon-btn" type="submit" disabled={isSubmitting}>
                {isSubmitting ? "Entrando..." : "Entrar"}
              </button>

              <button
                className="secondary-button"
                type="button"
                onClick={goChoose}
                disabled={isSubmitting}
              >
                Volver
              </button>
            </form>
          )}
        </div>
      )}

      {mode === "character" && (
        <div className="character-layout">
          <h2 className="auth-title character-title">Elige tu developer</h2>

          {characters.length === 0 ? (
            <p className="error-text">
              No se han encontrado las imagenes de personajes/CV. Revisa{" "}
              <code>src/assets/images/fotosPlayersCarnet</code> y{" "}
              <code>src/assets/images/CV</code>.
            </p>
          ) : (
            <>
              <div className="character-carousel" aria-label="Selector de personaje">
                <button className="carousel-arrow" type="button" onClick={goPrevCharacter}>
                  {"<"}
                </button>

                <div
                  className="coverflow-stage"
                  role="group"
                  aria-label="Fotos de personajes"
                  tabIndex={0}
                  // Keep it moving unless the user is actively interacting (dragging/focus).
                  onFocus={() => {
                    clearResumeTimeout();
                    setIsCoverflowPaused(true);
                  }}
                  onBlur={() => pauseCoverflowFor(350)}
                  onKeyDown={(e) => {
                    if (e.key === "ArrowLeft") goPrevCharacter();
                    if (e.key === "ArrowRight") goNextCharacter();
                  }}
                  onPointerDown={(e) => {
                    dragStartXRef.current = e.clientX;
                    clearResumeTimeout();
                    setIsCoverflowPaused(true);
                  }}
                  onPointerUp={(e) => {
                    const startX = dragStartXRef.current;
                    dragStartXRef.current = null;
                    if (startX == null) return;
                    const dx = e.clientX - startX;
                    if (Math.abs(dx) < 35) {
                      pauseCoverflowFor(350);
                      return;
                    }

                    if (dx > 0) goPrevCharacter();
                    else goNextCharacter();

                    pauseCoverflowFor(900);
                  }}
                  onPointerCancel={() => {
                    dragStartXRef.current = null;
                    pauseCoverflowFor(350);
                  }}
                >
                  <div className="coverflow">
                    {characters.map((c, idx) => {
                      const offset = wrapOffset(idx, carouselIndex, characters.length);
                      const abs = Math.abs(offset);
                      const zIndex = 10 - abs;

                      return (
                        <button
                          key={c.key}
                          type="button"
                          className={
                            offset === 0
                              ? "coverflow-card coverflow-card-active"
                              : "coverflow-card"
                          }
                          style={{
                            "--offset": offset,
                            "--abs": abs,
                            zIndex,
                          }}
                          onClick={() => {
                            setCarouselIndex(idx);
                            pauseCoverflowFor(900);
                          }}
                          title={c.name}
                          tabIndex={-1}
                        >
                          {c.key === "cabra" ? (
                            <video
                              className="coverflow-img"
                              src={cabraAnimationUrl}
                              autoPlay
                              loop
                              muted
                              playsInline
                              preload="metadata"
                              aria-label={c.name}
                            />
                          ) : c.key === "cat" ? (
                            <video
                              className="coverflow-img"
                              src={catAnimationUrl}
                              autoPlay
                              loop
                              muted
                              playsInline
                              preload="metadata"
                              aria-label={c.name}
                            />
                          ) : c.key === "conejo" ? (
                            <video
                              className="coverflow-img"
                              src={conejoAnimationUrl}
                              autoPlay
                              loop
                              muted
                              playsInline
                              preload="metadata"
                              aria-label={c.name}
                            />
                          ) : c.key === "hamster" ? (
                            <video
                              className="coverflow-img"
                              src={hamsterAnimationUrl}
                              autoPlay
                              loop
                              muted
                              playsInline
                              preload="metadata"
                              aria-label={c.name}
                            />
                          ) : c.key === "oveja" ? (
                            <video
                              className="coverflow-img"
                              src={ovejaAnimationUrl}
                              autoPlay
                              loop
                              muted
                              playsInline
                              preload="metadata"
                              aria-label={c.name}
                            />
                          ) : (
                            <img className="coverflow-img" src={c.portraitSrc} alt={c.name} />
                          )}
                        </button>
                      );
                    })}
                  </div>
                </div>

                <button className="carousel-arrow" type="button" onClick={goNextCharacter}>
                  {">"}
                </button>
              </div>

              <p className="character-name character-name-pill">
                {characters[carouselIndex].name}
              </p>

              <div className="carousel-dots" aria-hidden="true">
                {characters.map((c, idx) => (
                  <span
                    key={c.key}
                    className={idx === carouselIndex ? "dot dot-active" : "dot"}
                    title={c.name}
                  />
                ))}
              </div>

              <button
                className="character-select-btn"
                type="button"
                onClick={() => {
                  setSelectedCharacterKey(characters[carouselIndex].key);
                  setMode("signup");
                }}
              >
                Seleccionar
              </button>

              <button className="character-select-btn" type="button" onClick={goChoose}>
                Cancelar
              </button>
            </>
          )}
        </div>
      )}

      {mode === "signup" && (
        <div className="signup-layout">
          {selectedCharacter && (
            <img
              className={
                selectedCharacter.key === "hamster"
                  ? "signup-cv signup-cv--hamster"
                  : "signup-cv"
              }
              src={selectedCharacter.cvBgSrc}
              alt=""
              aria-hidden="true"
            />
          )}

          <div className="signup-overlay">
            <form className="auth-form signup-form" onSubmit={handleSignup}>
            <h2 className="auth-title">Completa tu CV</h2>

            {selectedCharacter && (
              <p className="signup-subtitle">Personaje: {selectedCharacter.name}</p>
            )}

            <div className="signup-fields">
              <input
                className="text-input"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Usuario"
                autoComplete="username"
                disabled={isSubmitting}
              />

              <input
                className="text-input"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Contrasena"
                type="password"
                autoComplete="new-password"
                disabled={isSubmitting}
              />

              <input
                className="text-input"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="Repite contrasena"
                type="password"
                autoComplete="new-password"
                disabled={isSubmitting}
              />

              <input
                className="text-input"
                value={age}
                onChange={(e) => setAge(e.target.value)}
                placeholder="Edad"
                type="number"
                min="1"
                inputMode="numeric"
                disabled={isSubmitting}
              />

              {error && <p className="error-text">{error}</p>}
            </div>

            <button className="signup-btn" type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Creando..." : "Crear y entrar"}
            </button>

              <button
                className="signup-btn"
                type="button"
                onClick={goChoose}
                disabled={isSubmitting}
              >
                Cancelar
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Login;
