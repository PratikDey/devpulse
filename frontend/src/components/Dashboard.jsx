import React, { useState, useEffect } from 'react';
import {
    fetchLogs,
    fetchLogsByService,
    fetchLogsByLevel,
    extractData,
    formatTimestamp,
    getLogLevelClass,
    getLogRowClass,
} from '../utils/api';

/**
 * Dashboard Component
 * Main log viewer with filtering and pagination
 */
function Dashboard() {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // Filters
    const [serviceFilter, setServiceFilter] = useState('');
    const [levelFilter, setLevelFilter] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalLogs, setTotalLogs] = useState(0);

    // Available services and levels
    const [services, setServices] = useState([]);
    const logLevels = ['DEBUG', 'INFO', 'WARN', 'ERROR'];

    // Fetch logs based on current filters
    useEffect(() => {
        loadLogs();
    }, [page, serviceFilter, levelFilter]);

    async function loadLogs() {
        setLoading(true);
        setError(null);

        try {
            let response;

            if (serviceFilter) {
                response = await fetchLogsByService(serviceFilter, page, 20);
            } else if (levelFilter) {
                response = await fetchLogsByLevel(levelFilter, page, 20);
            } else {
                response = await fetchLogs(page, 20);
            }

            const data = extractData(response);

            // Handle paginated response
            if (data.content && Array.isArray(data.content)) {
                setLogs(data.content);
                setTotalPages(data.totalPages || 0);
                setTotalLogs(data.totalElements || 0);

                // Extract unique services
                const uniqueServices = [...new Set(data.content.map(log => log.serviceName))];
                setServices(prev => {
                    const combined = [...new Set([...prev, ...uniqueServices])];
                    return combined.sort();
                });
            } else if (Array.isArray(data)) {
                // Handle array response (recent logs)
                setLogs(data);
                setTotalLogs(data.length);

                const uniqueServices = [...new Set(data.map(log => log.serviceName))];
                setServices(uniqueServices.sort());
            } else {
                setLogs([]);
            }
        } catch (err) {
            setError(err.message);
            console.error('Failed to load logs:', err);
        } finally {
            setLoading(false);
        }
    }

    function handleServiceChange(e) {
        setServiceFilter(e.target.value);
        setPage(0); // Reset to first page
    }

    function handleLevelChange(e) {
        setLevelFilter(e.target.value);
        setPage(0);
    }

    function handleClearFilters() {
        setServiceFilter('');
        setLevelFilter('');
        setPage(0);
    }

    function handlePreviousPage() {
        if (page > 0) {
            setPage(page - 1);
        }
    }

    function handleNextPage() {
        if (page < totalPages - 1) {
            setPage(page + 1);
        }
    }

    return (
        <div>
            <div className="card-header" style={{ marginBottom: '1.5rem' }}>
                <h1 className="card-title">Log Dashboard</h1>
                <div className="text-secondary text-sm">
                    {totalLogs > 0 ? `${totalLogs} logs found` : 'No logs'}
                </div>
            </div>

            {/* Filters */}
            <div className="filters-bar">
                <div className="filter-group">
                    <label className="form-label">Service</label>
                    <select
                        className="select"
                        value={serviceFilter}
                        onChange={handleServiceChange}
                    >
                        <option value="">All Services</option>
                        {services.map(service => (
                            <option key={service} value={service}>{service}</option>
                        ))}
                    </select>
                </div>

                <div className="filter-group">
                    <label className="form-label">Level</label>
                    <select
                        className="select"
                        value={levelFilter}
                        onChange={handleLevelChange}
                    >
                        <option value="">All Levels</option>
                        {logLevels.map(level => (
                            <option key={level} value={level}>{level}</option>
                        ))}
                    </select>
                </div>

                <div className="filter-group" style={{ display: 'flex', alignItems: 'flex-end' }}>
                    <button
                        className="btn btn-secondary"
                        onClick={handleClearFilters}
                        disabled={!serviceFilter && !levelFilter}
                    >
                        Clear Filters
                    </button>
                </div>

                <div className="filter-group" style={{ display: 'flex', alignItems: 'flex-end' }}>
                    <button
                        className="btn btn-primary"
                        onClick={loadLogs}
                        disabled={loading}
                    >
                        {loading ? 'Loading...' : 'Refresh'}
                    </button>
                </div>
            </div>

            {/* Error Message */}
            {error && (
                <div className="card" style={{
                    background: 'rgba(239, 68, 68, 0.1)',
                    borderColor: 'rgba(239, 68, 68, 0.3)',
                    marginBottom: '1rem'
                }}>
                    <p style={{ margin: 0, color: 'var(--accent-error)' }}>
                        ⚠️ Error: {error}
                    </p>
                </div>
            )}

            {/* Log Table */}
            <div className="card">
                {loading ? (
                    <div style={{
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                        padding: '3rem'
                    }}>
                        <div className="spinner"></div>
                        <span style={{ marginLeft: '1rem' }}>Loading logs...</span>
                    </div>
                ) : logs.length === 0 ? (
                    <div style={{
                        textAlign: 'center',
                        padding: '3rem',
                        color: 'var(--text-secondary)'
                    }}>
                        <p>No logs found.</p>
                        <p className="text-sm">Try adjusting your filters or check if backend services are running.</p>
                    </div>
                ) : (
                    <>
                        <div className="table-container">
                            <table className="table">
                                <thead>
                                    <tr>
                                        <th>Timestamp</th>
                                        <th>Service</th>
                                        <th>Level</th>
                                        <th>Message</th>
                                        <th>Trace ID</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {logs.map((log, index) => (
                                        <tr key={log.id || index} className={getLogRowClass(log.level)}>
                                            <td className="text-xs mono">{formatTimestamp(log.timestamp)}</td>
                                            <td className="text-sm">
                                                <span style={{
                                                    padding: '0.25rem 0.5rem',
                                                    background: 'rgba(99, 102, 241, 0.1)',
                                                    borderRadius: '0.25rem',
                                                    fontWeight: '500'
                                                }}>
                                                    {log.serviceName}
                                                </span>
                                            </td>
                                            <td>
                                                <span className={`badge ${getLogLevelClass(log.level)}`}>
                                                    {log.level}
                                                </span>
                                            </td>
                                            <td className="text-sm" style={{ maxWidth: '500px' }}>
                                                {log.message}
                                            </td>
                                            <td className="text-xs mono text-muted">
                                                {log.traceId || '-'}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>

                        {/* Pagination */}
                        {totalPages > 1 && (
                            <div style={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                marginTop: '1rem',
                                padding: '1rem',
                                borderTop: '1px solid var(--border-color)'
                            }}>
                                <div className="text-sm text-secondary">
                                    Page {page + 1} of {totalPages}
                                </div>
                                <div style={{ display: 'flex', gap: '0.5rem' }}>
                                    <button
                                        className="btn btn-secondary btn-small"
                                        onClick={handlePreviousPage}
                                        disabled={page === 0}
                                    >
                                        Previous
                                    </button>
                                    <button
                                        className="btn btn-secondary btn-small"
                                        onClick={handleNextPage}
                                        disabled={page >= totalPages - 1}
                                    >
                                        Next
                                    </button>
                                </div>
                            </div>
                        )}
                    </>
                )}
            </div>
        </div>
    );
}

export default Dashboard;
