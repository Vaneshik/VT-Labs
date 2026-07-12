import React from 'react';
import { Link } from 'react-router-dom';

function Home() {
  return (
    <div style={{ 
      textAlign: 'center',
      padding: '2rem'
    }}>
      <div className="card" style={{
        maxWidth: '800px',
        margin: '0 auto',
        padding: '2rem'
      }}>
        <h1 style={{ 
          fontSize: 'clamp(2rem, 6vw, 3rem)',
          marginBottom: '1rem'
        }}>
          Person Management System
        </h1>
        
        <p style={{ 
          fontSize: '1rem',
          color: '#ffc0cb',
          marginBottom: '2rem'
        }}>
          Система управления персонами
        </p>

        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
          gap: '1rem',
          marginTop: '2rem'
        }}>
          <Link to="/persons" style={{ textDecoration: 'none' }}>
            <div className="card" style={{
              padding: '1.5rem',
              cursor: 'pointer',
              textAlign: 'center'
            }}>
              <div style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>👥</div>
              <h3 style={{ margin: '0.5rem 0' }}>Список персон</h3>
              <p style={{ fontSize: '0.9rem', color: '#999' }}>
                Просмотр всех записей
              </p>
            </div>
          </Link>

          <Link to="/persons/new" style={{ textDecoration: 'none' }}>
            <div className="card" style={{
              padding: '1.5rem',
              cursor: 'pointer',
              textAlign: 'center'
            }}>
              <div style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>➕</div>
              <h3 style={{ margin: '0.5rem 0' }}>Добавить персону</h3>
              <p style={{ fontSize: '0.9rem', color: '#999' }}>
                Создать новую запись
              </p>
            </div>
          </Link>

          <Link to="/operations" style={{ textDecoration: 'none' }}>
            <div className="card" style={{
              padding: '1.5rem',
              cursor: 'pointer',
              textAlign: 'center'
            }}>
              <div style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>🔧</div>
              <h3 style={{ margin: '0.5rem 0' }}>Операции</h3>
              <p style={{ fontSize: '0.9rem', color: '#999' }}>
                Специальные функции
              </p>
            </div>
          </Link>
        </div>
      </div>
    </div>
  );
}

export default Home;
