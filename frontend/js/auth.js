// Client-side authentication manager
const AUTH_KEY = 'railpass_jwt_token';
const AUTH_MODE_KEY = 'railpass_auth_mode'; // 'session' or 'token'
const USER_KEY = 'railpass_user';

const API_BASE = ''; // Same host (Spring Boot static resource deployment)

const Auth = {
    getMode() {
        return localStorage.getItem(AUTH_MODE_KEY) || 'session';
    },
    
    setMode(mode) {
        localStorage.setItem(AUTH_MODE_KEY, mode);
    },
    
    getToken() {
        return localStorage.getItem(AUTH_KEY);
    },
    
    setToken(token) {
        localStorage.setItem(AUTH_KEY, token);
    },
    
    getUser() {
        const u = localStorage.getItem(USER_KEY);
        return u ? JSON.parse(u) : null;
    },
    
    setUser(user) {
        localStorage.setItem(USER_KEY, JSON.stringify(user));
    },
    
    clear() {
        localStorage.removeItem(AUTH_KEY);
        localStorage.removeItem(USER_KEY);
    },
    
    // Wrapper around standard fetch to handle Session and Token auth automatically
    async fetch(url, options = {}) {
        const fullUrl = url.startsWith('http') ? url : API_BASE + url;
        options.headers = options.headers || {};
        
        // Convert object body to JSON string
        if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
            options.headers['Content-Type'] = 'application/json';
            options.body = JSON.stringify(options.body);
        }
        
        // Emits CORS cookies for Session mode
        options.credentials = 'include';
        
        // Injects JWT token if Token Mode is active
        if (this.getMode() === 'token') {
            const token = this.getToken();
            if (token) {
                options.headers['Authorization'] = `Bearer ${token}`;
            }
        }
        
        const response = await fetch(fullUrl, options);
        
        if (response.status === 401) {
            // Auto logout on token expiration or unauthorized access
            this.clear();
        }
        
        return response;
    },
    
    async checkAuth() {
        try {
            const res = await this.fetch('/api/auth/me');
            if (res.ok) {
                const user = await res.json();
                this.setUser(user);
                return user;
            } else {
                this.clear();
                return null;
            }
        } catch (e) {
            this.clear();
            return null;
        }
    },
    
    async logout() {
        try {
            await this.fetch('/api/auth/logout', { method: 'POST' });
        } catch (e) {
            console.error('Logout error:', e);
        } finally {
            this.clear();
            window.location.href = '/login.html?logout=true';
        }
    }
};
