function Game({ onFinish, difficulty }) {
  return (
    <div className="screen game-screen">
      <div className="game-layout">
        <p className="game-difficulty-pill">{String(difficulty ?? "").toUpperCase()}</p>
        <h2 className="question-text">
          ¿Para qué se utiliza el método hashCode()?
        </h2>

        <div className="answers-container">
          <button className="dungeon-btn" type="button">
            Para ordenar colecciones
          </button>

          <button className="dungeon-btn" type="button">
            Para identificar objetos en estructuras como HashMap
          </button>

          <button className="dungeon-btn" type="button">
            Para convertir objetos en texto
          </button>

          <button className="dungeon-btn" type="button">
            Para comparar objetos por referencia
          </button>
        </div>

        <button className="dungeon-btn" onClick={onFinish} type="button">
          Ir a resultado
        </button>
      </div>
    </div>
  );
}

export default Game;
