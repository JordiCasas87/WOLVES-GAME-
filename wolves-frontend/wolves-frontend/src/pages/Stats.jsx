import { useEffect, useState } from "react";
import { apiRequest, clearToken } from "../services/api";

function Stats({ onBack, onBackToLogin }) {
  const [me, setMe] = useState(null);
  const [ranking, setRanking] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const topRanking = ranking.slice(0, 10);

  useEffect(() => {
    let isActive = true;

    async function load() {
      setIsLoading(true);
      setError("");

      try {
        const [meData, rankingData] = await Promise.all([
          apiRequest("/me"),
          apiRequest("/players/ranking"),
        ]);
        if (!isActive) return;
        setMe(meData ?? null);
        setRanking(Array.isArray(rankingData) ? rankingData : []);
      } catch (err) {
        if (!isActive) return;
        setError(err instanceof Error ? err.message : "Error al cargar el estatus.");
      } finally {
        if (isActive) setIsLoading(false);
      }
    }

    load();
    return () => {
      isActive = false;
    };
  }, []);

  return (
    <div className="screen menu-screen stats-screen">
      <div className="menu-layout stats-layout">
        <h2 className="stats-title">Estatus y ranking</h2>

        {isLoading && <p className="helper-text">Cargando...</p>}
        {error && <p className="error-text">{error}</p>}

        {me && !isLoading && (
          <section className="stats-card" aria-label="Estatus del jugador">
            <div className="stats-row">
              <span>Jugador</span>
              <strong>{me.name ?? "-"}</strong>
            </div>
            <div className="stats-row">
              <span>Nivel</span>
              <strong>{me.level ?? "-"}</strong>
            </div>
            <div className="stats-row">
              <span>Dinero</span>
              <strong>{me.money ?? "-"}</strong>
            </div>
            <div className="stats-row">
              <span>Partidas</span>
              <strong>{me.gamesPlayed ?? "-"}</strong>
            </div>
          </section>
        )}

        {!isLoading && (
          <section className="stats-card" aria-label="Ranking">
            <h3 className="stats-subtitle">Top 10</h3>
            {ranking.length === 0 ? (
              <p className="helper-text">Sin datos de ranking.</p>
            ) : (
              <ol className="ranking-list">
                {topRanking.map((p, idx) => {
                  const medal =
                    idx === 0 ? "gold" : idx === 1 ? "silver" : idx === 2 ? "bronze" : null;

                  return (
                    <li
                      key={p.playerId ?? p.id ?? `${p.name ?? "player"}-${idx}`}
                      className="ranking-item"
                    >
                      <span className={`ranking-pos ${medal ? `ranking-pos--${medal}` : ""}`}>
                        {medal && (
                          <span
                            className={`ranking-medal ranking-medal--${medal}`}
                            aria-hidden="true"
                            title={`Medalla ${medal}`}
                          />
                        )}
                        #{idx + 1}
                      </span>
                      <span className="ranking-name">{p.name ?? "?"}</span>
                      <span className="ranking-score">{p.money ?? 0}</span>
                    </li>
                  );
                })}
              </ol>
            )}
          </section>
        )}

        <div className="stats-actions">
          <button className="dungeon-btn" onClick={onBack} type="button">
            Volver al menu
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
      </div>
    </div>
  );
}

export default Stats;
