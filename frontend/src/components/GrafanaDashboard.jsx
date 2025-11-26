import React from 'react';

const GrafanaDashboard = () => {
    return (
        <div className="h-full w-full flex flex-col" >
            <div className="p-4 border-b border-[var(--border-color)]">
                <h2 className="text-xl font-semibold">Metrics Dashboard</h2>
                <p className="text-sm text-muted">Powered by Grafana</p>
            </div>
            <div className="flex-1 bg-[#181b1f]" style={{ height: '80vh' }}> {/* Grafana dark theme bg color match */}
                <iframe
                    src="http://localhost:3000/d/devpulse-dashboard/devpulse-dashboard?theme=dark&kiosk"
                    width="100%"
                    height="100%"
                    className="border-0"
                    title="Grafana Dashboard"
                />
            </div>
        </div>
    );
};

export default GrafanaDashboard;
