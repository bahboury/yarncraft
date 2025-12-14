import {useContext, useEffect, useState} from "react";
import api from "../../api/axios";
import {AuthContext} from "../../context/AuthContext";
import {useNavigate} from "react-router-dom";

const VendorAnalytics = () => {
    const {user, logout} = useContext(AuthContext);
    const navigate = useNavigate();
    const [stats, setStats] = useState([]);
    const [loading, setLoading] = useState(true);

    // 1. SECURITY CHECK & DATA FETCHING (FIXED)
    useEffect(() => {
        // Case A: User is logged out
        if (!user) {
            navigate('/login');
            return;
        }

        // Case B: Not an Admin
        if (user.role !== 'ADMIN') {
            navigate('/'); // Just redirect without alert for analytics
            return;
        }

        // Case C: Is Admin -> Fetch Data
        const fetchStats = async () => {
            try {
                const res = await api.get("/admin/vendor-stats");
                setStats(res.data.data || []);
            } catch (err) {
                console.error("Failed to load stats", err);
            } finally {
                setLoading(false);
            }
        };
        fetchStats();

    }, [user, navigate]);

    const handleLogout = () => {
        logout();
        navigate("/login");
    };

    if (loading) return <div className="flex h-screen items-center justify-center text-gray-500">Loading Analytics...
        📊</div>;
    if (!user || user.role !== 'ADMIN') return null;

    return (
        <div className="min-h-screen bg-gray-100 flex flex-col md:flex-row font-sans">
            <aside className="w-full md:w-64 bg-white border-r border-gray-200 shadow-sm flex flex-col">
                <div className="p-6 border-b border-gray-100">
                    <h1 className="text-xl font-bold text-gray-800">YarnCraft Admin</h1>
                </div>
                <nav className="flex-1 p-4 space-y-2">
                    <div onClick={() => navigate('/admin')}
                         className="px-4 py-2 text-gray-600 hover:bg-gray-50 rounded cursor-pointer transition">
                        Vendor Applications
                    </div>
                    <div
                        className="px-4 py-2 bg-blue-50 text-blue-700 font-semibold rounded cursor-pointer border-l-4 border-blue-600">
                        Vendor Analytics
                    </div>
                </nav>
                <div className="p-4 border-t border-gray-100">
                    <button onClick={handleLogout}
                            className="w-full text-left px-4 py-2 text-red-600 hover:bg-red-50 rounded font-medium transition">
                        🚪 Logout
                    </button>
                </div>
            </aside>

            <main className="flex-1 p-8 overflow-y-auto">
                <header className="mb-8">
                    <h2 className="text-3xl font-bold text-gray-800">Vendor Performance</h2>
                    <p className="text-gray-500">Live sales and inventory data for active vendors.</p>
                </header>

                <div className="bg-white rounded-lg shadow border border-gray-200 overflow-hidden">
                    <table className="w-full text-left border-collapse">
                        <thead>
                        <tr className="bg-gray-50 border-b border-gray-200 text-gray-600 uppercase text-xs tracking-wider">
                            <th className="p-4 font-semibold">Vendor / Shop</th>
                            <th className="p-4 font-semibold text-center">Active Products</th>
                            <th className="p-4 font-semibold text-center">Items Sold</th>
                            <th className="p-4 font-semibold text-right">Total Revenue</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100">
                        {stats.map((vendor) => (
                            <tr key={vendor.vendorId} className="hover:bg-gray-50 transition">
                                <td className="p-4">
                                    <div className="font-bold text-gray-900">{vendor.shopName}</div>
                                    <div className="text-sm text-gray-500">{vendor.vendorName}</div>
                                </td>
                                <td className="p-4 text-center">
                                        <span
                                            className="bg-blue-100 text-blue-800 text-xs font-bold px-2 py-1 rounded-full">
                                            {vendor.totalProducts}
                                        </span>
                                </td>
                                <td className="p-4 text-center font-medium text-gray-700">
                                    {vendor.totalSold}
                                </td>
                                <td className="p-4 text-right font-bold text-green-600">
                                    ${vendor.totalRevenue.toFixed(2)}
                                </td>
                            </tr>
                        ))}
                        {stats.length === 0 && !loading && (
                            <tr>
                                <td colSpan="4" className="p-8 text-center text-gray-400">No active vendor data
                                    available.
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>
            </main>
        </div>
    );
};

export default VendorAnalytics;