/**
 * Application Configuration
 * 
 * Centralizes API endpoints and other environment-specific settings.
 * Reads from Vite environment variables (import.meta.env).
 */

const config = {
    api: {
        // Base URLs for microservices
        // In development, these might be empty to use Vite proxy
        // In production, they should be full URLs
        products: import.meta.env.VITE_API_PRODUCT_URL || '',
        orders: import.meta.env.VITE_API_ORDER_URL || '',
        logs: import.meta.env.VITE_API_LOG_URL || '',
        alerts: import.meta.env.VITE_API_ALERT_URL || '',
    },
    grafanaUrl: import.meta.env.VITE_GRAFANA_URL || '/grafana/d/devpulse-dashboard/devpulse-dashboard?theme=dark&kiosk',
};

export default config;
