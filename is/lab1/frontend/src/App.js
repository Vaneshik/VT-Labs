import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import PersonList from './pages/PersonList';
import PersonForm from './pages/PersonForm';
import Operations from './pages/Operations';
import Home from './pages/Home';

function App() {
  return (
    <Router>
      <div style={{ minHeight: '100vh' }}>
        <nav style={{
          background: 'rgba(255, 255, 255, 0.05)',
          backdropFilter: 'blur(10px)',
          padding: '1rem 2rem',
          boxShadow: '0 8px 32px rgba(31, 38, 135, 0.37)',
          borderBottom: '1px solid rgba(255, 105, 180, 0.3)',
          position: 'sticky',
          top: 0,
          zIndex: 100
        }}>
          <div style={{ 
            maxWidth: '1200px', 
            margin: '0 auto',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexWrap: 'wrap',
            gap: '1rem'
          }}>
            <h1 style={{ 
              background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              backgroundClip: 'text',
              fontSize: 'clamp(1.2rem, 4vw, 2rem)',
              margin: 0,
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem'
            }}>
              ✨ Anime Waifu Database ✨
            </h1>
            <div style={{ 
              display: 'flex', 
              gap: '0.5rem',
              flexWrap: 'wrap'
            }}>
              <Link to="/" style={{ textDecoration: 'none' }}>
                <button>🏠 Домой</button>
              </Link>
              <Link to="/persons" style={{ textDecoration: 'none' }}>
                <button>👧 Список</button>
              </Link>
              <Link to="/persons/new" style={{ textDecoration: 'none' }}>
                <button>➕ Добавить</button>
              </Link>
              <Link to="/operations" style={{ textDecoration: 'none' }}>
                <button>⚡ Операции</button>
              </Link>
            </div>
          </div>
        </nav>

        <div style={{ 
          maxWidth: '1200px', 
          margin: '2rem auto',
          padding: '0 1rem'
        }}>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/persons" element={<PersonList />} />
            <Route path="/persons/new" element={<PersonForm />} />
            <Route path="/persons/:id" element={<PersonForm />} />
            <Route path="/operations" element={<Operations />} />
          </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App;

