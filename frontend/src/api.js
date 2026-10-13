import axios from 'axios';

/**
 * Client HTTP de l'API REST. Le jeton JWT est ajouté automatiquement sur
 * chaque requête ; en cas de réponse 401, la session est réinitialisée.
 */
const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('pfa_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/**
 * Événement émis par l'intercepteur lorsque l'API répond 401 : le token est
 * retiré du stockage, mais l'état React doit aussi être réinitialisé, sinon
 * l'application continue d'afficher l'utilisateur comme connecté.
 */
export const EVT_SESSION_EXPIREE = 'pfa:session-expiree';

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const isLoginCall = error.config?.url?.includes('/auth/login');
    if (status === 401 && !isLoginCall) {
      const etaitConnecte = Boolean(localStorage.getItem('pfa_token'));
      localStorage.removeItem('pfa_token');
      localStorage.removeItem('pfa_user');
      if (etaitConnecte) {
        // Seul AuthContext sait « déconnecter » React : on l'avise.
        window.dispatchEvent(new CustomEvent(EVT_SESSION_EXPIREE));
      }
    }
    return Promise.reject(error);
  }
);

/** Extrait un message d'erreur exploitable depuis la réponse API. */
export function messageErreur(error) {
  const data = error.response?.data;
  if (data?.message) {
    if (Array.isArray(data.details) && data.details.length > 0) {
      return `${data.message} — ${data.details.join(' ; ')}`;
    }
    return data.message;
  }
  return error.message || 'Erreur inattendue.';
}

export default api;