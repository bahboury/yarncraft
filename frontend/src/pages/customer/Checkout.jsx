import {useContext, useState} from "react";
import {CartContext} from "../../context/CartContext";
import {AuthContext} from "../../context/AuthContext";
import {useNavigate} from "react-router-dom";
// 👇 UNCOMMENTED THIS IMPORT
import api from "../../api/axios";

const Checkout = () => {
    const {cart, getCartTotal, clearCart} = useContext(CartContext);
    const {user} = useContext(AuthContext);
    const navigate = useNavigate();

    const [address, setAddress] = useState({
        street: "",
        city: "",
        zip: "",
        phone: ""
    });

    const handleChange = (e) => {
        setAddress({...address, [e.target.name]: e.target.value});
    };

    const handlePlaceOrder = async (e) => {
        e.preventDefault();

        if (cart.length === 0) {
            alert("Your cart is empty!");
            return;
        }

        try {
            // 1. Prepare the Order Data
            const orderPayload = {
                userId: user?.id,
                shippingAddress: `${address.street}, ${address.city} ${address.zip}`,
                phone: address.phone,
                items: cart.map(item => ({
                    productId: item.id,
                    quantity: item.quantity,
                    price: item.price
                })),
                totalAmount: getCartTotal()
            };

            console.log("📤 Sending Order to Backend:", orderPayload);

            // 👇 2. Send to Backend (UNCOMMENTED & FIXED URL)
            // We use "/orders" if your axios baseURL already has "/api",
            // otherwise use "/api/orders". I'll use "/orders" assuming standard setup.
            await api.post("/orders", orderPayload);

            // 3. Real Success
            alert("Order Placed Successfully! 🎉");
            clearCart();
            navigate("/"); // Go back home

        } catch (error) {
            console.error("Order Failed:", error);
            // Show a more helpful error message
            const errorMessage = error.response?.data?.message || "Failed to place order.";
            alert(`Error: ${errorMessage}`);
        }
    };

    if (cart.length === 0) return <div className="text-center mt-20">Redirecting...</div>;

    return (
        <div className="max-w-4xl mx-auto p-8 bg-gray-50 min-h-screen">
            <h1 className="text-3xl font-bold text-gray-800 mb-8">Checkout</h1>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">

                {/* LEFT: Shipping Form */}
                <form onSubmit={handlePlaceOrder} className="bg-white p-6 rounded-lg shadow space-y-4">
                    <h2 className="text-xl font-bold text-gray-700 mb-4">Shipping Details</h2>

                    <div>
                        <label className="block text-gray-600 mb-1">Street Address</label>
                        <input name="street" required onChange={handleChange} className="w-full border p-2 rounded"
                               placeholder="123 Yarn St"/>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-gray-600 mb-1">City</label>
                            <input name="city" required onChange={handleChange} className="w-full border p-2 rounded"
                                   placeholder="New York"/>
                        </div>
                        <div>
                            <label className="block text-gray-600 mb-1">ZIP Code</label>
                            <input name="zip" required onChange={handleChange} className="w-full border p-2 rounded"
                                   placeholder="10001"/>
                        </div>
                    </div>

                    <div>
                        <label className="block text-gray-600 mb-1">Phone Number</label>
                        <input name="phone" required onChange={handleChange} className="w-full border p-2 rounded"
                               placeholder="+1 234 567 890"/>
                    </div>

                    <button type="submit"
                            className="w-full bg-green-600 text-white font-bold py-3 rounded mt-4 hover:bg-green-700 transition">
                        Confirm & Pay ${getCartTotal().toFixed(2)}
                    </button>
                </form>

                {/* RIGHT: Order Summary */}
                <div className="bg-white p-6 rounded-lg shadow h-fit">
                    <h2 className="text-xl font-bold text-gray-700 mb-4">Your Order</h2>
                    <div className="space-y-3 max-h-60 overflow-auto mb-4">
                        {cart.map((item) => (
                            <div key={item.id} className="flex justify-between text-sm">
                                <span>{item.quantity}x {item.name}</span>
                                <span className="font-semibold">${(item.price * item.quantity).toFixed(2)}</span>
                            </div>
                        ))}
                    </div>
                    <hr className="border-gray-200 my-2"/>
                    <div className="flex justify-between text-xl font-bold text-gray-900">
                        <span>Total</span>
                        <span>${getCartTotal().toFixed(2)}</span>
                    </div>
                </div>

            </div>
        </div>
    );
};

export default Checkout;