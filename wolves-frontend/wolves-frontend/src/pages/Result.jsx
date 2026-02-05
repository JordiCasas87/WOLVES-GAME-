function Result({ onRestart }) {
  return (
    <div className="screen">
      <h1>RESULTADO</h1>
      <p>El lobo decide tu destino...</p>
      <button className="primary-button" onClick={onRestart}>
        Volver al inicio
      </button>
    </div>
  );
}

export default Result;