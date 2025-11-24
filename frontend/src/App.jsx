import React from "react";
import { BrowserRouter as Router, Routes, Route, Link, useLocation } from "react-router-dom";
import Dashboard from "./components/Dashboard";
import LiveLogStream from "./components/LiveLogStream";
import OrderManagement from "./components/OrderManagement";
import ProductManagement from './components/ProductManagement';
import GrafanaDashboard from "./components/GrafanaDashboard";
import AlertToast from "./components/AlertToast";

function AppLayout() {
  const location = useLocation();

  const isActive = (path) => location.pathname === path;

  return (
    <div className="app">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="logo">
            <span>⚡</span>
            <span>DevPulse</span>
          </div>
        </div>

        <nav>
          <ul className="nav-menu">
            <li className="nav-item">
              <Link
                to="/"
                className={`nav-link ${isActive('/') ? 'active' : ''}`}
              >
                <span>📊</span>
                <span>Dashboard</span>
              </Link>
            </li>
            <li className="nav-item">
              <Link
                to="/live"
                className={`nav-link ${isActive('/live') ? 'active' : ''}`}
              >
                <span>📡</span>
                <span>Live Stream</span>
              </Link>
            </li>
            <li className="nav-item">
              <Link
                to="/orders"
                className={`nav-link ${isActive('/orders') ? 'active' : ''}`}
              >
                <span>📦</span>
                <span>Orders</span>
              </Link>
            </li>
            <li className="nav-item">
              <Link
                to="/products"
                className={`nav-link ${isActive('/products') ? 'active' : ''}`}
              >
                <span>🏷️</span>
                <span>Products</span>
              </Link>
            </li>
            <li className="nav-item">
              <Link
                to="/metrics"
                className={`nav-link ${isActive('/metrics') ? 'active' : ''}`}
              >
                <span>📈</span>
                <span>Metrics</span>
              </Link>
            </li>
          </ul>
        </nav>

        <div style={{ marginTop: 'auto', padding: '1rem 0', borderTop: '1px solid var(--border-color)' }}>
          <div className="text-xs text-muted">
            DevPulse v1.0.0
          </div>
          <div className="text-xs text-muted" style={{ marginTop: '0.25rem' }}>
            Developer Observability Platform
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <main className="main-content">
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/live" element={<LiveLogStream />} />
          <Route path="/orders" element={<OrderManagement />} />
          <Route path="/products" element={<ProductManagement />} />
          <Route path="/metrics" element={<GrafanaDashboard />} />
        </Routes>
      </main>

      {/* Toast Notifications */}
      <AlertToast />
    </div>
  );
}

function App() {
  return (
    <Router>
      <AppLayout />
    </Router>
  );
}

export default App;
