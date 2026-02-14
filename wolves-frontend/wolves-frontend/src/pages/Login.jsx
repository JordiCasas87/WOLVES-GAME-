import { useEffect, useRef, useState } from "react";
import { login, register } from "../services/auth";

const logoVideoUrl = new URL(
  "../assets/videos/videoLogofinal.mov",
  import.meta.url,
).href;

const fondoEntradaVideoUrl = new URL(
  "../assets/animaciones/fondoEntrada.mp4",
  import.meta.url,
).href;

const moneySfxUrl = new URL("../assets/sounds/money.mp3", import.meta.url).href;

const flyingYenGifUrl = new URL(
  "../assets/animaciones/flying-yen.gif",
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

function Login({ onLoginSuccess, onCredits, onGameInfo }) {
  const [showUi, setShowUi] = useState(false);
  const [geldMoneySprites, setGeldMoneySprites] = useState([]);
  const geldMoneyNextIdRef = useRef(1);
  const moneySfxRef = useRef(null);
  const [moneyScore, setMoneyScore] = useState(0);
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
  const hasRevealedUiRef = useRef(false);
  const characterCount = characters.length;
  const isYenMiniGameEnabled = mode === "choose" || mode === "login";

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

    if (password.length < 6) {
      setError("La contrasena debe tener al menos 6 caracteres.");
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

    if (password.length < 6) {
      setError("La contrasena debe tener al menos 6 caracteres.");
      return;
    }

    try {
      setIsSubmitting(true);
      await register({ name: username.trim(), password, age: ageNumber });
      onLoginSuccess();
    } catch (err) {
      const status = err && typeof err === "object" ? err.status : null;
      if (status === 409) {
        setError("Ese nombre de usuario ya existe.");
        return;
      }
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

  useEffect(() => {
    const mq = window.matchMedia?.("(prefers-reduced-motion: reduce)");
    if (mq?.matches) {
      setShowUi(true);
      return undefined;
    }

    const id = window.setTimeout(() => setShowUi(true), 2000);
    return () => window.clearTimeout(id);
  }, []);

  useEffect(() => {
    if (!showUi) return;
    hasRevealedUiRef.current = true;
  }, [showUi]);

  useEffect(() => () => clearResumeTimeout(), []);

  useEffect(() => {
    if (!isYenMiniGameEnabled) {
      setGeldMoneySprites([]);
      setMoneyScore(0);
      return undefined;
    }

    const mq = window.matchMedia?.("(prefers-reduced-motion: reduce)");
    if (mq?.matches) return undefined;

    const rand = (min, max) => min + Math.random() * (max - min);
    const randInt = (min, max) => Math.floor(rand(min, max + 1));
    const angleDeg = (dxPx, dyPx) => (Math.atan2(dyPx, dxPx) * 180) / Math.PI;

    const spawn = () => {
      const offLeft = -25;
      const offRight = 125;
      const offTop = -25;
      const offBottom = 125;

      const y1 = randInt(8, 92);
      const y2 = randInt(8, 92);
      const x1 = randInt(5, 95);
      const x2 = randInt(5, 95);
      const pattern = randInt(0, 7);

      let fromXvw;
      let fromYvh;
      let toXvw;
      let toYvh;

      switch (pattern) {
        case 0:
          fromXvw = offLeft;
          toXvw = offRight;
          fromYvh = y1;
          toYvh = y1;
          break;
        case 1:
          fromXvw = offRight;
          toXvw = offLeft;
          fromYvh = y1;
          toYvh = y1;
          break;
        case 2:
          fromXvw = x1;
          toXvw = x1;
          fromYvh = offTop;
          toYvh = offBottom;
          break;
        case 3:
          fromXvw = x1;
          toXvw = x1;
          fromYvh = offBottom;
          toYvh = offTop;
          break;
        case 4:
          fromXvw = offLeft;
          toXvw = offRight;
          fromYvh = y1;
          toYvh = y2;
          break;
        case 5:
          fromXvw = offRight;
          toXvw = offLeft;
          fromYvh = y1;
          toYvh = y2;
          break;
        case 6:
          fromXvw = x1;
          toXvw = x2;
          fromYvh = offTop;
          toYvh = offBottom;
          break;
        case 7:
        default:
          fromXvw = x1;
          toXvw = x2;
          fromYvh = offBottom;
          toYvh = offTop;
          break;
      }

      const dxPx = ((toXvw - fromXvw) / 100) * window.innerWidth;
      const dyPx = ((toYvh - fromYvh) / 100) * window.innerHeight;
      let rotate = angleDeg(dxPx, dyPx);
      let flipX = 1;

      // Sprite faces "forward" to the right by default.
      // Keep rotation within [-90, 90] and flip horizontally for left-facing motion.
      if (rotate > 90 || rotate < -90) {
        flipX = -1;
        rotate = rotate > 0 ? rotate - 180 : rotate + 180;
      }

      const id = geldMoneyNextIdRef.current++;
      const durationMs = Math.round(rand(6500, 12000));
      const scale = Number(rand(0.9, 1.08).toFixed(2));

      setGeldMoneySprites((prev) => {
        if (prev.length >= 4) return prev;
        return [
          ...prev,
          {
            id,
            fromX: `${fromXvw}vw`,
            fromY: `${fromYvh}vh`,
            toX: `${toXvw}vw`,
            toY: `${toYvh}vh`,
            durationMs,
            scale,
            rotate: Number(rotate.toFixed(1)),
            flipX,
          },
        ];
      });
    };

    // Seed one so it feels alive immediately.
    spawn();

    const interval = window.setInterval(spawn, 3800);
    return () => window.clearInterval(interval);
  }, [isYenMiniGameEnabled]);

  const removeGeldMoneySprite = (id) => {
    setGeldMoneySprites((prev) => prev.filter((sprite) => sprite.id !== id));
  };

  const playMoneySfx = () => {
    const audio = moneySfxRef.current;
    if (!audio) return;
    try {
      audio.currentTime = 0;
    } catch {
      // Some browsers may block seeks before metadata is ready.
    }
    const attempt = audio.play();
    if (attempt && typeof attempt.catch === "function") attempt.catch(() => {});
  };

  const captureGeldMoneySprite = (id) => {
    playMoneySfx();
    setMoneyScore((prev) => prev + 10);
    removeGeldMoneySprite(id);
  };

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

  const uiEnterClass = showUi && !hasRevealedUiRef.current ? " login-ui-enter" : "";

  return (
    <div className="screen login-screen screen--footer-reserve">
      <audio ref={moneySfxRef} src={moneySfxUrl} preload="auto" aria-hidden="true" />
      <div className="login-bg">
        <video
          className="login-bg-video"
          src={fondoEntradaVideoUrl}
          autoPlay
          loop
          muted
          playsInline
          preload="auto"
          aria-hidden="true"
        >
          Tu navegador no soporta el video.
        </video>

        {isYenMiniGameEnabled &&
          geldMoneySprites.map((sprite) => (
            <button
              key={sprite.id}
              className="login-bg-geld-money"
              type="button"
              onClick={() => captureGeldMoneySprite(sprite.id)}
              onAnimationEnd={() => removeGeldMoneySprite(sprite.id)}
              aria-label="Capturar yen"
              tabIndex={-1}
              style={{
                "--from-x": sprite.fromX,
                "--from-y": sprite.fromY,
                "--to-x": sprite.toX,
                "--to-y": sprite.toY,
                "--money-duration": `${sprite.durationMs}ms`,
                "--money-scale": sprite.scale,
                "--money-rot": `${sprite.rotate}deg`,
                "--money-flip-x": sprite.flipX,
              }}
            >
              <img src={flyingYenGifUrl} alt="" draggable="false" />
            </button>
          ))}
      </div>

      {showUi && isYenMiniGameEnabled && (
        <button
          className="login-money-counter-btn"
          type="button"
          aria-label="Dinero capturado"
          tabIndex={-1}
        >
          ${moneyScore.toLocaleString("en-US")}
        </button>
      )}

      {showUi && (
        <>
          {mode !== "character" && mode !== "signup" && (
            <div className={`auth-layout${uiEnterClass}`}>
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
		                className="dungeon-btn"
		                type="button"
		                onClick={goChoose}
		                disabled={isSubmitting}
		              >
		                Volver
		              </button>
	            </form>
	          )}

            <div className="login-aux-buttons">
              <button
                className="credits-btn game-btn"
                type="button"
                onClick={onGameInfo}
                disabled={isSubmitting}
              >
                El juego
              </button>

              <button
                className="credits-btn game-btn"
                type="button"
                onClick={onCredits}
                disabled={isSubmitting}
              >
                Credits
              </button>
            </div>
	        </div>
	      )}

      {mode === "character" && (
        <div className={`character-layout${uiEnterClass}`}>
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
	        <div className={`signup-layout${uiEnterClass}`}>
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
        </>
      )}
	    </div>
	  );
}

export default Login;
