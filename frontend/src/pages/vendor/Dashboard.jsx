import {useContext, useEffect, useState} from "react";
import api from "../../api/axios";
import {AuthContext} from "../../context/AuthContext";
import {Link, useNavigate} from "react-router-dom";
import RestockModal from "../../components/RestockModal";

const Dashboard = () => {
    const {user} = useContext(AuthContext);
    const navigate = useNavigate();

    // === 🟢 DASHBOARD STATE (For Approved Vendors) ===
    const [stats, setStats] = useState(null);
    const [products, setProducts] = useState([]);
    const [dashboardLoading, setDashboardLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedProduct, setSelectedProduct] = useState(null);

    // === 🟠 ONBOARDING STATE (For New/Pending Vendors) ===
    // Status can be: CHECKING, APPROVED, PENDING, NEW
    const [appStatus, setAppStatus] = useState("CHECKING");
    const [formData, setFormData] = useState({shopName: "", description: ""});
    const [submitLoading, setSubmitLoading] = useState(false);

    // =======================================================
    // 1️⃣ INITIAL CHECK & DATA FETCHING
    // =======================================================
    useEffect(() => {
        if (!user) return;

        if (user.role !== 'VENDOR') {
            alert("Access Denied.");
            navigate("/");
            return;
        }

        const checkStatus = async () => {
            try {
                const response = await api.get("/users/me");

                // 👇 DEBUG: Log this to see exactly what you get
                console.log("Profile Data:", response.data.data);

                const status = response.data.data.applicationStatus;

                // 👇 FIX: Change 'isApproved' to 'approved' to match your JSON
                const isApproved = response.data.data.approved;

                // We accept EITHER the boolean flag OR the explicit status string
                if (isApproved === true || status === "APPROVED") {
                    setAppStatus("APPROVED");
                    fetchDashboardData();
                } else if (status === "PENDING") {
                    setAppStatus("PENDING");
                } else if (status === "REJECTED") {
                    setAppStatus("NEW");
                    alert("Your previous application was rejected. Please update your details and submit again.");
                } else {
                    setAppStatus("NEW");
                }
            } catch (error) {
                console.error("Failed to check status", error);
                setAppStatus("NEW");
            }
        };

        checkStatus();

    }, [user, navigate]);

    // Function to fetch the actual dashboard data (Stats + Inventory)
    const fetchDashboardData = async () => {
        setDashboardLoading(true);
        try {
            const statsRes = await api.get("/inventory/dashboard");
            setStats(statsRes.data.data);

            const inventoryRes = await api.get("/inventory/my-inventory");
            setProducts(inventoryRes.data.data || []);
        } catch (error) {
            console.error("Error loading dashboard:", error);
        } finally {
            setDashboardLoading(false);
        }
    };

    // =======================================================
    // 2️⃣ APPLICATION HANDLERS (For Unapproved Vendors)
    // =======================================================
    const handleApplicationSubmit = async (e) => {
        e.preventDefault();
        setSubmitLoading(true);
        try {
            await api.post("/users/vendor-application", formData);
            setAppStatus("PENDING"); // Switch UI to "Under Review"
            alert("Application Submitted! Please wait for Admin approval.");
        } catch (error) {
            console.error("Application error:", error);
            // Optional: If backend says "Application already exists", set status to PENDING
            alert("Failed to submit: " + (error.response?.data?.message || "Server Error"));
        } finally {
            setSubmitLoading(false);
        }
    };

    // =======================================================
    // 3️⃣ DASHBOARD HANDLERS (For Approved Vendors)
    // =======================================================
    const handleRestock = (item) => {
        setSelectedProduct(item);
        setIsModalOpen(true);
    };

    const handleDelete = async (productId) => {
        if (!window.confirm("Are you sure you want to delete this product?")) return;
        try {
            await api.delete(`/products/${productId}`);
            alert("Product deleted successfully!");
            await fetchDashboardData();
        } catch (error) {
            alert(`Failed to delete: ${error.response?.data?.message}`);
        }
    };

    // =======================================================
    // 🎨 RENDER LOGIC
    // =======================================================

    // 🛑 STATE: PENDING (User submitted, waiting for Admin)
    if (appStatus === "PENDING") {
        return (
            <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50 p-4">
                <div className="bg-white p-8 rounded-xl shadow-lg text-center max-w-md border border-gray-100">
                    <div className="text-6xl mb-4">⏳</div>
                    <h2 className="text-2xl font-bold text-gray-800 mb-2">Application Under Review</h2>
                    <p className="text-gray-600 mb-6">
                        We have received your request for <strong>{formData.shopName || "your shop"}</strong>.
                        The admin is currently reviewing your details.
                    </p>
                    <button onClick={() => window.location.reload()}
                            className="text-blue-600 font-semibold hover:underline">
                        Check Status Again
                    </button>
                </div>
            </div>
        );
    }

    // 🛑 STATE: NEW (User needs to apply)
    if (appStatus === "NEW") {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
                <form onSubmit={handleApplicationSubmit}
                      className="bg-white p-10 rounded-xl shadow-xl max-w-lg w-full border border-gray-100">
                    <div className="text-center mb-8">
                        <span className="text-4xl">🏪</span>
                        <h1 className="text-2xl font-bold text-gray-800 mt-2">Setup Your Shop</h1>
                        <p className="text-gray-500">Tell us about your business to start selling.</p>
                    </div>

                    <div className="mb-5">
                        <label className="block text-gray-700 font-bold mb-2 text-sm uppercase">Shop Name</label>
                        <input
                            type="text"
                            required
                            className="w-full border border-gray-300 p-3 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition"
                            placeholder="e.g. Grandma's Knits"
                            value={formData.shopName}
                            onChange={(e) => setFormData({...formData, shopName: e.target.value})}
                        />
                    </div>

                    <div className="mb-6">
                        <label className="block text-gray-700 font-bold mb-2 text-sm uppercase">Description</label>
                        <textarea
                            required
                            className="w-full border border-gray-300 p-3 rounded-lg h-32 focus:ring-2 focus:ring-blue-500 outline-none transition"
                            placeholder="What kind of yarn products do you make?"
                            value={formData.description}
                            onChange={(e) => setFormData({...formData, description: e.target.value})}
                        />
                    </div>

                    <button
                        disabled={submitLoading}
                        className="w-full bg-blue-600 text-white font-bold py-3 rounded-lg hover:bg-blue-700 transition shadow-md disabled:bg-blue-300"
                    >
                        {submitLoading ? "Submitting..." : "Submit Application"}
                    </button>
                </form>
            </div>
        );
    }

    // 🛑 STATE: LOADING DASHBOARD
    if (dashboardLoading && appStatus === "APPROVED") {
        return <div className="p-8 text-center text-gray-500">Loading Dashboard... 📊</div>;
    }

    // ✅ STATE: APPROVED (Render the Real Dashboard)
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
                    {products.map((item) => (
                        <tr key={item.id} className="hover:bg-gray-50 transition">
                            <td className="p-4 font-semibold text-gray-700">{item.productName}</td>
                            <td className="p-4 text-gray-600">${item.unitPrice}</td>
                            <td className="p-4 font-mono text-blue-600">{item.stockQuantity}</td>
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
                                    onClick={() => handleRestock(item)}
                                    className="text-blue-500 hover:text-blue-700 text-sm font-semibold mr-3"
                                >
                                    Restock 📦
                                </button>
                                <button
                                    onClick={() => handleDelete(item.productId)}
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

            {/* MODAL */}
            {isModalOpen && selectedProduct && (
                <RestockModal
                    product={selectedProduct}
                    onClose={() => setIsModalOpen(false)}
                    onRestockSuccess={fetchDashboardData}
                />
            )}
        </div>
    );
};

export default Dashboard;