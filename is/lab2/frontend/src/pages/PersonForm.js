import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { personApi, locationApi } from '../services/api';

function PersonForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [locations, setLocations] = useState([]);
  const [error, setError] = useState('');
  const [person, setPerson] = useState({
    name: '',
    coordinates: { x: 0, y: 0 },
    eyeColor: 'BLUE',
    hairColor: '',
    height: '',
    weight: '',
    passportID: '',
    nationality: '',
    location: null
  });

  const loadLocations = useCallback(async () => {
    try {
      const response = await locationApi.getDistinct();
      setLocations(response.data);
    } catch (err) {
      console.error('Error loading locations');
    }
  }, []);

  const loadPerson = useCallback(async () => {
    try {
      const response = await personApi.getById(id);
      const data = response.data;
      setPerson({
        ...data,
        hairColor: data.hairColor || '',
        height: data.height || '',
        weight: data.weight || '',
        passportID: data.passportID || '',
        nationality: data.nationality || '',
        location: data.location || null
      });
    } catch (err) {
      setError('Ошибка загрузки данных');
    }
  }, [id]);

  useEffect(() => {
    loadLocations();
    if (id) {
      loadPerson();
    }
  }, [id, loadLocations, loadPerson]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (person.weight && person.weight > 2147483647) {
      setError('Вес не может превышать 2,147,483,647 кг');
      return;
    }
    
    try {
      const cleanedPerson = {
        ...person,
        hairColor: person.hairColor === '' ? null : person.hairColor,
        height: person.height === '' ? null : person.height,
        weight: person.weight === '' ? null : person.weight,
        passportID: person.passportID === '' ? null : person.passportID,
        nationality: person.nationality === '' ? null : person.nationality,
        location: person.location?.id ? { id: person.location.id } : null
      };
      
      if (id) {
        await personApi.update(id, cleanedPerson);
      } else {
        await personApi.create(cleanedPerson);
      }
      navigate('/persons');
    } catch (err) {
      const errorMsg = err.response?.data?.error || err.response?.data?.message || err.message;
      if (errorMsg && errorMsg.includes('out of range')) {
        setError('Значение слишком большое. Вес не может превышать 2,147,483,647 кг');
      } else {
        setError(errorMsg || 'Ошибка сохранения');
      }
    }
  };

  const handleChange = (field, value) => {
    if (field.includes('.')) {
      const [parent, child] = field.split('.');
      setPerson({
        ...person,
        [parent]: { ...person[parent], [child]: value }
      });
    } else {
      setPerson({ ...person, [field]: value });
    }
  };

  return (
    <div className="card">
      <h2>{id ? 'Редактировать' : 'Добавить'} человека</h2>
      
      {error && <div className="error">{error}</div>}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Имя *</label>
          <input 
            required
            value={person.name} 
            onChange={(e) => handleChange('name', e.target.value)}
          />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
          <div className="form-group">
            <label>Координата X * (макс. 454)</label>
            <input 
              type="number" 
              required
              max="454"
              step="0.01"
              value={person.coordinates.x} 
              onChange={(e) => handleChange('coordinates.x', parseFloat(e.target.value))}
            />
          </div>

          <div className="form-group">
            <label>Координата Y * (макс. 698)</label>
            <input 
              type="number" 
              required
              max="698"
              value={person.coordinates.y} 
              onChange={(e) => handleChange('coordinates.y', parseInt(e.target.value))}
            />
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
          <div className="form-group">
            <label>Цвет глаз *</label>
            <select 
              required
              value={person.eyeColor} 
              onChange={(e) => handleChange('eyeColor', e.target.value)}
            >
              <option value="GREEN">GREEN</option>
              <option value="BLACK">BLACK</option>
              <option value="BLUE">BLUE</option>
              <option value="WHITE">WHITE</option>
              <option value="BROWN">BROWN</option>
            </select>
          </div>

          <div className="form-group">
            <label>Цвет волос</label>
            <select 
              value={person.hairColor} 
              onChange={(e) => handleChange('hairColor', e.target.value)}
            >
              <option value="">Не указан</option>
              <option value="GREEN">GREEN</option>
              <option value="BLACK">BLACK</option>
              <option value="BLUE">BLUE</option>
              <option value="WHITE">WHITE</option>
              <option value="BROWN">BROWN</option>
            </select>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
          <div className="form-group">
            <label>Рост (см)</label>
            <input 
              type="number" 
              min="1"
              value={person.height} 
              onChange={(e) => handleChange('height', e.target.value ? parseInt(e.target.value) : '')}
            />
          </div>

          <div className="form-group">
            <label>Вес (кг)</label>
            <input 
              type="number" 
              min="1"
              max="2147483647"
              value={person.weight} 
              onChange={(e) => handleChange('weight', e.target.value ? parseInt(e.target.value) : '')}
            />
            <small style={{ 
              display: 'block', 
              marginTop: '0.25rem', 
              fontSize: '0.75rem', 
              color: '#888' 
            }}>
              Максимум: 2,147,483,647
            </small>
          </div>
        </div>

        <div className="form-group">
          <label>Паспорт ID</label>
          <input 
            value={person.passportID} 
            onChange={(e) => handleChange('passportID', e.target.value)}
          />
        </div>

        <div className="form-group">
          <label>Национальность</label>
          <select 
            value={person.nationality} 
            onChange={(e) => handleChange('nationality', e.target.value)}
          >
            <option value="">Не указана</option>
            <option value="GERMANY">GERMANY</option>
            <option value="FRANCE">FRANCE</option>
            <option value="SPAIN">SPAIN</option>
            <option value="VATICAN">VATICAN</option>
          </select>
        </div>

        <div className="form-group">
          <label>Локация</label>
          <select 
            value={person.location?.id || ''} 
            onChange={(e) => {
              const loc = locations.find(l => l.id === parseInt(e.target.value));
              handleChange('location', loc || null);
            }}
          >
            <option value="">Не указана</option>
            {locations.map(loc => (
              <option key={loc.id} value={loc.id}>
                {loc.name || `Location ${loc.id}`} ({loc.x}, {loc.y}, {loc.z})
              </option>
            ))}
          </select>
        </div>

        <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
          <button type="submit" className="btn btn-success">Сохранить</button>
          <button type="button" onClick={() => navigate('/persons')} className="btn">Отмена</button>
        </div>
      </form>
    </div>
  );
}

export default PersonForm;

