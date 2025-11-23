/**
 * API Client for DevPulse Backend
 * Centralized HTTP client with error handling
 */

// In development, use empty string to leverage Vite proxy
// In production, use environment variable or full URL
const API_BASE_URL = import.meta.env.VITE_API_URL || '';

/**
 * Generic fetch wrapper with error handling
 */
async function apiFetch(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;

    try {
        const response = await fetch(url, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers,
            },
            ...options,
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        const data = await response.json();
        return data;
    } catch (error) {
        console.error(`API Error [${endpoint}]:`, error);
        throw error;
    }
}

// ================================================
// Log API
// ================================================

/**
 * Fetch paginated logs
 */
export async function fetchLogs(page = 0, size = 20) {
    return apiFetch(`/api/logs?page=${page}&size=${size}`);
}

/**
 * Fetch logs by service name
 */
export async function fetchLogsByService(serviceName, page = 0, size = 20) {
    return apiFetch(`/api/logs/service/${encodeURIComponent(serviceName)}?page=${page}&size=${size}`);
}

/**
 * Fetch logs by level (DEBUG, INFO, WARN, ERROR)
 */
export async function fetchLogsByLevel(level, page = 0, size = 20) {
    return apiFetch(`/api/logs/level/${encodeURIComponent(level)}?page=${page}&size=${size}`);
}

/**
 * Fetch recent logs (top 100)
 */
export async function fetchRecentLogs() {
    return apiFetch('/api/logs/recent');
}

/**
 * Fetch logs within a time range
 */
export async function fetchLogsByRange(fromIso, toIso, page = 0, size = 20) {
    return apiFetch(
        `/api/logs/range?from=${encodeURIComponent(fromIso)}&to=${encodeURIComponent(toIso)}&page=${page}&size=${size}`
    );
}

/**
 * Create EventSource for real-time log streaming (SSE)
 */
export function createLogStream() {
    return new EventSource(`${API_BASE_URL}/api/logs/stream`);
}

// ================================================
// Utility Functions
// ================================================

/**
 * Extract data from ApiResponse wrapper
 */
export function extractData(apiResponse) {
    if (apiResponse && apiResponse.data) {
        return apiResponse.data;
    }
    return apiResponse;
}

/**
 * Format timestamp for display
 */
export function formatTimestamp(timestamp) {
    if (!timestamp) return '-';
    const date = new Date(timestamp);
    return date.toLocaleString('en-US', {
        month: 'short',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false,
    });
}

/**
 * Get log level badge class
 */
export function getLogLevelClass(level) {
    const levelMap = {
        DEBUG: 'badge-debug',
        INFO: 'badge-info',
        WARN: 'badge-warn',
        ERROR: 'badge-error',
    };
    return levelMap[level] || 'badge-info';
}

/**
 * Get log row class for border color
 */
export function getLogRowClass(level) {
    return `log-row-${level}`;
}
