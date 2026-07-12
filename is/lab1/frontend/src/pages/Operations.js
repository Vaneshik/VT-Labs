import React, { useState, useEffect } from 'react';
import { operationsApi, locationApi } from '../services/api';

function Operations() {
  const [locations, setLocations] = useState([]);
  const [results, setResults] = useState({});
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    loadLocations();
  }, []);

  const loadLocations = async () => {
    try {
      const response = await locationApi.getDistinct();
      setLocations(response.data);
    } catch (err) {
      console.error('Error loading locations');
    }
  };

  const handleDeleteByNationality = async (e) => {
    e.preventDefault();
    const nationality = e.target.nationality.value;
    if (!nationality) {
      setError('Выберите национальность');
      return;
    }
    
    if (window.confirm(`Удалить всех людей с национальностью ${nationality}?`)) {
      try {
        await operationsApi.deleteByNationality(nationality);
        setSuccess('Удалено успешно');
        setError('');
      } catch (err) {
        const errorMessage = err.response?.data?.error || 'Ошибка удаления';
        setError(errorMessage);
        setSuccess('');
      }
    }
  };

  const handleAverageHeight = async () => {
    try {
      const response = await operationsApi.getAverageHeight();
      setResults({...results, avgHeight: response.data.averageHeight});
      setSuccess('Рассчитано успешно');
      setError('');
    } catch (err) {
      const errorMessage = err.response?.data?.error || 'Ошибка расчета среднего роста';
      setError(errorMessage);
      setSuccess('');
    }
  };

  const handleUniqueNationalities = async () => {
    try {
      const response = await operationsApi.getUniqueNationalities();
      setResults({...results, nationalities: response.data});
      setSuccess('Список национальностей получен');
      setError('');
    } catch (err) {
      const errorMessage = err.response?.data?.error || 'Ошибка получения данных';
      setError(errorMessage);
      setSuccess('');
    }
  };

  const handleHairColorPercentage = async (e) => {
    e.preventDefault();
    const color = e.target.color.value;
    if (!color) {
      setError('Выберите цвет волос');
      return;
    }

    try {
      const response = await operationsApi.getHairColorPercentage(color);
      setResults({...results, percentage: response.data.percentage});
      setSuccess('Процент рассчитан');
      setError('');
    } catch (err) {
      const errorMessage = err.response?.data?.error || 'Ошибка расчета процента';
      setError(errorMessage);
      setSuccess('');
    }
  };

  const handleCountByLocation = async (e) => {
    e.preventDefault();
    const color = e.target.color.value;
    const locationId = e.target.location.value;
    if (!color) {
      setError('Выберите цвет волос');
      return;
    }

    try {
      const response = await operationsApi.countByLocation(color, locationId);
      setResults({...results, count: response.data.count});
      setSuccess('Подсчет выполнен');
      setError('');
    } catch (err) {
      const errorMessage = err.response?.data?.error || 'Ошибка подсчета';
      setError(errorMessage);
      setSuccess('');
    }
  };

  return (
    <div style={{ padding: 'clamp(0.5rem, 2vw, 1rem)' }}>
      <div className="card" style={{ 
        marginBottom: '1.5rem',
        padding: 'clamp(1rem, 3vw, 2rem)'
      }}>
        <h2 style={{ 
          fontSize: 'clamp(1.5rem, 4vw, 2.5rem)',
          marginBottom: '1.5rem'
        }}>
          Специальные операции
        </h2>
        
        {error && (
          <div style={{
            background: 'rgba(244, 67, 54, 0.2)',
            border: '2px solid #f44336',
            color: '#ff6b6b',
            padding: '1rem',
            borderRadius: '10px',
            marginBottom: '1.5rem'
          }}>
            {error}
          </div>
        )}
        
        {success && (
          <div style={{
            background: 'rgba(76, 175, 80, 0.2)',
            border: '2px solid #4CAF50',
            color: '#4CAF50',
            padding: '1rem',
            borderRadius: '10px',
            marginBottom: '1.5rem'
          }}>
            {success}
          </div>
        )}

        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))',
          gap: '1.5rem'
        }}>
          
          <div className="card" style={{
            padding: '1.5rem',
            background: 'rgba(255, 255, 255, 0.03)',
            border: '1px solid rgba(255, 105, 180, 0.2)',
            marginBottom: 0
          }}>
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.75rem',
              marginBottom: '1rem',
              paddingBottom: '0.75rem',
              borderBottom: '1px solid rgba(255, 255, 255, 0.1)'
            }}>
              <h3 style={{ margin: 0, fontSize: '1.15rem' }}>Удалить по национальности</h3>
            </div>
            <form onSubmit={handleDeleteByNationality}>
              <div className="form-group" style={{ marginBottom: '1rem' }}>
                <label style={{
                  display: 'block',
                  marginBottom: '0.5rem',
                  fontSize: '0.9rem',
                  color: '#ffc0cb'
                }}>
                  Национальность
                </label>
                <select name="nationality" style={{ width: '100%' }}>
                  <option value="">Выберите...</option>
                  <option value="GERMANY">GERMANY</option>
                  <option value="FRANCE">FRANCE</option>
                  <option value="SPAIN">SPAIN</option>
                  <option value="VATICAN">VATICAN</option>
                </select>
              </div>
              <button 
                type="submit" 
                style={{
                  width: '100%',
                  background: 'linear-gradient(135deg, #f5576c 0%, #f093fb 100%)'
                }}
              >
                Удалить всех
              </button>
            </form>
          </div>

          <div className="card" style={{
            padding: '1.5rem',
            background: 'rgba(255, 255, 255, 0.03)',
            border: '1px solid rgba(255, 105, 180, 0.2)',
            marginBottom: 0
          }}>
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.75rem',
              marginBottom: '1rem',
              paddingBottom: '0.75rem',
              borderBottom: '1px solid rgba(255, 255, 255, 0.1)'
            }}>
              <h3 style={{ margin: 0, fontSize: '1.15rem' }}>Среднее значение роста</h3>
            </div>
            <button 
              onClick={handleAverageHeight}
              style={{ width: '100%', marginBottom: '1rem' }}
            >
              Рассчитать
            </button>
            {results.avgHeight !== undefined && (
              <div style={{
                padding: '1rem',
                background: 'rgba(255, 192, 203, 0.1)',
                borderRadius: '10px',
                textAlign: 'center'
              }}>
                <div style={{ 
                  fontSize: '0.85rem', 
                  color: '#888',
                  marginBottom: '0.25rem'
                }}>
                  Результат
                </div>
                <div style={{ 
                  fontSize: '1.5rem',
                  fontWeight: 'bold',
                  color: '#ffc0cb'
                }}>
                  {results.avgHeight.toFixed(2)} см
                </div>
              </div>
            )}
          </div>

          <div className="card" style={{
            padding: '1.5rem',
            background: 'rgba(255, 255, 255, 0.03)',
            border: '1px solid rgba(255, 105, 180, 0.2)',
            marginBottom: 0
          }}>
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.75rem',
              marginBottom: '1rem',
              paddingBottom: '0.75rem',
              borderBottom: '1px solid rgba(255, 255, 255, 0.1)'
            }}>
              <h3 style={{ margin: 0, fontSize: '1.15rem' }}>Уникальные национальности</h3>
            </div>
            <button 
              onClick={handleUniqueNationalities}
              style={{ width: '100%', marginBottom: '1rem' }}
            >
              Получить список
            </button>
            {results.nationalities && (
              <div style={{
                padding: '1rem',
                background: 'rgba(255, 192, 203, 0.1)',
                borderRadius: '10px'
              }}>
                <div style={{ 
                  fontSize: '0.85rem', 
                  color: '#888',
                  marginBottom: '0.5rem'
                }}>
                  Найдено национальностей: {results.nationalities.length}
                </div>
                <ul style={{
                  listStyle: 'none',
                  padding: 0,
                  margin: 0,
                  display: 'flex',
                  flexWrap: 'wrap',
                  gap: '0.5rem'
                }}>
                  {results.nationalities.map((nat, idx) => (
                    <li 
                      key={idx}
                      style={{
                        background: 'rgba(255, 105, 180, 0.2)',
                        padding: '0.5rem 1rem',
                        borderRadius: '15px',
                        fontSize: '0.9rem'
                      }}
                    >
                      {nat}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>

          <div className="card" style={{
            padding: '1.5rem',
            background: 'rgba(255, 255, 255, 0.03)',
            border: '1px solid rgba(255, 105, 180, 0.2)',
            marginBottom: 0
          }}>
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.75rem',
              marginBottom: '1rem',
              paddingBottom: '0.75rem',
              borderBottom: '1px solid rgba(255, 255, 255, 0.1)'
            }}>
              <h3 style={{ margin: 0, fontSize: '1.15rem' }}>Доля по цвету волос</h3>
            </div>
            <form onSubmit={handleHairColorPercentage}>
              <div className="form-group" style={{ marginBottom: '1rem' }}>
                <label style={{
                  display: 'block',
                  marginBottom: '0.5rem',
                  fontSize: '0.9rem',
                  color: '#ffc0cb'
                }}>
                  Цвет волос
                </label>
                <select name="color" style={{ width: '100%' }}>
                  <option value="">Выберите...</option>
                  <option value="GREEN">GREEN</option>
                  <option value="BLACK">BLACK</option>
                  <option value="BLUE">BLUE</option>
                  <option value="WHITE">WHITE</option>
                  <option value="BROWN">BROWN</option>
                </select>
              </div>
              <button type="submit" style={{ width: '100%' }}>
                Рассчитать %
              </button>
            </form>
            {results.percentage !== undefined && (
              <div style={{
                padding: '1rem',
                background: 'rgba(255, 192, 203, 0.1)',
                borderRadius: '10px',
                textAlign: 'center',
                marginTop: '1rem'
              }}>
                <div style={{ 
                  fontSize: '0.85rem', 
                  color: '#888',
                  marginBottom: '0.25rem'
                }}>
                  Результат
                </div>
                <div style={{ 
                  fontSize: '1.5rem',
                  fontWeight: 'bold',
                  color: '#ffc0cb'
                }}>
                  {results.percentage.toFixed(2)}%
                </div>
              </div>
            )}
          </div>

          <div className="card" style={{
            padding: '1.5rem',
            background: 'rgba(255, 255, 255, 0.03)',
            border: '1px solid rgba(255, 105, 180, 0.2)',
            marginBottom: 0
          }}>
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.75rem',
              marginBottom: '1rem',
              paddingBottom: '0.75rem',
              borderBottom: '1px solid rgba(255, 255, 255, 0.1)'
            }}>
              <h3 style={{ margin: 0, fontSize: '1.15rem' }}>По цвету и локации</h3>
            </div>
            <form onSubmit={handleCountByLocation}>
              <div className="form-group" style={{ marginBottom: '1rem' }}>
                <label style={{
                  display: 'block',
                  marginBottom: '0.5rem',
                  fontSize: '0.9rem',
                  color: '#ffc0cb'
                }}>
                  Цвет волос
                </label>
                <select name="color" style={{ width: '100%' }}>
                  <option value="">Выберите...</option>
                  <option value="GREEN">GREEN</option>
                  <option value="BLACK">BLACK</option>
                  <option value="BLUE">BLUE</option>
                  <option value="WHITE">WHITE</option>
                  <option value="BROWN">BROWN</option>
                </select>
              </div>
              <div className="form-group" style={{ marginBottom: '1rem' }}>
                <label style={{
                  display: 'block',
                  marginBottom: '0.5rem',
                  fontSize: '0.9rem',
                  color: '#ffc0cb'
                }}>
                  Локация
                </label>
                <select name="location" style={{ width: '100%' }}>
                  <option value="">Все локации</option>
                  {locations.map(loc => (
                    <option key={loc.id} value={loc.id}>
                      {loc.name || `Location ${loc.id}`}
                    </option>
                  ))}
                </select>
              </div>
              <button type="submit" style={{ width: '100%' }}>
                Подсчитать
              </button>
            </form>
            {results.count !== undefined && (
              <div style={{
                padding: '1rem',
                background: 'rgba(255, 192, 203, 0.1)',
                borderRadius: '10px',
                textAlign: 'center',
                marginTop: '1rem'
              }}>
                <div style={{ 
                  fontSize: '0.85rem', 
                  color: '#888',
                  marginBottom: '0.25rem'
                }}>
                  Найдено
                </div>
                <div style={{ 
                  fontSize: '1.5rem',
                  fontWeight: 'bold',
                  color: '#ffc0cb'
                }}>
                  {results.count}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default Operations;

