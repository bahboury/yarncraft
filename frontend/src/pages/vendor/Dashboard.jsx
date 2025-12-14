import {useContext, useEffect, useState} from "react";
import api from "../../api/axios";
import {AuthContext} from "../../context/AuthContext";
import {Link, useNavigate} from "react-router-dom";
import RestockModal from "../../components/RestockModal";

const Dashboard = () => {
    const {user} = useContext(AuthContext);
    const navigate = useNavigate();
    const [stats, setStats] = useState(null);
    const [products, setProducts] = useState([]); // This now holds InventoryItem objects
    const [loading, setLoading] = useState(true);

    // NEW STATE for Modal Management
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedProduct, setSelectedProduct] = useState(null);

    const fetchData = async () => {
        setLoading(true);
        try {
            // 1. Fetch Stats (Still needed for the stat cards)
            const statsRes = await api.get("/inventory/dashboard");
            setStats(statsRes.data.data);

            // 2. NEW: Fetch Vendor's INVENTORY ITEMS (This contains the Stock!)
            // Calls the new /api/inventory/my-inventory endpoint
            const inventoryRes = await api.get("/inventory/my-inventory");

            // We set the products state to the list of inventory items
            setProducts(inventoryRes.data.data || []);

        } catch (error) {
            console.error("Error loading dashboard:", error);
            if (error.response?.status === 403) {
                alert("Access Denied. You must be a Vendor.");
                navigate("/");
            }
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (user) {
            fetchData();
        }
    }, [user]);

    // =======================================================
    // 🛠️ HANDLERS
    // =======================================================

    const handleRestock = (item) => {
        // Pass the InventoryItem object to the modal
        setSelectedProduct(item);
        setIsModalOpen(true);
    };

    const handleDelete = async (productId) => {
        if (!window.confirm("Are you sure you want to delete this product? This action cannot be undone.")) {
            return;
        }

        try {
            // Delete endpoint expects the Product ID, which is stored in the InventoryItem as 'productId'
            await api.delete(`/products/${productId}`);
            alert("Product deleted successfully!");
            fetchData(); // Refresh the product list and stats

        } catch (error) {
            console.error("Failed to delete product:", error);
            alert(`Failed to delete product. Error: ${error.response?.data?.message || 'Server error'}`);
        }
    };

    // =======================================================

    if (loading) return <div className="p-8 text-center text-gray-500">Loading Dashboard... 📊</div>;

    if (!user || user.role !== 'VENDOR') return <div className="p-8 text-center text-red-500">Access Denied. Please
        login as a VENDOR.</div>;


    return (
        <div className="max-w-6xl mx-auto p-6 bg-gray-50 min-h-screen">
            <div className="flex justify-between items-center mb-8">
                <div>
                    <h1 className="text-3xl font-bold text-gray-800">Vendor Dashboard</h1>
                    <p className="text-gray-500">Welcome back, {user?.name}!</p>
                </div>
                <Link to="/vendor/add-product"
                      className="bg-blue-600 text-white px-6 py-2 rounded-lg font-bold hover:bg-blue-700 shadow-md">
                    + Add New Product
                </Link>
            </div>

            {/* 📈 STATS CARDS */}
            {stats && (
                <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
                    <div className="bg-white p-6 rounded-xl shadow border border-gray-100">
                        <p className="text-gray-500 text-sm font-semibold uppercase">Total Revenue</p>
                        <p className="text-3xl font-bold text-green-600">${stats.potentialRevenue?.toFixed(2) || 0}</p>
                    </div>
                    <div className="bg-white p-6 rounded-xl shadow border border-gray-100">
                        <p className="text-gray-500 text-sm font-semibold uppercase">Total Sold</p>
                        <p className="text-3xl font-bold text-blue-600">{stats.totalSold} items</p>
                    </div>
                    <div className="bg-white p-6 rounded-xl shadow border border-gray-100">
                        <p className="text-gray-500 text-sm font-semibold uppercase">Active Products</p>
                        <p className="text-3xl font-bold text-indigo-600">{stats.activeProducts}</p>
                    </div>
                    <div className="bg-white p-6 rounded-xl shadow border border-gray-100">
                        <p className="text-gray-500 text-sm font-semibold uppercase">Low Stock Alert</p>
                        <p className={`text-3xl font-bold ${stats.lowStockCount > 0 ? "text-red-600" : "text-gray-400"}`}>
                            {stats.lowStockCount}
                        </p>
                    </div>
                </div>
            )}

            {/* 📋 PRODUCTS TABLE */}
            <div className="bg-white rounded-xl shadow overflow-hidden">
                <div className="p-6 border-b border-gray-100">
                    <h2 className="text-xl font-bold text-gray-800">My Inventory</h2>
                </div>
                <table className="w-full text-left border-collapse">
                    <thead>
                    <tr className="bg-gray-50 text-gray-600 text-sm uppercase">
                        <th className="p-4">Product</th>
                        <th className="p-4">Price</th>
                        <th className="p-4">Stock</th>
                        <th className="p-4">Status</th>
                        <th className="p-4 text-right">Actions</th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                    {products.map((item) => ( // 👈 Loop over InventoryItem (renamed from 'product' to 'item')
                        <tr key={item.id} className="hover:bg-gray-50 transition">
                            {/* 👇 Display InventoryItem fields */}
                            <td className="p-4 font-semibold text-gray-700">{item.productName}</td>
                            <td className="p-4 text-gray-600">${item.unitPrice}</td>
                            <td className="p-4 font-mono text-blue-600">{item.stockQuantity}</td>
                            {/* 👈 FIXED: Displays real stock */}

                            <td className="p-4">
                                <span className={`px-2 py-1 rounded text-xs font-bold 
                                    ${item.status === 'IN_STOCK' ? "bg-green-100 text-green-700" :
                                    item.status === 'LOW_STOCK' ? "bg-yellow-100 text-yellow-700" :
                                        "bg-red-100 text-red-700"}`}>
                                    {item.status.toLowerCase().replace('_', ' ')}
                                </span>
                            </td>

                            <td className="p-4 text-right">
                                <button
                                    onClick={() => handleRestock(item)} // 👈 Pass the InventoryItem
                                    className="text-blue-500 hover:text-blue-700 text-sm font-semibold mr-3"
                                >
                                    Restock 📦
                                </button>
                                <button
                                    onClick={() => handleDelete(item.productId)} // 👈 Pass the required productId
                                    className="text-red-500 hover:text-red-700 text-sm font-semibold"
                                >
                                    Delete
                                </button>
                            </td>
                        </tr>
                    ))}
                    {products.length === 0 && (
                        <tr>
                            <td colSpan="5" className="p-8 text-center text-gray-400">
                                You have no products yet. Click "Add New Product" to start selling!
                            </td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>

            {/* RENDER THE MODAL HERE */}
            {isModalOpen && selectedProduct && (
                <RestockModal
                    product={selectedProduct} // This is the InventoryItem
                    onClose={() => setIsModalOpen(false)}
                    onRestockSuccess={fetchData} // Re-fetch data after successful restock
                />
            )}
        </div>
    );
};

export default Dashboard;