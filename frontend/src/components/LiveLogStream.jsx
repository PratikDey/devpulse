import React, { useState, useEffect, useRef } from 'react';
import { createLogStream, formatTimestamp, getLogLevelClass } from '@utils/api';

/**
 * LiveLogStream Component
 * Real-time log streaming using Server-Sent Events (SSE)
 */
function LiveLogStream() {
    const [logs, setLogs] = useState([]);
    const [isConnected, setIsConnected] = useState(false);
    const [error, setError] = useState(null);
    const [isPaused, setIsPaused] = useState(false);
    const eventSourceRef = useRef(null);
    const maxLogs = 100; // Keep only last 100 logs

    useEffect(() => {
        if (!isPaused) {
            connectToStream();
        }

        return () => {
            disconnectFromStream();
        };
    }, [isPaused]);

    function connectToStream() {
        try {
            const eventSource = createLogStream();
            eventSourceRef.current = eventSource;

            eventSource.onopen = () => {
                setIsConnected(true);
                setError(null);
                console.log('SSE connection established');
            };

            eventSource.addEventListener('log', (event) => {
                try {
                    const logData = JSON.parse(event.data);
                    setLogs(prevLogs => {
                        const newLogs = [logData, ...prevLogs];
                        return newLogs.slice(0, maxLogs); // Keep only recent logs
                    });
                } catch (err) {
                    console.error('Error parsing log data:', err);
                }
            });

            eventSource.onerror = (err) => {
                console.error('SSE connection error:', err);
                setIsConnected(false);
                setError('Connection lost. Retrying...');

                // EventSource will automatically reconnect
            };
        } catch (err) {
            setError(err.message);
            setIsConnected(false);
        }
    }

    function disconnectFromStream() {
        if (eventSourceRef.current) {
            eventSourceRef.current.close();
            eventSourceRef.current = null;
            setIsConnected(false);
        }
    }

    function handleTogglePause() {
        setIsPaused(!isPaused);
        if (isPaused) {
            connectToStream();
        } else {
            disconnectFromStream();
        }
    }

    function handleClearLogs() {
        setLogs([]);
    }

    return (
        <div>
            <div className="card-header" style={{ marginBottom: '1.5rem' }}>
                <h1 className="card-title">Live Log Stream</h1>
                <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
                    {isConnected && (
                        <div className="live-indicator">
                            <span className="live-dot"></span>
                            LIVE
                        </div>
                    )}
                    <button
                        className="btn btn-secondary btn-small"
                        onClick={handleTogglePause}
                    >
                        {isPaused ? '▶️ Resume' : '⏸️ Pause'}
                    </button>
                    <button
                        className="btn btn-secondary btn-small"
                        onClick={handleClearLogs}
                    >
                        🗑️ Clear
                    </button>
                </div>
            </div>

            {error && (
                <div className="card" style={{
                    background: 'rgba(251, 191, 36, 0.1)',
                    borderColor: 'rgba(251, 191, 36, 0.3)',
                    marginBottom: '1rem'
                }}>
                    <p style={{ margin: 0, color: 'var(--accent-warning)' }}>
                        ⚠️ {error}
                    </p>
                </div>
            )}

            <div className="card">
                {!isConnected && !isPaused ? (
                    <div style={{
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                        padding: '3rem',
                        flexDirection: 'column',
                        gap: '1rem'
                    }}>
                        <div className="spinner"></div>
                        <span>Connecting to log stream...</span>
                    </div>
                ) : isPaused ? (
                    <div style={{
                        textAlign: 'center',
                        padding: '3rem',
                        color: 'var(--text-secondary)'
                    }}>
                        <p>⏸️ Stream paused</p>
                        <p className="text-sm">Click Resume to continue receiving logs</p>
                    </div>
                ) : logs.length === 0 ? (
                    <div style={{
                        textAlign: 'center',
                        padding: '3rem',
                        color: 'var(--text-secondary)'
                    }}>
                        <p>Waiting for logs...</p>
                        <p className="text-sm">New logs will appear here in real-time</p>
                    </div>
                ) : (
                    <div style={{ maxHeight: '600px', overflowY: 'auto' }}>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                            {logs.map((log, index) => (
                                <div
                                    key={`${log.id}-${index}`}
                                    className="animate-slide-up"
                                    style={{
                                        padding: '0.75rem',
                                        background: 'var(--bg-tertiary)',
                                        borderLeft: `3px solid ${log.level === 'ERROR' ? 'var(--log-error)' :
                                                log.level === 'WARN' ? 'var(--log-warn)' :
                                                    log.level === 'DEBUG' ? 'var(--log-debug)' :
                                                        'var(--log-info)'
                                            }`,
                                        borderRadius: 'var(--radius-sm)',
                                        display: 'grid',
                                        gridTemplateColumns: 'auto 1fr auto',
                                        gap: '1rem',
                                        alignItems: 'start',
                                    }}
                                >
                                    <div className="text-xs mono text-muted" style={{ minWidth: '140px' }}>
                                        {formatTimestamp(log.timestamp)}
                                    </div>

                                    <div>
                                        <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.25rem' }}>
                                            <span className={`badge ${getLogLevelClass(log.level)}`}>
                                                {log.level}
                                            </span>
                                            <span style={{
                                                fontSize: '0.75rem',
                                                padding: '0.25rem 0.5rem',
                                                background: 'rgba(99, 102, 241, 0.15)',
                                                borderRadius: '0.25rem',
                                                color: 'var(--accent-primary)'
                                            }}>
                                                {log.serviceName}
                                            </span>
                                        </div>
                                        <div className="text-sm" style={{ marginTop: '0.5rem' }}>
                                            {log.message}
                                        </div>
                                    </div>

                                    {log.traceId && (
                                        <div className="text-xs mono text-muted">
                                            {log.traceId}
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {logs.length > 0 && (
                    <div style={{
                        marginTop: '1rem',
                        padding: '0.5rem',
                        textAlign: 'center',
                        borderTop: '1px solid var(--border-color)',
                        fontSize: '0.75rem',
                        color: 'var(--text-muted)'
                    }}>
                        Showing {logs.length} most recent logs
                    </div>
                )}
            </div>
        </div>
    );
}

export default LiveLogStream;
