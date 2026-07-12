import React, { useState, useEffect } from 'react';
import { importApi } from '../services/api';

function Import() {
  const [file, setFile] = useState(null);
  const [username, setUsername] = useState('user1');
  const [history, setHistory] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  useEffect(() => {
    loadHistory();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadHistory = async () => {
    try {
      const response = await importApi.getImportHistory();
      setHistory(response.data);
    } catch (err) {
      console.error('Error loading history:', err);
    }
  };

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    if (selectedFile) {
      if (selectedFile.type === 'application/json') {
        setFile(selectedFile);
        setError('');
      } else {
        setError('Пожалуйста, выберите JSON файл');
        setFile(null);
      }
    }
  };

  const handleImport = async (e) => {
    e.preventDefault();
    
    if (!file) {
      setError('Выберите файл для импорта');
      return;
    }

    if (!username.trim()) {
      setError('Введите имя пользователя');
      return;
    }

    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const fileContent = await file.text();
      const jsonData = JSON.parse(fileContent);

      const response = await importApi.importPersons(jsonData, username);
      
      if (response.data.status === 'SUCCESS') {
        setSuccess(`Успешно импортировано ${response.data.importedCount} объектов!`);
        setFile(null);
        document.getElementById('fileInput').value = '';
      } else {
        setError(`Импорт завершился с ошибкой: ${response.data.errorMessage}`);
      }
      loadHistory();
    } catch (err) {
      const data = err.response?.data;
      const errorMessage = data?.errorMessage || data?.error || err.message || 'Ошибка импорта';
      setError(errorMessage);
      loadHistory();
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleString('ru-RU');
  };

  const getStatusColor = (status) => {
    return status === 'SUCCESS' ? '#4CAF50' : '#f44336';
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
          Импорт данных
        </h2>

        {error && (
          <div style={{
            background: 'rgba(244, 67, 54, 0.2)',
            border: '2px solid #f44336',
            color: '#ff6b6b',
            padding: '1rem',
            borderRadius: '10px',
            marginBottom: '1.5rem',
            wordBreak: 'break-word'
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

        <form onSubmit={handleImport}>
          <div style={{
            display: 'grid',
            gap: '1.5rem',
            marginBottom: '1.5rem'
          }}>
            <div>
              <label style={{
                display: 'block',
                marginBottom: '0.5rem',
                fontSize: '0.9rem',
                color: '#ffc0cb'
              }}>
                Имя пользователя
              </label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="user1"
                style={{ width: '100%' }}
                required
              />
            </div>

            <div>
              <label style={{
                display: 'block',
                marginBottom: '0.5rem',
                fontSize: '0.9rem',
                color: '#ffc0cb'
              }}>
                JSON файл для импорта
              </label>
              <input
                id="fileInput"
                type="file"
                accept=".json,application/json"
                onChange={handleFileChange}
                style={{
                  width: '100%',
                  padding: '0.75rem',
                  background: 'rgba(255, 255, 255, 0.05)',
                  border: '2px solid rgba(255, 105, 180, 0.3)',
                  borderRadius: '10px',
                  color: '#fff',
                  cursor: 'pointer'
                }}
                required
              />
              {file && (
                <div style={{
                  marginTop: '0.5rem',
                  fontSize: '0.85rem',
                  color: '#4CAF50'
                }}>
                  Выбран файл: {file.name}
                </div>
              )}
            </div>
          </div>

          <button 
            type="submit" 
            disabled={loading || !file}
            style={{
              width: '100%',
              background: loading ? 'rgba(128, 128, 128, 0.3)' : 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
              cursor: loading ? 'not-allowed' : 'pointer',
              opacity: loading || !file ? 0.6 : 1
            }}
          >
            {loading ? 'Импорт...' : 'Импортировать'}
          </button>
        </form>

        <div style={{
          marginTop: '1.5rem',
          padding: '1rem',
          background: 'rgba(255, 192, 203, 0.1)',
          borderRadius: '10px',
          fontSize: '0.85rem',
          lineHeight: '1.6'
        }}>
          <div style={{ fontWeight: 'bold', marginBottom: '0.5rem' }}>
            Формат JSON файла:
          </div>
          <pre style={{
            background: 'rgba(0, 0, 0, 0.3)',
            padding: '0.75rem',
            borderRadius: '5px',
            overflow: 'auto',
            fontSize: '0.75rem'
          }}>
{`[
  {
    "name": "Test Person",
    "coordinates": {"x": 100.5, "y": 200},
    "eyeColor": "BLUE",
    "hairColor": "BLACK",
    "location": {
      "x": 10, "y": 20.5, 
      "z": 30, "name": "Tokyo"
    },
    "height": 175,
    "weight": 70,
    "passportID": "JP1234567",
    "nationality": "GERMANY"
  }
]`}
          </pre>
        </div>
      </div>

      <div className="card" style={{ 
        padding: 'clamp(1rem, 3vw, 2rem)'
      }}>
        <h2 style={{ 
          fontSize: 'clamp(1.5rem, 4vw, 2.5rem)',
          margin: '0 0 1.5rem 0'
        }}>
          История импорта
        </h2>

        {history.length === 0 ? (
          <div style={{
            textAlign: 'center',
            padding: '2rem',
            color: '#888',
            background: 'rgba(255, 255, 255, 0.03)',
            borderRadius: '15px',
            border: '1px dashed rgba(255, 105, 180, 0.2)'
          }}>
            История импорта пуста
          </div>
        ) : (
          <div style={{ 
            overflowX: 'auto',
            borderRadius: '15px',
            overflow: 'hidden',
            border: '1px solid rgba(255, 105, 180, 0.2)',
            boxShadow: '0 4px 20px rgba(255, 105, 180, 0.1)'
          }}>
            <table className="import-history-table" style={{
              width: '100%',
              borderCollapse: 'separate',
              borderSpacing: 0
            }}>
              <thead>
                <tr>
                  <th style={{ 
                    padding: '1rem 1.25rem', 
                    textAlign: 'left',
                    background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
                    color: 'white',
                    fontWeight: 600,
                    fontSize: '0.95rem'
                  }}>ID</th>
                  <th style={{ 
                    padding: '1rem 1.25rem', 
                    textAlign: 'left',
                    background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
                    color: 'white',
                    fontWeight: 600,
                    fontSize: '0.95rem'
                  }}>Статус</th>
                  <th style={{ 
                    padding: '1rem 1.25rem', 
                    textAlign: 'left',
                    background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
                    color: 'white',
                    fontWeight: 600,
                    fontSize: '0.95rem'
                  }}>Пользователь</th>
                  <th style={{ 
                    padding: '1rem 1.25rem', 
                    textAlign: 'left',
                    background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
                    color: 'white',
                    fontWeight: 600,
                    fontSize: '0.95rem'
                  }}>Время</th>
                  <th style={{ 
                    padding: '1rem 1.25rem', 
                    textAlign: 'center',
                    background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
                    color: 'white',
                    fontWeight: 600,
                    fontSize: '0.95rem'
                  }}>Объектов</th>
                  <th style={{ 
                    padding: '1rem 1.25rem', 
                    textAlign: 'left',
                    background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
                    color: 'white',
                    fontWeight: 600,
                    fontSize: '0.95rem'
                  }}>Ошибка</th>
                </tr>
              </thead>
              <tbody>
                {history.map((item, idx) => (
                  <tr 
                    key={item.id}
                    style={{
                      background: idx % 2 === 0 ? 'rgba(255, 255, 255, 0.03)' : 'rgba(255, 255, 255, 0.02)'
                    }}
                  >
                    <td style={{ 
                      padding: '1rem 1.25rem',
                      borderBottom: '1px solid rgba(255, 255, 255, 0.06)',
                      color: '#ffc0cb',
                      fontWeight: 500
                    }}>{item.id}</td>
                    <td style={{ 
                      padding: '1rem 1.25rem',
                      borderBottom: '1px solid rgba(255, 255, 255, 0.06)'
                    }}>
                      <span style={{
                        padding: '0.35rem 0.9rem',
                        borderRadius: '20px',
                        background: `${getStatusColor(item.status)}22`,
                        color: getStatusColor(item.status),
                        fontSize: '0.85rem',
                        fontWeight: 600,
                        border: `1px solid ${getStatusColor(item.status)}55`,
                        display: 'inline-block'
                      }}>
                        {item.status}
                      </span>
                    </td>
                    <td style={{ 
                      padding: '1rem 1.25rem',
                      borderBottom: '1px solid rgba(255, 255, 255, 0.06)',
                      color: '#f0f0f0'
                    }}>{item.username || 'N/A'}</td>
                    <td style={{ 
                      padding: '1rem 1.25rem',
                      borderBottom: '1px solid rgba(255, 255, 255, 0.06)',
                      fontSize: '0.9rem',
                      color: '#ccc'
                    }}>
                      {formatDate(item.createdAt)}
                    </td>
                    <td style={{ 
                      padding: '1rem 1.25rem',
                      borderBottom: '1px solid rgba(255, 255, 255, 0.06)',
                      textAlign: 'center',
                      color: '#ffc0cb',
                      fontWeight: 500
                    }}>
                      {item.importedCount !== null ? item.importedCount : '—'}
                    </td>
                    <td style={{ 
                      padding: '1rem 1.25rem',
                      borderBottom: '1px solid rgba(255, 255, 255, 0.06)',
                      fontSize: '0.85rem',
                      color: item.errorMessage ? '#ff8a80' : '#666',
                      maxWidth: '280px',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap'
                    }}>
                      {item.errorMessage || '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

export default Import;
