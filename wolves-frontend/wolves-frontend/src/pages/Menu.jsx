import { useEffect, useRef, useState } from "react";
import { apiRequest, clearToken } from "../services/api";

const flameGifUrl = new URL("../assets/animaciones/llama.gif", import.meta.url).href;
const wolfGreetingVideoUrl = new URL(
  "../assets/animaciones/lobo saludo.mp4",
  import.meta.url
).href;
const noteImgUrl = new URL("../assets/images/nota.png", import.meta.url).href;
const noteIconImgUrl = new URL("../assets/images/notaIcono.png", import.meta.url).href;

function sanitizeNotes(value) {
  return String(value ?? "")
    .replace(/\u00a0/g, " ")
    // If the backend stored escaped newlines (literal "\n"), unescape them.
    .replace(/\\r\\n/g, "\n")
    .replace(/\\n/g, "\n")
    .replace(/\r\n/g, "\n")
    .replace(/\r/g, "\n")
    .trimEnd();
}

function stripNotesHeader(value) {
  const text = String(value ?? "");
  const lines = text.split("\n");
  if (lines.length === 0) return text;
  const first = lines[0]?.trim() ?? "";
  if (!first.toUpperCase().startsWith("NOTAS:")) return text;
  return lines.slice(1).join("\n").trimStart();
}

function Menu({ onNewGame, onMistakesGame, onStats, onAdmin, onBackToLogin, onDuckBgMusic }) {
  const [showDifficulty, setShowDifficulty] = useState(false);
  const [isNotesOpen, setIsNotesOpen] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);
  const [meChecked, setMeChecked] = useState(false);
  const [notes, setNotes] = useState("");
  const [notesDraft, setNotesDraft] = useState("");
  const [notesError, setNotesError] = useState("");
  const [isSavingNotes, setIsSavingNotes] = useState(false);
  const lastSavedNotesRef = useRef("");
  const notesLoadedRef = useRef(false);
  const isSavingNotesRef = useRef(false);
  const notesBaselineRef = useRef("");

		  const replayGreetingVideo = (video) => {
		    video.muted = false;
		    video.volume = 1;
		    video.currentTime = 0;
        if (typeof onDuckBgMusic === "function") onDuckBgMusic(0.8);
		    const playAttempt = video.play();
		    if (playAttempt && typeof playAttempt.catch === "function") playAttempt.catch(() => {});
		  };

  const notesHeader = "NOTAS:";
  const defaultNotes = `-El lobo olía a rancio...\n-Repasar teoria Testing y lamdas.\n-Tus notas aqui ->`;

  const saveNotes = async (nextNotes) => {
    const sanitizedNotes = stripNotesHeader(sanitizeNotes(nextNotes));
    if (isSavingNotesRef.current) return;
    isSavingNotesRef.current = true;
    setIsSavingNotes(true);
    setNotesError("");

    try {
      await apiRequest("/me/notes", { method: "PATCH", body: sanitizedNotes });
      lastSavedNotesRef.current = sanitizedNotes;
      setNotes(sanitizedNotes);
      setNotesDraft(sanitizedNotes);
      notesBaselineRef.current = sanitizedNotes;
    } catch (err) {
      setNotesError(err instanceof Error ? err.message : "Error al guardar las notas.");
    } finally {
      isSavingNotesRef.current = false;
      setIsSavingNotes(false);
    }
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
        const serverNotes = stripNotesHeader(sanitizeNotes(me?.notes ?? ""));
        setNotes(serverNotes);
        lastSavedNotesRef.current = serverNotes;
        notesLoadedRef.current = true;
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

  useEffect(() => {
    if (!isNotesOpen) return undefined;

    const onKeyDown = (e) => {
      if (e.key === "Escape") setIsNotesOpen(false);
    };

    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [isNotesOpen]);

  useEffect(() => {
    if (!isNotesOpen) return undefined;
    if (!meChecked) return undefined;
    if (!notesLoadedRef.current) return undefined;
    if (notesDraft === notesBaselineRef.current) return undefined;

    const id = window.setTimeout(() => {
      void saveNotes(notesDraft);
    }, 700);

    return () => window.clearTimeout(id);
  }, [isNotesOpen, notesDraft, meChecked]);

  const startNewGame = (difficulty) => {
    setShowDifficulty(false);
    onNewGame(difficulty);
  };

  const openNotes = () => {
    const existingNotes = stripNotesHeader(sanitizeNotes(notes));
    const baseline = existingNotes.trim() ? existingNotes : defaultNotes;
    notesBaselineRef.current = baseline;
    setNotesDraft(baseline);
    setNotesError("");
    setIsNotesOpen(true);
  };

  const closeNotes = () => {
    setIsNotesOpen(false);
    setNotesError("");
  };

  return (
    <div className="screen menu-screen menu-screen--softblur">
      <div className="menu-stack">
        <div className="menu-video-wrap">
          <div className="menu-video-frame">
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
              onPlay={() => {
                if (typeof onDuckBgMusic === "function") onDuckBgMusic(0.8);
              }}
              onPause={() => {
                if (typeof onDuckBgMusic === "function") onDuckBgMusic(1);
              }}
              onEnded={(e) => {
                const video = e.currentTarget;
                const duration = video.duration;
                if (!Number.isFinite(duration) || duration <= 0) return;
                video.pause();
                video.currentTime = Math.max(0, duration - 0.05);
                if (typeof onDuckBgMusic === "function") onDuckBgMusic(1);
              }}
            />
            {!isNotesOpen && (
              <button
                className="menu-note-wrap"
                type="button"
                onClick={openNotes}
                aria-label="Abrir notas"
              >
                <img className="menu-note-img" src={noteIconImgUrl} alt="" aria-hidden="true" />
              </button>
            )}

            {isNotesOpen && (
              <div className="menu-notes-popover" role="dialog" aria-label="Notas">
                <div className="menu-notes-card">
                  <img className="menu-notes-img" src={noteImgUrl} alt="" aria-hidden="true" />
                  <p className="menu-notes-header" aria-hidden="true">
                    {notesHeader}
                  </p>
                  <button
                    className="menu-notes-close"
                    type="button"
                    onClick={() => {
                      if (notesDraft !== notesBaselineRef.current) void saveNotes(notesDraft);
                      closeNotes();
                    }}
                    aria-label="Cerrar notas"
                    disabled={isSavingNotes}
                  >
                    ×
                  </button>
                  <textarea
                    className="menu-notes-textarea"
                    value={notesDraft}
                    onChange={(e) => setNotesDraft(e.target.value)}
                    onBlur={() => {
                      if (!notesLoadedRef.current) return;
                      if (notesDraft === notesBaselineRef.current) return;
                      void saveNotes(notesDraft);
                    }}
                    aria-label="Editar notas"
                    spellCheck
                  />
                  {notesError && <p className="menu-notes-error">{notesError}</p>}
                  {!notesError && isSavingNotes && <p className="menu-notes-saving">Guardando...</p>}
                </div>
              </div>
            )}
          </div>
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
