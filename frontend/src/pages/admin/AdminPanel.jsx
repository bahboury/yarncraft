import {useContext, useEffect, useState} from "react";
import api from "../../api/axios";
import {AuthContext} from "../../context/AuthContext";
import {useNavigate} from "react-router-dom";

const AdminPanel = () => {
    const {user, logout} = useContext(AuthContext);
    const navigate = useNavigate();
    const [applications, setApplications] = useState([]); // Renamed from pendingApplications
    const [loading, setLoading] = useState(true);

    // 1. SECURITY CHECK
    useEffect(() => {
        if (!user || user.role !== 'ADMIN') {
            alert("Access Denied: Admin privileges required.");
            navigate('/');
        }
    }, [user, navigate]);

    // 2. FETCH DATA (Now fetches ALL applications)
    const fetchApplications = async () => {
        setLoading(true);
        try {
            const response = await api.get("/admin/applications");
            // Sort: Pending first, then by date
            const sortedData = (response.data.data || []).sort((a, b) => {
                if (a.status === 'PENDING' && b.status !== 'PENDING') return -1;
                if (a.status !== 'PENDING' && b.status === 'PENDING') return 1;
                return new Date(b.createdAt) - new Date(a.createdAt);
            });
            setApplications(sortedData);
        } catch (error) {
            console.error("Error fetching applications:", error);
            if (error.response?.status !== 403) {
                alert(`Failed to load data: ${error.response?.data?.message || 'Server error'}`);
            }
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (user && user.role === 'ADMIN') {
            fetchApplications();
        }
    }, [user]);

    // 3. ACTIONS
    const handleApprove = async (applicationId) => {
        if (!window.confirm(`Approve this vendor?`)) return;
        try {
            await api.post(`/admin/applications/${applicationId}/approve`);
            fetchApplications(); // Refresh list
        } catch (error) {
            alert(`Approval failed: ${error.response?.data?.message}`);
        }
    };

    const handleReject = async (applicationId) => {
        if (!window.confirm(`Reject this vendor?`)) return;
        try {
            await api.post(`/admin/applications/${applicationId}/reject`);
            fetchApplications(); // Refresh list
        } catch (error) {
            alert(`Rejection failed: ${error.response?.data?.message}`);
        }
    };

    const handleLogout = () => {
        logout();
        navigate("/login");
    };

    // Helper for Status Badge Color
    const getStatusColor = (status) => {
        switch (status) {
            case 'APPROVED':
                return 'bg-green-100 text-green-800 border-green-200';
            case 'REJECTED':
                return 'bg-red-100 text-red-800 border-red-200';
            default:
                return 'bg-yellow-100 text-yellow-800 border-yellow-200';
        }
    };

    if (loading) return <div className="flex h-screen items-center justify-center text-gray-500">Loading Admin Panel...
        🛡️</div>;
    if (!user || user.role !== 'ADMIN') return null;

    return (
        <div className="min-h-screen bg-gray-100 flex flex-col md:flex-row font-sans">

            {/* === SIDEBAR === */}
            <aside className="w-full md:w-64 bg-white border-r border-gray-200 shadow-sm flex flex-col">
                <div className="p-6 border-b border-gray-100">
                    <h1 className="text-xl font-bold text-gray-800">YarnCraft Admin</h1>
                </div>
                <nav className="flex-1 p-4 space-y-2">
                    {/* Current Page */}
                    <div
                        className="px-4 py-2 bg-blue-50 text-blue-700 font-semibold rounded cursor-pointer border-l-4 border-blue-600">
                        Vendor Applications
                    </div>

                    {/* 👇 Add this Link */}
                    <div onClick={() => navigate('/admin/analytics')}
                         className="px-4 py-2 text-gray-600 hover:bg-gray-50 rounded cursor-pointer transition">
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

            {/* === MAIN CONTENT === */}
            <main className="flex-1 p-8 overflow-y-auto">
                <header className="mb-8">
                    <h2 className="text-3xl font-bold text-gray-800">Vendor Management</h2>
                    <p className="text-gray-500">Overview of all vendor applications.</p>
                </header>

                <div className="bg-white rounded-lg shadow border border-gray-200 overflow-hidden">
                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                            <thead>
                            <tr className="bg-gray-50 border-b border-gray-200 text-gray-600 uppercase text-xs tracking-wider">
                                <th className="p-4 font-semibold">Applicant</th>
                                <th className="p-4 font-semibold">Shop Name</th>
                                <th className="p-4 font-semibold">Status</th>
                                <th className="p-4 font-semibold text-center">Actions</th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-100">
                            {applications.map((app) => (
                                <tr key={app.id} className="hover:bg-gray-50 transition">
                                    <td className="p-4">
                                        <div className="font-medium text-gray-900">{app.user?.name || "Unknown"}</div>
                                        <div className="text-sm text-gray-500">{app.user?.email}</div>
                                    </td>
                                    <td className="p-4 text-gray-700">{app.shopName || "N/A"}</td>

                                    {/* STATUS BADGE */}
                                    <td className="p-4">
                                            <span
                                                className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${getStatusColor(app.status)}`}>
                                                {app.status}
                                            </span>
                                    </td>

                                    {/* ACTIONS (Only show if PENDING) */}
                                    <td className="p-4 text-center">
                                        {app.status === 'PENDING' ? (
                                            <div className="flex justify-center gap-2">
                                                <button onClick={() => handleApprove(app.id)}
                                                        className="bg-green-600 hover:bg-green-700 text-white text-xs font-bold py-1.5 px-3 rounded shadow-sm">
                                                    Approve
                                                </button>
                                                <button onClick={() => handleReject(app.id)}
                                                        className="bg-white border border-gray-300 hover:bg-red-50 text-red-600 text-xs font-bold py-1.5 px-3 rounded shadow-sm">
                                                    Reject
                                                </button>
                                            </div>
                                        ) : (
                                            <span className="text-gray-400 text-sm italic">
                                                    {app.status === 'APPROVED' ? 'Active' : 'Closed'}
                                                </span>
                                        )}
                                    </td>
                                </tr>
                            ))}
                            {applications.length === 0 && (
                                <tr>
                                    <td colSpan="4" className="p-8 text-center text-gray-500">No applications found.
                                    </td>
                                </tr>
                            )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </main>
        </div>
    );
};

export default AdminPanel;