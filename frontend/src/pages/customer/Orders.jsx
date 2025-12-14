import {useContext, useEffect, useState} from "react";
import api from "../../api/axios";
import {AuthContext} from "../../context/AuthContext";
import {Link} from "react-router-dom";

const Orders = () => {
    const {user} = useContext(AuthContext);
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchOrders = async () => {
            try {
                // 👇 This calls the 'getMyOrders' method in your Spring Boot Controller
                const response = await api.get("/orders");
                // Access the list inside the response structure (ApiResponse)
                setOrders(response.data.data || []);
            } catch (error) {
                console.error("Failed to fetch orders:", error);
            } finally {
                setLoading(false);
            }
        };

        if (user) {
            fetchOrders();
        }
    }, [user]);

    if (loading) return <div className="text-center mt-20">Loading your history... 📜</div>;

    if (orders.length === 0) {
        return (
            <div className="max-w-4xl mx-auto p-8 text-center">
                <h1 className="text-3xl font-bold text-gray-800 mb-4">My Orders</h1>
                <p className="text-gray-600 mb-8">You haven't placed any orders yet.</p>
                <Link to="/" className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700">
                    Start Shopping
                </Link>
            </div>
        );
    }

    return (
        <div className="max-w-4xl mx-auto p-8 bg-gray-50 min-h-screen">
            <h1 className="text-3xl font-bold text-gray-800 mb-8">My Orders</h1>

            <div className="space-y-6">
                {orders.map((order) => (
                    <div key={order.id} className="bg-white p-6 rounded-lg shadow-md border border-gray-100">
                        {/* Order Header */}
                        <div className="flex flex-col md:flex-row justify-between border-b pb-4 mb-4">
                            <div>
                                <p className="text-sm text-gray-500">Order ID: <span
                                    className="font-mono text-gray-800">#{order.id}</span></p>
                                <p className="text-sm text-gray-500">Date: {new Date(order.orderDate).toLocaleDateString()}</p>
                            </div>
                            <div className="mt-2 md:mt-0 text-right">
                                <p className="text-lg font-bold text-green-600">${order.totalAmount}</p>
                                <span
                                    className="inline-block bg-yellow-100 text-yellow-800 text-xs px-2 py-1 rounded-full uppercase font-semibold">
                                    {order.status}
                                </span>
                            </div>
                        </div>

                        {/* Order Items */}
                        <div className="space-y-3">
                            {order.items.map((item) => (
                                <div key={item.id} className="flex justify-between items-center text-sm">
                                    <div className="flex items-center gap-3">
                                        <div
                                            className="h-10 w-10 bg-gray-200 rounded flex items-center justify-center text-lg">
                                            🧶
                                        </div>
                                        <div>
                                            {/* Note: In your backend DTO, ensure 'product' is included in 'items' */}
                                            <p className="font-semibold text-gray-800">{item.product?.name || "Product"}</p>
                                            <p className="text-gray-500">Qty: {item.quantity}</p>
                                        </div>
                                    </div>
                                    <p className="font-medium">${item.priceAtPurchase}</p>
                                </div>
                            ))}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default Orders;