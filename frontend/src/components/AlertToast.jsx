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

    useEffect(() => {
        // Connect to WebSocket
        const socket = new SockJS(`${config.api.alerts}/alert-ws`);
        const stompClient = Stomp.over(socket);

        // Disable debug logs for cleaner console
        stompClient.debug = () => { };

        stompClient.connect({}, (frame) => {
            console.log('Connected to Alert WebSocket: ' + frame);

            stompClient.subscribe('/topic/alerts', (message) => {
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
            if (stompClient && stompClient.connected) {
                stompClient.disconnect();
            }
        };
    }, []);

    function addToast(toast) {
        setToasts(prevToasts => [...prevToasts, toast]);

        // Auto-remove after 5 seconds
        setTimeout(() => {
            removeToast(toast.id);
        }, 5000);
    }

    function removeToast(id) {
        setToasts(prevToasts => prevToasts.filter(toast => toast.id !== id));
    }

    if (toasts.length === 0) {
        return null;
    }

    return (
        <div className="toast-container">
            {toasts.map(toast => (
                <div
                    key={toast.id}
                    className={`toast toast-${toast.type}`}
                >
                    <div style={{ flex: 1 }}>
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
                        }}
                    >
                        ×
                    </button>
                </div>
            ))}
        </div>
    );
}

export default AlertToast;
