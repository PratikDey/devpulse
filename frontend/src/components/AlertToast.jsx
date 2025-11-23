import React, { useState, useEffect } from 'react';

/**
 * AlertToast Component
 * Toast notification system for alerts
 */
function AlertToast() {
    const [toasts, setToasts] = useState([]);

    // Simulate alert notifications (in real app, this would come from WebSocket/SSE)
    useEffect(() => {
        // This is a placeholder - in production, you'd connect to your alert system
        const exampleToast = {
            id: Date.now(),
            type: 'info',
            title: 'Alert System',
            message: 'Alert notifications will appear here when triggered',
        };

        // Show example toast on mount
        setTimeout(() => {
            addToast(exampleToast);
        }, 2000);
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
