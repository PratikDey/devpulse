import React, { useState, useEffect } from 'react';
import { fetchAlerts, formatTimestamp, getLogLevelClass } from '@utils/api';

function AlertsPage() {
    const [alerts, setAlerts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        loadAlerts();
    }, []);

    async function loadAlerts() {
        try {
            setLoading(true);
            const response = await fetchAlerts();
            if (response.success) {
                setAlerts(response.data);
            } else {
                setError(response.message || 'Failed to fetch alerts');
            }
        } catch (err) {
            setError('Failed to connect to alert service');
            console.error(err);
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="order-management">
            <div className="page-header">
                <h1>Alert History</h1>
                <button className="btn btn-secondary" onClick={loadAlerts} disabled={loading}>
                    {loading ? 'Refreshing...' : 'Refresh'}
                </button>
            </div>

            {error && (
                <div className="error-banner">
                    <span>⚠️ {error}</span>
                    <button onClick={() => setError(null)}>×</button>
                </div>
            )}

            <div className="card">
                <div className="table-container">
                    <table className="table">
                        <thead>
                            <tr>
                                <th>Timestamp</th>
                                <th>Service</th>
                                <th>Severity</th>
                                <th>Message</th>
                                <th>Details</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading && alerts.length === 0 ? (
                                <tr>
                                    <td colSpan="5" className="text-center p-4">Loading alerts...</td>
                                </tr>
                            ) : alerts.length === 0 ? (
                                <tr>
                                    <td colSpan="5" className="text-center p-4">No alerts found</td>
                                </tr>
                            ) : (
                                alerts.map((alert) => (
                                    <tr key={alert.id} className={`log-row-${alert.severity}`}>
                                        <td className="mono text-sm text-secondary">
                                            {formatTimestamp(alert.timestamp)}
                                        </td>
                                        <td>
                                            <span className="badge badge-debug">{alert.serviceName}</span>
                                        </td>
                                        <td>
                                            <span className={`badge ${getLogLevelClass(alert.severity)}`}>
                                                {alert.severity}
                                            </span>
                                        </td>
                                        <td style={{ maxWidth: '400px', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                            {alert.message}
                                        </td>
                                        <td className="text-sm text-muted">
                                            {alert.details || '-'}
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}

export default AlertsPage;
