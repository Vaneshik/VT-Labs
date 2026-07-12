import axios from 'axios';
import { API_BASE_URL } from '../config';

const API_BASE = API_BASE_URL;

export const personApi = {
  getAll: (page = 0, size = 10, name = '', nationality = '', sortBy = '') => {
    let url = `${API_BASE}/persons?page=${page}&size=${size}`;
    if (name) url += `&name=${name}`;
    if (nationality) url += `&nationality=${nationality}`;
    if (sortBy) url += `&sortBy=${sortBy}`;
    return axios.get(url);
  },
  
  getById: (id) => axios.get(`${API_BASE}/persons/${id}`),
  
  create: (person) => axios.post(`${API_BASE}/persons`, person),
  
  update: (id, person) => axios.put(`${API_BASE}/persons/${id}`, person),
  
  delete: (id) => axios.delete(`${API_BASE}/persons/${id}`)
};

export const locationApi = {
  getAll: () => axios.get(`${API_BASE}/locations`),
  
  getDistinct: () => axios.get(`${API_BASE}/locations/distinct`),
  
  create: (location) => axios.post(`${API_BASE}/locations`, location)
};

export const operationsApi = {
  deleteByNationality: (nationality) => 
    axios.delete(`${API_BASE}/persons/by-nationality?nationality=${nationality}`),
  
  getAverageHeight: () => 
    axios.get(`${API_BASE}/persons/statistics/average-height`),
  
  getUniqueNationalities: () => 
    axios.get(`${API_BASE}/persons/nationalities`),
  
  getHairColorPercentage: (color) => 
    axios.get(`${API_BASE}/persons/statistics/hair-color-percentage?color=${color}`),
  
  countByLocation: (color, locationId) => {
    let url = `${API_BASE}/persons/count?hairColor=${color}`;
    if (locationId) {
      url += `&locationId=${locationId}`;
    }
    return axios.get(url);
  }
};

export const importApi = {
  importPersons: (jsonData, username) => 
    axios.post(`${API_BASE}/import/persons?username=${username}`, jsonData, {
      headers: {
        'Content-Type': 'application/json'
      }
    }),
  
  getImportHistory: (username) => {
    let url = `${API_BASE}/import/history`;
    if (username) {
      url += `?username=${username}`;
    }
    return axios.get(url);
  },
  
  getImportHistoryById: (id) => 
    axios.get(`${API_BASE}/import/history/${id}`)
};

