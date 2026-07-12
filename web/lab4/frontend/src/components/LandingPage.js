// src/components/LandingPage.js
import React from 'react';
import { useNavigate } from 'react-router-dom';
import Header from './Header';
import { Button } from 'belle';

const LandingPage = () => {
  const navigate = useNavigate();

  const handleEnter = () => {
    navigate('/main');
  };

  return (
    <div className="landing-page">
      <Header />
        <div className="landing-page-content">
      <main>
        <img src="/meowmurr.gif" alt="Gengar GIF" className="anime-gif" />
        <h1>Welcome to PointGame! 🎯</h1>
        <p>Your shots have been plotted — green for hits, red for misses. <br />
Analyze your accuracy, refine your strategy, and aim for perfection!<br />
Can you master the diagram and hit every target? 🚀</p>
        <Button onClick={handleEnter}>Let's play!</Button>
      </main>
      </div>
    </div>
  );
};

export default LandingPage;