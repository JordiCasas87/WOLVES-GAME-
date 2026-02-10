import { useEffect, useRef, useState } from "react";
import "./App.css";
import Login from "./pages/Login";
import Menu from "./pages/Menu";
import Game from "./pages/Game";
import Result from "./pages/Result";
import Stats from "./pages/Stats";
import Admin from "./pages/Admin";
import Credits from "./pages/Credits";
import GameInfo from "./pages/GameInfo";
import PlanoSplash from "./pages/PlanoSplash";
import { clearToken } from "./services/api";

const bgMusicUrl = new URL("./assets/sounds/smash.mp3", import.meta.url).href;
const elevatorMusicUrl = new URL("./assets/sounds/Elevator.mp3", import.meta.url).href;
const naviSfxUrl = new URL("./assets/sounds/navi.mp3", import.meta.url).href;
const roarSfxUrl = new URL("./assets/sounds/Roar.mp3", import.meta.url).href;

function App() {
  const [screen, setScreen] = useState("login");
  const [gameDifficulty, setGameDifficulty] = useState("easy"); // easy | medium | hard
  const [gameMode, setGameMode] = useState("new"); // new | mistakes
  const [gameResult, setGameResult] = useState(null);
  const [bgMusicDuckMultiplier, setBgMusicDuckMultiplier] = useState(1);
  const bgMusicRef = useRef(null);
  const elevatorMusicRef = useRef(null);
  const naviSfxRef = useRef(null);
  const roarSfxRef = useRef(null);
  const naviSfxTimeoutRef = useRef(null);

	  const bgMusicMode =
	    screen === "menu" || screen === "stats" || screen === "admin"
	      ? "low"
	      : screen === "login" || screen === "credits" || screen === "gameInfo" || screen === "plano"
	        ? "full"
	        : "off";
	  const bgMusicVolume =
	    bgMusicMode === "low" ? 0.18 : bgMusicMode === "full" ? 0.75 : 0;
  const isElevatorActive = screen === "game";

  useEffect(() => {
    // Ensure we never keep background music ducked when leaving the menu screen.
    if (screen !== "menu") setBgMusicDuckMultiplier(1);
  }, [screen]);

  useEffect(() => {
    return () => {
      if (naviSfxTimeoutRef.current) window.clearTimeout(naviSfxTimeoutRef.current);
    };
  }, []);

  useEffect(() => {
    const audio = bgMusicRef.current;
    if (!audio) return undefined;

    if (bgMusicMode === "off") {
      audio.pause();
      return undefined;
    }

    audio.volume = Math.max(0, Math.min(1, bgMusicVolume * bgMusicDuckMultiplier));

    const tryPlay = () => {
      const attempt = audio.play();
      if (attempt && typeof attempt.catch === "function") attempt.catch(() => {});
    };

    // Attempt autoplay; if blocked, it will start on first user interaction.
    tryPlay();

    const onInteract = () => {
      tryPlay();
    };

    window.addEventListener("pointerdown", onInteract, { once: true });
    window.addEventListener("keydown", onInteract, { once: true });

    return () => {
      window.removeEventListener("pointerdown", onInteract);
      window.removeEventListener("keydown", onInteract);
    };
  }, [bgMusicVolume, bgMusicMode, bgMusicDuckMultiplier]);

  useEffect(() => {
    const audio = elevatorMusicRef.current;
    if (!audio) return undefined;

    audio.volume = 0.14;

    if (!isElevatorActive) {
      audio.pause();
      return undefined;
    }

    const tryPlay = () => {
      const attempt = audio.play();
      if (attempt && typeof attempt.catch === "function") attempt.catch(() => {});
    };

    tryPlay();

    const onInteract = () => {
      tryPlay();
    };

    window.addEventListener("pointerdown", onInteract, { once: true });
    window.addEventListener("keydown", onInteract, { once: true });

    return () => {
      window.removeEventListener("pointerdown", onInteract);
      window.removeEventListener("keydown", onInteract);
    };
  }, [isElevatorActive]);

	  const isLoginScreens =
	    screen === "login" || screen === "credits" || screen === "gameInfo" || screen === "plano";

  const playNaviSfx = () => {
    const audio = naviSfxRef.current;
    if (!audio) return;
    audio.volume = isLoginScreens ? bgMusicVolume : 0.45;
    audio.currentTime = 0;
    const attempt = audio.play();
    if (attempt && typeof attempt.catch === "function") attempt.catch(() => {});
  };

  const playRoarSfx = () => {
    const audio = roarSfxRef.current;
    if (!audio) return;
    audio.volume = isLoginScreens ? bgMusicVolume : 0.7;
    audio.currentTime = 0;
    const attempt = audio.play();
    if (attempt && typeof attempt.catch === "function") attempt.catch(() => {});
  };

  const footerText =
    "Developed as part of an academic project. Original concept, design & layout: Jordi Casas.";

  let content = null;

	  if (screen === "login") {
	    content = (
	      <Login
	        onLoginSuccess={() => setScreen("plano")}
	        onGameInfo={() => setScreen("gameInfo")}
	        onCredits={() => {
	          if (naviSfxTimeoutRef.current) window.clearTimeout(naviSfxTimeoutRef.current);
	          naviSfxTimeoutRef.current = window.setTimeout(() => {
	            playNaviSfx();
          }, 1000);
          setScreen("credits");
        }}
      />
    );
  }

	  if (screen === "menu") {
	    content = (
	      <Menu
	        onDuckBgMusic={(multiplier) => {
	          const next = Number(multiplier);
	          if (!Number.isFinite(next)) return;
	          setBgMusicDuckMultiplier(Math.max(0, Math.min(1, next)));
	        }}
	        onNewGame={(difficulty) => {
	          setGameMode("new");
	          setGameDifficulty(difficulty);
	          setScreen("game");
	        }}
        onMistakesGame={() => {
          setGameMode("mistakes");
          setScreen("game");
        }}
        onStats={() => setScreen("stats")}
        onAdmin={() => setScreen("admin")}
        onBackToLogin={() => setScreen("login")}
      />
    );
  }

  if (screen === "game") {
    content = (
      <Game
        mode={gameMode}
        difficulty={gameDifficulty}
        onFinish={(result) => {
          setGameResult(result ?? null);
          setScreen("result");
        }}
        onBack={() => setScreen("menu")}
      />
    );
  }

  if (screen === "result") {
    content = (
      <Result
        mode={gameMode}
        result={gameResult}
        onRestart={() => {
          setGameResult(null);
          setScreen("menu");
        }}
      />
    );
  }

  if (screen === "stats") {
    content = (
      <Stats onBack={() => setScreen("menu")} onBackToLogin={() => setScreen("login")} />
    );
  }

  if (screen === "admin") {
    content = <Admin onBack={() => setScreen("menu")} />;
  }

	  if (screen === "credits") {
	    content = (
	      <Credits
	        onBack={() => {
	          playRoarSfx();
	          setScreen("login");
	        }}
	      />
	    );
	  }

	  if (screen === "plano") {
	    content = (
	      <PlanoSplash
	        onDone={() => setScreen("menu")}
	        onBack={() => {
	          clearToken();
	          setScreen("login");
	        }}
	      />
	    );
	  }

	  if (screen === "gameInfo") {
	    content = <GameInfo onBack={() => setScreen("login")} />;
	  }

  if (!content) return null;

  return (
    <>
      <audio ref={bgMusicRef} src={bgMusicUrl} preload="auto" loop aria-hidden="true" />
      <audio
        ref={elevatorMusicRef}
        src={elevatorMusicUrl}
        preload="auto"
        loop
        aria-hidden="true"
      />
      <audio ref={naviSfxRef} src={naviSfxUrl} preload="auto" aria-hidden="true" />
      <audio ref={roarSfxRef} src={roarSfxUrl} preload="auto" aria-hidden="true" />
      {content}
      <footer className="page-footer">{footerText}</footer>
    </>
  );
}

export default App;
