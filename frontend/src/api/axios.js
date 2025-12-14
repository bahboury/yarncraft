import axios from 'axios';

// 1. Point to your Spring Boot Backend
const api = axios.create({
    baseURL: 'http://localhost:8080/api', // This matches your Java Controller paths
});

// 2. The Interceptor (The Magic)
// Automatically checks if we have a token in our pocket (localStorage)
// and attaches it to the request so the Backend lets us in.
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export default api;