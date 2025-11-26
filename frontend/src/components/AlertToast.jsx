import React, { useState, useEffect } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import config from '@config';

/**
 * AlertToast Component
 * Toast notification system for alerts
 * Connects to backend WebSocket to receive real-time alerts
 */
function AlertToast() {
    const [toasts, setToasts] = useState([]);

    const lastToastRef = React.useRef({ message: '', time: 0 });

    useEffect(() => {
        let isMounted = true;
        const socket = new SockJS(`${config.api.alerts}/alert-ws`);
        const stompClient = Stomp.over(socket);

        // Disable debug logs for cleaner console
        stompClient.debug = () => { };

        stompClient.connect({}, (frame) => {
            if (!isMounted) {
                stompClient.disconnect();
                return;
            }
            console.log('Connected to Alert WebSocket: ' + frame);

            stompClient.subscribe('/topic/alerts', (message) => {
                if (!isMounted) return;
                try {
                    const alert = JSON.parse(message.body);
                    addToast({
                        id: alert.id || Date.now(),
                        type: 'error', // Alerts are usually critical/error
                        title: `Alert: ${alert.ruleName || 'System Alert'}`,
                        message: alert.message || JSON.stringify(alert),
                    });
                } catch (e) {
                    console.error("Failed to parse alert message", e);
                }
            });
        }, (error) => {
            console.error('WebSocket connection error:', error);
        });

        return () => {
            isMounted = false;
            if (stompClient && stompClient.connected) {
                stompClient.disconnect();
            }
        };
    }, []);

    function addToast(toast) {
        // Simple deduplication: ignore if same message received within 500ms
        const now = Date.now();
        if (toast.message === lastToastRef.current.message && (now - lastToastRef.current.time) < 500) {
            return;
        }
        lastToastRef.current = { message: toast.message, time: now };

        setToasts(prevToasts => [...prevToasts, { ...toast, isExiting: false }]);

        // Auto-remove after 10 seconds
        setTimeout(() => {
            triggerRemoveToast(toast.id);
        }, 10000);
    }

    function triggerRemoveToast(id) {
        setToasts(prevToasts => prevToasts.map(t =>
            t.id === id ? { ...t, isExiting: true } : t
        ));

        // Wait for animation to finish before actual removal
        setTimeout(() => {
            setToasts(prevToasts => prevToasts.filter(toast => toast.id !== id));
        }, 300);
    }

    function removeToast(id) {
        triggerRemoveToast(id);
    }

    if (toasts.length === 0) {
        return null;
    }

    return (
        <div className="toast-container">
            {toasts.map(toast => (
                <div
                    key={toast.id}
                    className={`toast toast-${toast.type} ${toast.isExiting ? 'toast-exit' : 'toast-enter'}`}
                    style={{ position: 'relative', overflow: 'hidden' }}
                >
                    <div style={{ flex: 1, zIndex: 1 }}>
                        <div style={{ fontWeight: 600, marginBottom: '0.25rem' }}>
                            {toast.title}
                        </div>
                        <div className="text-sm text-secondary">
                            {toast.message}
                        </div>
                    </div>
                    <button
                        onClick={() => removeToast(toast.id)}
                        style={{
                            background: 'none',
                            border: 'none',
                            color: 'var(--text-secondary)',
                            cursor: 'pointer',
                            fontSize: '1.25rem',
                            padding: '0',
                            width: '24px',
                            height: '24px',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            zIndex: 1
                        }}
                    >
                        ×
                    </button>
                    <div className="toast-progress"></div>
                </div>
            ))}
        </div>
    );
}

export default AlertToast;
