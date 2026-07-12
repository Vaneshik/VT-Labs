const BACKEND_HOST = process.env.REACT_APP_BACKEND_HOST || window.location.hostname;
const BACKEND_PORT = process.env.REACT_APP_BACKEND_PORT || window.location.port || '80';
const BACKEND_PATH = process.env.REACT_APP_BACKEND_PATH || '/person-management-system/api';

export const API_BASE_URL = BACKEND_PORT === '80' || BACKEND_PORT === '' 
    ? `http://${BACKEND_HOST}${BACKEND_PATH}`
    : `http://${BACKEND_HOST}:${BACKEND_PORT}${BACKEND_PATH}`;

    export const WS_BASE_URL = BACKEND_PORT === '80' || BACKEND_PORT === ''
    ? `ws://${BACKEND_HOST}/person-management-system/ws/persons`
    : `ws://${BACKEND_HOST}:${BACKEND_PORT}/person-management-system/ws/persons`;
