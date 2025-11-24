import React, { useState, useEffect } from 'react';

const OrderManagement = () => {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [pageSize] = useState(10);

    // Modal states
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [selectedOrder, setSelectedOrder] = useState(null);

    // Form states
    const [formData, setFormData] = useState({
        name: '',
        productId: '',
        quantity: 1,
        price: 0
    });

    // Bulk delete states
    const [selectedOrderIds, setSelectedOrderIds] = useState([]);

    // Fetch orders with pagination
    const fetchOrders = async (page = 0) => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch(`/api/orders?page=${page}&size=${pageSize}`);
            const result = await response.json();

            if (result.success && result.data) {
                setOrders(result.data.content || []);
                setTotalPages(result.data.totalPages || 0);
                setTotalElements(result.data.totalElements || 0);
                setCurrentPage(page);
                setSelectedOrderIds([]); // Clear selections on page change
            } else {
                setError(result.message || 'Failed to fetch orders');
            }
        } catch (err) {
            setError('Error connecting to server: ' + err.message);
        } finally {
            setLoading(false);
        }
    };

    // Create new order
    const createOrder = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            const response = await fetch('/api/orders', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    name: formData.name,
                    productId: formData.productId,
                    quantity: parseInt(formData.quantity),
                    price: parseFloat(formData.price),
                    orderDate: new Date().toISOString()
                }),
            });

            const result = await response.json();

            if (result.success) {
                setShowCreateModal(false);
                resetForm();
                fetchOrders(currentPage);
            } else {
                setError(result.message || 'Failed to create order');
            }
        } catch (err) {
            setError('Error creating order: ' + err.message);
        } finally {
            setLoading(false);
        }
    };

    // Update order
    const updateOrder = async (e) => {
        e.preventDefault();
        if (!selectedOrder) return;

        setLoading(true);
        setError(null);

        try {
            const response = await fetch(`/api/orders/${selectedOrder.orderId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    name: formData.name,
                    productId: formData.productId,
                    quantity: parseInt(formData.quantity),
                    price: parseFloat(formData.price),
                }),
            });

            const result = await response.json();

            if (result.success) {
                setShowEditModal(false);
                setSelectedOrder(null);
                resetForm();
                fetchOrders(currentPage);
            } else {
                setError(result.message || 'Failed to update order');
            }
        } catch (err) {
            setError('Error updating order: ' + err.message);
        } finally {
            setLoading(false);
        }
    };

    // Delete order
    const deleteOrder = async (orderId) => {
        if (!window.confirm('Are you sure you want to delete this order?')) {
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const response = await fetch(`/api/orders/${orderId}`, {
                method: 'DELETE',
            });

            const result = await response.json();

            if (result.success) {
                fetchOrders(currentPage);
            } else {
                setError(result.message || 'Failed to delete order');
            }
        } catch (err) {
            setError('Error deleting order: ' + err.message);
        } finally {
            setLoading(false);
        }
    };

    // Bulk delete orders
    const bulkDeleteOrders = async () => {
        if (selectedOrderIds.length === 0) {
            setError('No orders selected');
            return;
        }

        const count = selectedOrderIds.length;
        if (!window.confirm(`Are you sure you want to delete ${count} order(s)?`)) {
            return;
        }

        setLoading(true);
        setError(null);

        try {
            // Delete all selected orders in parallel
            const deletePromises = selectedOrderIds.map(orderId =>
                fetch(`/api/orders/${orderId}`, { method: 'DELETE' })
                    .then(res => res.json())
            );

            const results = await Promise.all(deletePromises);

            // Check if all deletions were successful
            const failedDeletions = results.filter(result => !result.success);

            if (failedDeletions.length > 0) {
                setError(`Failed to delete ${failedDeletions.length} order(s)`);
            }

            // Refresh the list and clear selections
            setSelectedOrderIds([]);
            fetchOrders(currentPage);
        } catch (err) {
            setError('Error deleting orders: ' + err.message);
        } finally {
            setLoading(false);
        }
    };

    // Toggle individual order selection
    const toggleOrderSelection = (orderId) => {
        setSelectedOrderIds(prev =>
            prev.includes(orderId)
                ? prev.filter(id => id !== orderId)
                : [...prev, orderId]
        );
    };

    // Toggle select all orders on current page
    const toggleSelectAll = () => {
        if (selectedOrderIds.length === orders.length) {
            setSelectedOrderIds([]);
        } else {
            setSelectedOrderIds(orders.map(order => order.orderId));
        }
    };

    // Open edit modal with order data
    const openEditModal = (order) => {
        setSelectedOrder(order);
        setFormData({
            name: order.name,
            productId: order.productId,
            quantity: order.quantity,
            price: order.price
        });
        setShowEditModal(true);
    };

    // Reset form
    const resetForm = () => {
        setFormData({
            name: '',
            productId: '',
            quantity: 1,
            price: 0
        });
    };

    // Handle form input changes
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    // Load orders on component mount
    useEffect(() => {
        fetchOrders(0);
    }, []);

    // Format date
    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        const date = new Date(dateString);
        return date.toLocaleString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    return (
        <div className="order-management">
            {/* Header */}
            <div className="page-header">
                <div>
                    <h1>📦 Order Management</h1>
                    <p className="text-secondary">Create, view, update, and delete orders</p>
                </div>
                <div className="flex gap-sm">
                    {selectedOrderIds.length > 0 && (
                        <button
                            className="btn btn-danger"
                            onClick={bulkDeleteOrders}
                            disabled={loading}
                        >
                            <span>🗑️</span>
                            <span>Delete Selected ({selectedOrderIds.length})</span>
                        </button>
                    )}
                    <button
                        className="btn btn-primary"
                        onClick={() => {
                            resetForm();
                            setShowCreateModal(true);
                        }}
                    >
                        <span>➕</span>
                        <span>Create Order</span>
                    </button>
                </div>
            </div>

            {/* Error Display */}
            {error && (
                <div className="error-banner">
                    <span>⚠️</span>
                    <span>{error}</span>
                    <button onClick={() => setError(null)}>✕</button>
                </div>
            )}

            {/* Stats */}
            <div className="stats-grid">
                <div className="stat-card">
                    <div className="stat-value">{totalElements}</div>
                    <div className="stat-label">Total Orders</div>
                </div>
                <div className="stat-card">
                    <div className="stat-value">{currentPage + 1}</div>
                    <div className="stat-label">Current Page</div>
                </div>
                <div className="stat-card">
                    <div className="stat-value">{totalPages}</div>
                    <div className="stat-label">Total Pages</div>
                </div>
            </div>

            {/* Orders Table */}
            <div className="card">
                <div className="card-header">
                    <h3 className="card-title">Orders List</h3>
                    <button
                        className="btn btn-secondary btn-small"
                        onClick={() => fetchOrders(currentPage)}
                        disabled={loading}
                    >
                        {loading ? '🔄' : '🔃'} Refresh
                    </button>
                </div>

                {loading && orders.length === 0 ? (
                    <div className="loading-state">
                        <div className="spinner"></div>
                        <p>Loading orders...</p>
                    </div>
                ) : orders.length === 0 ? (
                    <div className="empty-state">
                        <span style={{ fontSize: '3rem' }}>📦</span>
                        <h3>No Orders Found</h3>
                        <p className="text-secondary">Create your first order to get started</p>
                    </div>
                ) : (
                    <>
                        <div className="table-container">
                            <table className="table">
                                <thead>
                                    <tr>
                                        <th style={{ width: '40px' }}>
                                            <input
                                                type="checkbox"
                                                checked={orders.length > 0 && selectedOrderIds.length === orders.length}
                                                onChange={toggleSelectAll}
                                                title="Select all on this page"
                                            />
                                        </th>
                                        <th>Order ID</th>
                                        <th>Name</th>
                                        <th>Product ID</th>
                                        <th>Quantity</th>
                                        <th>Price</th>
                                        <th>Total</th>
                                        <th>Order Date</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {orders.map((order) => (
                                        <tr key={order.orderId}>
                                            <td>
                                                <input
                                                    type="checkbox"
                                                    checked={selectedOrderIds.includes(order.orderId)}
                                                    onChange={() => toggleOrderSelection(order.orderId)}
                                                />
                                            </td>
                                            <td>
                                                <code className="mono text-sm">{order.orderId}</code>
                                            </td>
                                            <td>
                                                <strong>{order.name}</strong>
                                            </td>
                                            <td>
                                                <code className="mono text-sm">{order.productId}</code>
                                            </td>
                                            <td>{order.quantity}</td>
                                            <td>${order.price.toFixed(2)}</td>
                                            <td>
                                                <strong>${(order.quantity * order.price).toFixed(2)}</strong>
                                            </td>
                                            <td className="text-sm text-secondary">
                                                {formatDate(order.orderDate)}
                                            </td>
                                            <td>
                                                <div className="action-buttons">
                                                    <button
                                                        className="btn-icon btn-edit"
                                                        onClick={() => openEditModal(order)}
                                                        title="Edit Order"
                                                    >
                                                        ✏️
                                                    </button>
                                                    <button
                                                        className="btn-icon btn-delete"
                                                        onClick={() => deleteOrder(order.orderId)}
                                                        title="Delete Order"
                                                    >
                                                        🗑️
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>

                        {/* Pagination */}
                        <div className="pagination">
                            <button
                                className="btn btn-secondary btn-small"
                                onClick={() => fetchOrders(currentPage - 1)}
                                disabled={currentPage === 0 || loading}
                            >
                                ← Previous
                            </button>
                            <span className="pagination-info">
                                Page {currentPage + 1} of {totalPages}
                            </span>
                            <button
                                className="btn btn-secondary btn-small"
                                onClick={() => fetchOrders(currentPage + 1)}
                                disabled={currentPage >= totalPages - 1 || loading}
                            >
                                Next →
                            </button>
                        </div>
                    </>
                )}
            </div>

            {/* Create Order Modal */}
            {showCreateModal && (
                <div className="modal-overlay" onClick={() => setShowCreateModal(false)}>
                    <div className="modal" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3>Create New Order</h3>
                            <button
                                className="modal-close"
                                onClick={() => setShowCreateModal(false)}
                            >
                                ✕
                            </button>
                        </div>
                        <form onSubmit={createOrder}>
                            <div className="modal-body">
                                <div className="form-group">
                                    <label className="form-label">Order Name *</label>
                                    <input
                                        type="text"
                                        name="name"
                                        className="input"
                                        value={formData.name}
                                        onChange={handleInputChange}
                                        placeholder="Enter order name"
                                        required
                                    />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Product ID *</label>
                                    <input
                                        type="text"
                                        name="productId"
                                        className="input"
                                        value={formData.productId}
                                        onChange={handleInputChange}
                                        placeholder="Enter product ID"
                                        required
                                    />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Quantity *</label>
                                    <input
                                        type="number"
                                        name="quantity"
                                        className="input"
                                        value={formData.quantity}
                                        onChange={handleInputChange}
                                        min="1"
                                        required
                                    />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Price *</label>
                                    <input
                                        type="number"
                                        name="price"
                                        className="input"
                                        value={formData.price}
                                        onChange={handleInputChange}
                                        step="0.01"
                                        min="0"
                                        required
                                    />
                                </div>
                            </div>
                            <div className="modal-footer">
                                <button
                                    type="button"
                                    className="btn btn-secondary"
                                    onClick={() => setShowCreateModal(false)}
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    className="btn btn-primary"
                                    disabled={loading}
                                >
                                    {loading ? 'Creating...' : 'Create Order'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Edit Order Modal */}
            {showEditModal && selectedOrder && (
                <div className="modal-overlay" onClick={() => setShowEditModal(false)}>
                    <div className="modal" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3>Edit Order</h3>
                            <button
                                className="modal-close"
                                onClick={() => setShowEditModal(false)}
                            >
                                ✕
                            </button>
                        </div>
                        <form onSubmit={updateOrder}>
                            <div className="modal-body">
                                <div className="form-group">
                                    <label className="form-label">Order ID</label>
                                    <input
                                        type="text"
                                        className="input"
                                        value={selectedOrder.orderId}
                                        disabled
                                    />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Order Name *</label>
                                    <input
                                        type="text"
                                        name="name"
                                        className="input"
                                        value={formData.name}
                                        onChange={handleInputChange}
                                        required
                                    />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Product ID *</label>
                                    <input
                                        type="text"
                                        name="productId"
                                        className="input"
                                        value={formData.productId}
                                        onChange={handleInputChange}
                                        required
                                    />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Quantity *</label>
                                    <input
                                        type="number"
                                        name="quantity"
                                        className="input"
                                        value={formData.quantity}
                                        onChange={handleInputChange}
                                        min="1"
                                        required
                                    />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Price *</label>
                                    <input
                                        type="number"
                                        name="price"
                                        className="input"
                                        value={formData.price}
                                        onChange={handleInputChange}
                                        step="0.01"
                                        min="0"
                                        required
                                    />
                                </div>
                            </div>
                            <div className="modal-footer">
                                <button
                                    type="button"
                                    className="btn btn-secondary"
                                    onClick={() => setShowEditModal(false)}
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    className="btn btn-primary"
                                    disabled={loading}
                                >
                                    {loading ? 'Updating...' : 'Update Order'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default OrderManagement;
