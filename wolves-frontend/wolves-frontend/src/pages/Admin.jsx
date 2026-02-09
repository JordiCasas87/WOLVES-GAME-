import { useEffect, useMemo, useState } from "react";
import { apiRequest } from "../services/api";

const adminWolfVideoUrl = new URL("../assets/animaciones/loboAdmin.mp4", import.meta.url).href;
const adminWolfPosterUrl = new URL("../assets/images/loboAdmin.jpg", import.meta.url).href;

function isAdminRole(role) {
  return String(role ?? "").toUpperCase() === "ADMIN";
}

function Admin({ onBack }) {
  const [me, setMe] = useState(null);
  const [players, setPlayers] = useState([]);
  const [questions, setQuestions] = useState([]);
  const [isPlayersOpen, setIsPlayersOpen] = useState(false);
  const [isQuestionsOpen, setIsQuestionsOpen] = useState(false);
  const [isLoadingMe, setIsLoadingMe] = useState(true);
  const [isLoadingPlayers, setIsLoadingPlayers] = useState(false);
  const [isLoadingQuestions, setIsLoadingQuestions] = useState(false);
  const [error, setError] = useState("");
  const [info, setInfo] = useState("");
  const [isAdminSoundEnabled, setIsAdminSoundEnabled] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(null);
  const [playerModalId, setPlayerModalId] = useState("");
  const [playerModalData, setPlayerModalData] = useState(null);
  const [playerModalDraft, setPlayerModalDraft] = useState(null);
  const [isPlayerModalOpen, setIsPlayerModalOpen] = useState(false);
  const [isLoadingPlayerModal, setIsLoadingPlayerModal] = useState(false);
  const [isSavingPlayerModal, setIsSavingPlayerModal] = useState(false);

  useEffect(() => {
    let isActive = true;

    async function loadMe() {
      setIsLoadingMe(true);
      setError("");
      setInfo("");

      try {
        const data = await apiRequest("/me");
        if (!isActive) return;
        setMe(data ?? null);
      } catch (err) {
        if (!isActive) return;
        setError(err instanceof Error ? err.message : "Error al cargar el perfil.");
      } finally {
        if (isActive) setIsLoadingMe(false);
      }
    }

    loadMe();
    return () => {
      isActive = false;
    };
  }, []);

  useEffect(() => {
    if (!confirmDelete && !isPlayerModalOpen) return undefined;

    const onKeyDown = (e) => {
      if (e.key !== "Escape") return;
      setConfirmDelete(null);
      setIsPlayerModalOpen(false);
    };

    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [confirmDelete, isPlayerModalOpen]);

  const loadPlayers = async () => {
    setIsLoadingPlayers(true);
    setError("");
    setInfo("");

    try {
      const list = await apiRequest("/players");
      setPlayers(Array.isArray(list) ? list : []);
      setInfo("Jugadores cargados.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error al cargar jugadores.");
    } finally {
      setIsLoadingPlayers(false);
    }
  };

  const togglePlayers = async () => {
    if (isLoadingPlayers) return;

    if (isPlayersOpen) {
      setIsPlayersOpen(false);
      return;
    }

    setIsPlayersOpen(true);
    if (players.length === 0) await loadPlayers();
  };

  const loadQuestions = async () => {
    setIsLoadingQuestions(true);
    setError("");
    setInfo("");

    try {
      const list = await apiRequest("/questions");
      setQuestions(Array.isArray(list) ? list : []);
      setInfo("Preguntas cargadas.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error al cargar preguntas.");
    } finally {
      setIsLoadingQuestions(false);
    }
  };

  const toggleQuestions = async () => {
    if (isLoadingQuestions) return;

    if (isQuestionsOpen) {
      setIsQuestionsOpen(false);
      return;
    }

    setIsQuestionsOpen(true);
    if (questions.length === 0) await loadQuestions();
  };

  const openPlayerModal = async (id) => {
    const finalId = String(id ?? "").trim();
    if (!finalId) return;

    setIsPlayerModalOpen(true);
    setPlayerModalId(finalId);
    setPlayerModalData(null);
    setPlayerModalDraft(null);
    setError("");
    setInfo("");
    setIsLoadingPlayerModal(true);

    try {
      const data = await apiRequest(`/players/${encodeURIComponent(finalId)}`);
      setPlayerModalData(data ?? null);
      setPlayerModalDraft(
        data
          ? {
              name: String(data.name ?? ""),
              age: String(data.age ?? ""),
              level: String(data.level ?? ""),
              money: String(data.money ?? ""),
              role: String(data.role ?? "USER"),
            }
          : null,
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error al cargar el jugador.");
    } finally {
      setIsLoadingPlayerModal(false);
    }
  };

  const closePlayerModal = () => {
    if (isSavingPlayerModal) return;
    setIsPlayerModalOpen(false);
    setPlayerModalId("");
    setPlayerModalData(null);
    setPlayerModalDraft(null);
    setIsLoadingPlayerModal(false);
  };

  const handleDeletePlayer = async (id) => {
    const finalId = String(id ?? "").trim();
    if (!finalId) return;
    setError("");
    setInfo("");

    try {
      await apiRequest(`/players/${encodeURIComponent(finalId)}`, { method: "DELETE" });
      setPlayers((prev) => prev.filter((p) => String(p.id ?? "") !== finalId));
      setInfo("Jugador eliminado.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error al eliminar jugador.");
    }
  };

  const savePlayerModal = async () => {
    if (!playerModalId || !playerModalDraft || isSavingPlayerModal) return;
    setError("");
    setInfo("");
    setIsSavingPlayerModal(true);

    try {
      const toIntOrUndefined = (value) => {
        if (value == null) return undefined;
        const text = String(value).trim();
        if (!text) return undefined;
        const parsed = Number.parseInt(text, 10);
        return Number.isFinite(parsed) ? parsed : undefined;
      };

      const nextName = String(playerModalDraft.name ?? "").trim();
      if (!nextName) {
        setError("El nombre no puede estar vacío.");
        return;
      }

      const payload = {};
      if (nextName !== String(playerModalData?.name ?? "")) payload.name = nextName;

      const nextAge = toIntOrUndefined(playerModalDraft.age);
      if (nextAge !== undefined && nextAge !== Number(playerModalData?.age ?? 0)) payload.age = nextAge;

      const nextLevel = toIntOrUndefined(playerModalDraft.level);
      if (nextLevel !== undefined && nextLevel !== Number(playerModalData?.level ?? 0))
        payload.level = nextLevel;

      const nextMoney = toIntOrUndefined(playerModalDraft.money);
      if (nextMoney !== undefined && nextMoney !== Number(playerModalData?.money ?? 0))
        payload.money = nextMoney;

      const nextRole = String(playerModalDraft.role ?? "").trim();
      if (nextRole && nextRole !== String(playerModalData?.role ?? "")) payload.role = nextRole;

      const updated = await apiRequest(`/players/${encodeURIComponent(playerModalId)}`, {
        method: "PUT",
        body: payload,
      });

      setPlayerModalData(updated ?? null);
      if (updated) {
        setPlayerModalDraft({
          name: String(updated.name ?? ""),
          age: String(updated.age ?? ""),
          level: String(updated.level ?? ""),
          money: String(updated.money ?? ""),
          role: String(updated.role ?? "USER"),
        });
      }
      setInfo("Jugador actualizado.");
      setPlayers((prev) =>
        prev.map((p) => (String(p?.id ?? "") === playerModalId ? { ...p, ...(updated ?? {}) } : p)),
      );
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Error al actualizar el jugador.",
      );
    } finally {
      setIsSavingPlayerModal(false);
    }
  };

  const handleDeleteQuestion = async (id) => {
    const finalId = String(id ?? "").trim();
    if (!finalId) return;
    setError("");
    setInfo("");

    try {
      await apiRequest(`/questions/${encodeURIComponent(finalId)}`, { method: "DELETE" });
      setInfo("Pregunta eliminada.");
      setQuestions((prev) => prev.filter((q) => String(q?.id ?? "") !== finalId));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error al eliminar pregunta.");
    }
  };

  const onConfirmDelete = async () => {
    if (!confirmDelete) return;

    try {
      if (confirmDelete.kind === "player") {
        await handleDeletePlayer(confirmDelete.id);
      } else {
        await handleDeleteQuestion(confirmDelete.id);
      }
      setConfirmDelete(null);
    } catch {
      // keep modal open; error is shown above
    }
  };

  const toggleAdminVideoSound = (video) => {
    setIsAdminSoundEnabled((prev) => {
      const next = !prev;
      video.muted = !next;

      if (next) {
        video.volume = 0.65;
        const attempt = video.play?.();
        if (attempt && typeof attempt.catch === "function") attempt.catch(() => {});
      }

      return next;
    });
  };

  const canUseAdmin = isAdminRole(me?.role);

  const confirmTitle = useMemo(() => {
    if (!confirmDelete) return "";
    return confirmDelete.kind === "player"
      ? "¿Seguro que quieres borrar este jugador?"
      : "¿Seguro que quieres borrar esta pregunta?";
  }, [confirmDelete]);

  const playerModalTitle = useMemo(() => {
    if (!playerModalData) return "Jugador";
    return `Jugador: ${playerModalData.name ?? "-"}`;
  }, [playerModalData]);

  return (
    <div className="screen menu-screen menu-screen--softblur">
      <div className="menu-layout stats-layout admin-layout">
        <video
          className="admin-wolf-img"
          src={adminWolfVideoUrl}
          autoPlay
          loop
          muted={!isAdminSoundEnabled}
          playsInline
          preload="metadata"
          poster={adminWolfPosterUrl}
          role="button"
          tabIndex={0}
          aria-label={
            isAdminSoundEnabled
              ? "Silenciar vídeo de administración"
              : "Activar sonido del vídeo de administración"
          }
          onLoadedMetadata={(e) => {
            e.currentTarget.volume = 0.65;
          }}
          onClick={(e) => toggleAdminVideoSound(e.currentTarget)}
          onKeyDown={(e) => {
            if (e.key === "Enter" || e.key === " ") {
              e.preventDefault();
              toggleAdminVideoSound(e.currentTarget);
            }
          }}
        />
        <h2 className="stats-title">Administración</h2>

        {isLoadingMe && <p className="helper-text">Cargando...</p>}
        {error && <p className="error-text">{error}</p>}
        {info && <p className="helper-text">{info}</p>}

        {!isLoadingMe && me && (
          <section className="stats-card" aria-label="Perfil">
            <div className="stats-row">
              <span>Usuario</span>
              <strong>{me.name ?? "-"}</strong>
            </div>
            <div className="stats-row">
              <span>Rol</span>
              <strong>{String(me.role ?? "-")}</strong>
            </div>
          </section>
        )}

        {!isLoadingMe && me && !canUseAdmin && (
          <p className="error-text">No tienes permisos de administrador.</p>
        )}

        {canUseAdmin && (
          <>
            <section className="stats-card" aria-label="Acciones admin">
              <p className="stats-subtitle">Acciones</p>

              <div className="admin-actions">
                <button
                  className="dungeon-btn"
                  type="button"
                  onClick={togglePlayers}
                  disabled={isLoadingPlayers}
                >
                  {isLoadingPlayers
                    ? "Cargando..."
                    : isPlayersOpen
                      ? "Ocultar jugadores"
                      : players.length > 0
                        ? "Mostrar jugadores"
                        : "Cargar jugadores"}
                </button>

                <button
                  className="dungeon-btn"
                  type="button"
                  onClick={toggleQuestions}
                  disabled={isLoadingQuestions}
                >
                  {isLoadingQuestions
                    ? "Cargando..."
                    : isQuestionsOpen
                      ? "Ocultar preguntas"
                      : questions.length > 0
                        ? "Mostrar preguntas"
                        : "Cargar preguntas"}
                </button>
              </div>
            </section>

            {isPlayersOpen && players.length > 0 && (
              <section className="stats-card" aria-label="Jugadores">
                <p className="stats-subtitle">Jugadores</p>
                <ol className="ranking-list">
                  {players.map((p, idx) => (
                    <li key={p.id ?? `${p.name ?? "player"}-${idx}`}>
                      <button
                        className="ranking-item admin-row-btn"
                        type="button"
                        onClick={() => {
                          openPlayerModal(p.id ?? "");
                        }}
                        aria-label={`Ver jugador ${p.name ?? "?"}`}
                      >
                        <span className="ranking-pos">#{idx + 1}</span>
                        <span className="ranking-name">
                          {p.name ?? "?"} ({String(p.role ?? "-")})
                        </span>
                        <span className="ranking-score">{p.money ?? 0}</span>
                      </button>
                    </li>
                  ))}
                </ol>
              </section>
            )}

            {isPlayersOpen && players.length === 0 && (
              <p className="helper-text">No hay jugadores cargados.</p>
            )}

            {isQuestionsOpen && questions.length > 0 && (
              <section className="stats-card" aria-label="Preguntas">
                <p className="stats-subtitle">Preguntas</p>
                <ol className="admin-question-list">
                  {questions.map((q, idx) => (
                    <li key={q.id ?? `${idx}`}>
                      <button
                        className="admin-question-item admin-row-btn"
                        type="button"
                        onClick={() => {
                          setConfirmDelete({
                            kind: "question",
                            id: q.id ?? "",
                            label: q.text ?? "?",
                          });
                        }}
                        aria-label="Borrar pregunta"
                      >
                        <span className="admin-question-pos">#{idx + 1}</span>
                        <span className="admin-question-text">{q.text ?? "?"}</span>
                      </button>
                    </li>
                  ))}
                </ol>
              </section>
            )}

            {isQuestionsOpen && questions.length === 0 && (
              <p className="helper-text">No hay preguntas cargadas.</p>
            )}
          </>
        )}

        <div className="stats-actions">
          <button className="dungeon-btn" onClick={onBack} type="button">
            Volver al menú
          </button>
        </div>
      </div>

      {confirmDelete && (
        <div
          className="admin-confirm-backdrop"
          role="presentation"
          onClick={() => setConfirmDelete(null)}
        >
          <div
            className="admin-confirm-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="admin-confirm-title"
            onClick={(e) => e.stopPropagation()}
          >
            <h3 className="admin-confirm-title" id="admin-confirm-title">
              {confirmTitle}
            </h3>
            <p className="admin-confirm-body">{confirmDelete.label}</p>
            <p className="admin-confirm-id">
              ID: <code>{confirmDelete.id || "-"}</code>
            </p>
            <div className="admin-confirm-actions">
              <button
                className="secondary-button admin-confirm-btn"
                type="button"
                onClick={() => setConfirmDelete(null)}
              >
                Cancelar
              </button>
              <button
                className="dungeon-btn admin-confirm-danger"
                type="button"
                onClick={onConfirmDelete}
                disabled={!confirmDelete.id}
              >
                Borrar
              </button>
            </div>
          </div>
        </div>
      )}

      {isPlayerModalOpen && (
        <div className="admin-confirm-backdrop" role="presentation" onClick={closePlayerModal}>
          <div
            className="admin-detail-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="admin-player-title"
            onClick={(e) => e.stopPropagation()}
          >
            <h3 className="admin-confirm-title" id="admin-player-title">
              {playerModalTitle}
            </h3>

            {isLoadingPlayerModal && <p className="helper-text">Cargando jugador...</p>}

            {!isLoadingPlayerModal && !playerModalData && (
              <p className="error-text">No se ha podido cargar el jugador.</p>
            )}

            {!isLoadingPlayerModal && playerModalData && (
              <>
                <div className="admin-detail-grid" aria-label="Datos del jugador">
                  <div className="admin-detail-row">
                    <span className="admin-detail-label">ID</span>
                    <code className="admin-detail-value">{playerModalData.id ?? "-"}</code>
                  </div>
                  <div className="admin-detail-row">
                    <span className="admin-detail-label">Nombre</span>
                    <span className="admin-detail-value">{playerModalData.name ?? "-"}</span>
                  </div>
                  <div className="admin-detail-row">
                    <span className="admin-detail-label">Creación</span>
                    <span className="admin-detail-value">
                      {String(playerModalData.dateOfCreation ?? "-")}
                    </span>
                  </div>
                  <div className="admin-detail-row">
                    <span className="admin-detail-label">Preguntas falladas</span>
                    <span className="admin-detail-value">
                      {Array.isArray(playerModalData.incorrectQuestionsIdList)
                        ? playerModalData.incorrectQuestionsIdList.length
                        : 0}
                    </span>
                  </div>
                </div>

                <div className="admin-edit-grid" aria-label="Editar jugador">
                  <label className="admin-edit-field admin-edit-field--full">
                    <span className="admin-detail-label">Nombre</span>
                    <input
                      className="text-input"
                      value={playerModalDraft?.name ?? ""}
                      onChange={(e) =>
                        setPlayerModalDraft((prev) => ({ ...(prev ?? {}), name: e.target.value }))
                      }
                      disabled={isSavingPlayerModal}
                    />
                  </label>

                  <label className="admin-edit-field">
                    <span className="admin-detail-label">Edad</span>
                    <input
                      className="text-input"
                      type="number"
                      inputMode="numeric"
                      min={0}
                      value={playerModalDraft?.age ?? ""}
                      onChange={(e) =>
                        setPlayerModalDraft((prev) => ({ ...(prev ?? {}), age: e.target.value }))
                      }
                      disabled={isSavingPlayerModal}
                    />
                  </label>

                  <label className="admin-edit-field">
                    <span className="admin-detail-label">Nivel</span>
                    <input
                      className="text-input"
                      type="number"
                      inputMode="numeric"
                      min={0}
                      value={playerModalDraft?.level ?? ""}
                      onChange={(e) =>
                        setPlayerModalDraft((prev) => ({ ...(prev ?? {}), level: e.target.value }))
                      }
                      disabled={isSavingPlayerModal}
                    />
                  </label>

                  <label className="admin-edit-field">
                    <span className="admin-detail-label">Dinero</span>
                    <input
                      className="text-input"
                      type="number"
                      inputMode="numeric"
                      min={0}
                      value={playerModalDraft?.money ?? ""}
                      onChange={(e) =>
                        setPlayerModalDraft((prev) => ({ ...(prev ?? {}), money: e.target.value }))
                      }
                      disabled={isSavingPlayerModal}
                    />
                  </label>

                  <label className="admin-edit-field">
                    <span className="admin-detail-label">Rol</span>
                    <select
                      className="text-input"
                      value={playerModalDraft?.role ?? "USER"}
                      onChange={(e) =>
                        setPlayerModalDraft((prev) => ({ ...(prev ?? {}), role: e.target.value }))
                      }
                      disabled={isSavingPlayerModal}
                    >
                      <option value="USER">USER</option>
                      <option value="ADMIN">ADMIN</option>
                    </select>
                  </label>
                </div>

                <div className="admin-confirm-actions">
                  <button className="dungeon-btn" type="button" onClick={closePlayerModal}>
                    Cerrar
                  </button>
                  <button
                    className="dungeon-btn"
                    type="button"
                    onClick={savePlayerModal}
                    disabled={isSavingPlayerModal || !playerModalId}
                  >
                    {isSavingPlayerModal ? "Guardando..." : "Guardar cambios"}
                  </button>
                </div>

                <div className="admin-detail-danger">
                  <button
                    className="dungeon-btn admin-confirm-danger"
                    type="button"
                    onClick={() => {
                      closePlayerModal();
                      setConfirmDelete({
                        kind: "player",
                        id: playerModalData.id ?? "",
                        label: `${playerModalData.name ?? "?"} (${String(playerModalData.role ?? "-")})`,
                      });
                    }}
                    disabled={isSavingPlayerModal}
                  >
                    Borrar jugador
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default Admin;
