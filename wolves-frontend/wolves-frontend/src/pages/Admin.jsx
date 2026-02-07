import { useEffect, useState } from "react";
import { apiRequest } from "../services/api";

const adminWolfImgUrl = new URL("../assets/images/loboAdmin.jpg", import.meta.url).href;

function isAdminRole(role) {
  return String(role ?? "").toUpperCase() === "ADMIN";
}

function Admin({ onBack }) {
  const [me, setMe] = useState(null);
  const [players, setPlayers] = useState([]);
  const [isLoadingMe, setIsLoadingMe] = useState(true);
  const [isLoadingPlayers, setIsLoadingPlayers] = useState(false);
  const [error, setError] = useState("");
  const [info, setInfo] = useState("");

  const [deletePlayerId, setDeletePlayerId] = useState("");
  const [deleteQuestionId, setDeleteQuestionId] = useState("");

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

  const handleDeletePlayer = async () => {
    const id = deletePlayerId.trim();
    if (!id) return;
    setError("");
    setInfo("");

    try {
      await apiRequest(`/players/${encodeURIComponent(id)}`, { method: "DELETE" });
      setPlayers((prev) => prev.filter((p) => String(p.id ?? "") !== id));
      setInfo("Jugador eliminado.");
      setDeletePlayerId("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error al eliminar jugador.");
    }
  };

  const handleDeleteQuestion = async () => {
    const id = deleteQuestionId.trim();
    if (!id) return;
    setError("");
    setInfo("");

    try {
      await apiRequest(`/questions/${encodeURIComponent(id)}`, { method: "DELETE" });
      setInfo("Pregunta eliminada.");
      setDeleteQuestionId("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error al eliminar pregunta.");
    }
  };

  const canUseAdmin = isAdminRole(me?.role);

  return (
    <div className="screen menu-screen menu-screen--softblur">
      <div className="menu-layout stats-layout admin-layout">
        <img className="admin-wolf-img" src={adminWolfImgUrl} alt="Lobo admin" />
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
                  onClick={loadPlayers}
                  disabled={isLoadingPlayers}
                >
                  {isLoadingPlayers ? "Cargando..." : "Cargar jugadores"}
                </button>

                <div className="admin-inline">
                  <input
                    className="text-input"
                    value={deletePlayerId}
                    onChange={(e) => setDeletePlayerId(e.target.value)}
                    placeholder="ID jugador a borrar"
                  />
                  <button className="dungeon-btn" type="button" onClick={handleDeletePlayer}>
                    Borrar jugador
                  </button>
                </div>

                <div className="admin-inline">
                  <input
                    className="text-input"
                    value={deleteQuestionId}
                    onChange={(e) => setDeleteQuestionId(e.target.value)}
                    placeholder="ID pregunta a borrar"
                  />
                  <button className="dungeon-btn" type="button" onClick={handleDeleteQuestion}>
                    Borrar pregunta
                  </button>
                </div>
              </div>
            </section>

            {players.length > 0 && (
              <section className="stats-card" aria-label="Jugadores">
                <p className="stats-subtitle">Jugadores</p>
                <ol className="ranking-list">
                  {players.map((p, idx) => (
                    <li key={p.id ?? `${p.name ?? "player"}-${idx}`} className="ranking-item">
                      <span className="ranking-pos">#{idx + 1}</span>
                      <span className="ranking-name">
                        {p.name ?? "?"} ({String(p.role ?? "-")}){" "}
                        <span className="admin-player-id">ID: {p.id ?? "-"}</span>
                      </span>
                      <span className="ranking-score">{p.money ?? 0}</span>
                    </li>
                  ))}
                </ol>
              </section>
            )}
          </>
        )}

        <div className="stats-actions">
          <button className="dungeon-btn" onClick={onBack} type="button">
            Volver al menú
          </button>
        </div>
      </div>
    </div>
  );
}

export default Admin;
