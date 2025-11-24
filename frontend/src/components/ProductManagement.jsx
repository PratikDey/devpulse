import React, { useState, useEffect } from 'react';

const ProductManagement = () => {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [pageSize] = useState(10);

    // Modal states
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [selectedProduct, setSelectedProduct] = useState(null);

    // Form states
    const [formData, setFormData] = useState({
        name: '',
        category: '',
        price: 0
    });

    // Fetch products with pagination
    const fetchProducts = async (page = 0) => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch(`/api/products?page=${page}&size=${pageSize}`);
            const result = await response.json();

            if (result.success && result.data) {
                setProducts(result.data.content || []);
                setTotalPages(result.data.totalPages || 0);
                setTotalElements(result.data.totalElements || 0);
                setCurrentPage(page);
            } else {
                setError(result.message || 'Failed to fetch products');
            }
        } catch (err) {
            setError('Error connecting to server: ' + err.message);
        } finally {
            setLoading(false);
        }
    };

    // Create new product
    const createProduct = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            const response = await fetch('/api/products', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    name: formData.name,
                    category: formData.category,
                    price: parseFloat(formData.price)
                }),
            });

            const result = await response.json();

            if (result.success) {
                setShowCreateModal(false);
                resetForm();
                fetchProducts(currentPage);
            } else {
                setError(result.message || 'Failed to create product');
            }
        } catch (err) {
            setError('Error creating product: ' + err.message);
        } finally {
            setLoading(false);
        }
    };

    // Update product
    const updateProduct = async (e) => {
        e.preventDefault();
        if (!selectedProduct) return;

        setLoading(true);
        setError(null);

        try {
            const response = await fetch(`/api/products/${selectedProduct.productId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    name: formData.name,
                    category: formData.category,
                    price: parseFloat(formData.price),
                }),
            });

            const result = await response.json();

            if (result.success) {
                setShowEditModal(false);
                setSelectedProduct(null);
                resetForm();
                fetchProducts(currentPage);
            } else {
                setError(result.message || 'Failed to update product');
            }
        } catch (err) {
            setError('Error updating product: ' + err.message);
        } finally {
            setLoading(false);
        }
    };

    // Delete product
    const deleteProduct = async (productId) => {
        if (!window.confirm('Are you sure you want to delete this product?')) {
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const response = await fetch(`/api/products/${productId}`, {
                method: 'DELETE',
            });

            const result = await response.json();

            if (result.success) {
                fetchProducts(currentPage);
            } else {
                setError(result.message || 'Failed to delete product');
            }
        } catch (err) {
            setError('Error deleting product: ' + err.message);
        } finally {
            setLoading(false);
        }
    };

    // Open edit modal with product data
    const openEditModal = (product) => {
        setSelectedProduct(product);
        setFormData({
            name: product.name,
            category: product.category || '',
            price: product.price
        });
        setShowEditModal(true);
    };

    // Reset form
    const resetForm = () => {
        setFormData({
            name: '',
            category: '',
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

    // Load products on component mount
    useEffect(() => {
        fetchProducts(0);
    }, []);

    return (
        <div className="product-management">
            {/* Header */}
            <div className="page-header">
                <div>
                    <h1>🏷️ Product Management</h1>
                    <p className="text-secondary">Manage your product catalog</p>
                </div>
                <div className="flex gap-sm">
                    <button
                        className="btn btn-primary"
                        onClick={() => {
                            resetForm();
                            setShowCreateModal(true);
                        }}
                    >
                        <span>➕</span>
                        <span>Create Product</span>
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
                    <div className="stat-label">Total Products</div>
                </div>
                <div className="stat-card">
                    <div className="stat-value">{currentPage + 1}</div>
                    <div className="stat-label">Current Page</div>
                </div>
            </div>

            {/* Products Table */}
            <div className="card">
                <div className="card-header">
                    <h3 className="card-title">Products List</h3>
                    <button
                        className="btn btn-secondary btn-small"
                        onClick={() => fetchProducts(currentPage)}
                        disabled={loading}
                    >
                        {loading ? '🔄' : '🔃'} Refresh
                    </button>
                </div>

                {loading && products.length === 0 ? (
                    <div className="loading-state">
                        <div className="spinner"></div>
                        <p>Loading products...</p>
                    </div>
                ) : products.length === 0 ? (
                    <div className="empty-state">
                        <span style={{ fontSize: '3rem' }}>🏷️</span>
                        <h3>No Products Found</h3>
                        <p className="text-secondary">Create your first product to get started</p>
                    </div>
                ) : (
                    <>
                        <div className="table-container">
                            <table className="table">
                                <thead>
                                    <tr>
                                        <th>Product ID</th>
                                        <th>Name</th>
                                        <th>Category</th>
                                        <th>Price</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {products.map((product) => (
                                        <tr key={product.productId}>
                                            <td>
                                                <code className="mono text-sm">{product.productId}</code>
                                            </td>
                                            <td>
                                                <strong>{product.name}</strong>
                                            </td>
                                            <td>
                                                <span className="badge badge-info">{product.category || 'Uncategorized'}</span>
                                            </td>
                                            <td>${product.price.toFixed(2)}</td>
                                            <td>
                                                <div className="action-buttons">
                                                    <button
                                                        className="btn-icon btn-edit"
                                                        onClick={() => openEditModal(product)}
                                                        title="Edit Product"
                                                    >
                                                        ✏️
                                                    </button>
                                                    <button
                                                        className="btn-icon btn-delete"
                                                        onClick={() => deleteProduct(product.productId)}
                                                        title="Delete Product"
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
                                onClick={() => fetchProducts(currentPage - 1)}
                                disabled={currentPage === 0 || loading}
                            >
                                ← Previous
                            </button>
                            <span className="pagination-info">
                                Page {currentPage + 1} of {totalPages}
                            </span>
                            <button
                                className="btn btn-secondary btn-small"
                                onClick={() => fetchProducts(currentPage + 1)}
                                disabled={currentPage >= totalPages - 1 || loading}
                            >
                                Next →
                            </button>
                        </div>
                    </>
                )}
            </div>

            {/* Create Product Modal */}
            {showCreateModal && (
                <div className="modal-overlay" onClick={() => setShowCreateModal(false)}>
                    <div className="modal" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3>Create New Product</h3>
                            <button
                                className="modal-close"
                                onClick={() => setShowCreateModal(false)}
                            >
                                ✕
                            </button>
                        </div>
                        <form onSubmit={createProduct}>
                            <div className="modal-body">
                                <div className="form-group">
                                    <label className="form-label">Product Name *</label>
                                    <input
                                        type="text"
                                        name="name"
                                        className="input"
                                        value={formData.name}
                                        onChange={handleInputChange}
                                        placeholder="Enter product name"
                                        required
                                    />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Category</label>
                                    <input
                                        type="text"
                                        name="category"
                                        className="input"
                                        value={formData.category}
                                        onChange={handleInputChange}
                                        placeholder="e.g. Electronics"
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
                                    {loading ? 'Creating...' : 'Create Product'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Edit Product Modal */}
            {showEditModal && selectedProduct && (
                <div className="modal-overlay" onClick={() => setShowEditModal(false)}>
                    <div className="modal" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3>Edit Product</h3>
                            <button
                                className="modal-close"
                                onClick={() => setShowEditModal(false)}
                            >
                                ✕
                            </button>
                        </div>
                        <form onSubmit={updateProduct}>
                            <div className="modal-body">
                                <div className="form-group">
                                    <label className="form-label">Product ID</label>
                                    <input
                                        type="text"
                                        className="input"
                                        value={selectedProduct.productId}
                                        disabled
                                    />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Product Name *</label>
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
                                    <label className="form-label">Category</label>
                                    <input
                                        type="text"
                                        name="category"
                                        className="input"
                                        value={formData.category}
                                        onChange={handleInputChange}
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
                                    {loading ? 'Updating...' : 'Update Product'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ProductManagement;
