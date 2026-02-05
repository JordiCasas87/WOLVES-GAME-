import { useState } from "react";
import "./App.css";
import Login from "./pages/Login";
import Menu from "./pages/Menu";
import Game from "./pages/Game";
import Result from "./pages/Result";
import Stats from "./pages/Stats";

function App() {
  const [screen, setScreen] = useState("login");
  const [gameDifficulty, setGameDifficulty] = useState("easy"); // easy | medium | hard

  const footerText =
    "Developed as part of an academic project. Original concept, design & layout: Jordi Casas.";

  let content = null;

  if (screen === "login") {
    content = <Login onLoginSuccess={() => setScreen("menu")} />;
  }

  if (screen === "menu") {
    content = (
      <Menu
        onNewGame={(difficulty) => {
          setGameDifficulty(difficulty);
          setScreen("game");
        }}
        onMistakesGame={() => setScreen("game")}
        onStats={() => setScreen("stats")}
        onBackToLogin={() => setScreen("login")}
      />
    );
  }

  if (screen === "game") {
    content = <Game difficulty={gameDifficulty} onFinish={() => setScreen("result")} />;
  }

  if (screen === "result") {
    content = <Result onRestart={() => setScreen("menu")} />;
  }

  if (screen === "stats") {
    content = (
      <Stats onBack={() => setScreen("menu")} onBackToLogin={() => setScreen("login")} />
    );
  }

  if (!content) return null;

  return (
    <>
      {content}
      <footer className="page-footer">{footerText}</footer>
    </>
  );
}

export default App;
