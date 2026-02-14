const planoImgUrl = new URL("../assets/images/plano.jpg", import.meta.url).href;

function PlanoSplash({ onDone, onBack }) {
  return (
    <div className="screen plano-screen" aria-label="Pantalla previa al menú">
      <p className="plano-message" aria-hidden="true">
        Elige tu código del alma y preparate para ser entrevistado!
      </p>
      <div className="plano-card">
        <img className="plano-img" src={planoImgUrl} alt="" aria-hidden="true" />
        <button
          className="plano-enter-hit"
          type="button"
          onClick={() => onDone?.()}
          aria-label="Entrar"
        />
      </div>
      <button className="plano-back-btn" type="button" onClick={() => onBack?.()}>
        Volver al login
      </button>
    </div>
  );
}

export default PlanoSplash;
