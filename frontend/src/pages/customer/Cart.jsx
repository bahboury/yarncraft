import {useContext} from "react";
import {CartContext} from "../../context/CartContext"; // Ensure correct path
import {Link, useNavigate} from "react-router-dom";

const Cart = () => {
    // 👇 Get updateQuantity from context
    const {cart, removeFromCart, updateQuantity, getCartTotal, clearCart} = useContext(CartContext);
    const navigate = useNavigate();

    if (cart.length === 0) {
        return (
            <div className="text-center mt-20">
                <h2 className="text-2xl font-bold text-gray-700">Your Cart is Empty 🛒</h2>
                <p className="text-gray-500 mt-2">Looks like you haven't added any yarn yet.</p>
                <Link to="/" className="mt-6 inline-block bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700">
                    Go Shopping
                </Link>
            </div>
        );
    }

    return (
        <div className="max-w-5xl mx-auto p-8 bg-gray-50 min-h-screen">
            <h1 className="text-3xl font-bold text-gray-800 mb-8">Shopping Cart ({cart.length} items)</h1>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                {/* LEFT: Cart Items List */}
                <div className="md:col-span-2 space-y-4">
                    {cart.map((item) => (
                        <div key={item.id}
                             className="bg-white p-4 rounded-lg shadow flex flex-col sm:flex-row items-center justify-between gap-4">

                            {/* Image & Name */}
                            <div className="flex items-center gap-4 w-full sm:w-auto">
                                <div className="h-20 w-20 bg-gray-200 rounded overflow-hidden flex-shrink-0">
                                    {item.imageUrl ? (
                                        <img src={item.imageUrl} alt={item.name}
                                             className="h-full w-full object-cover"/>
                                    ) : (
                                        <span className="flex h-full items-center justify-center text-2xl">🧶</span>
                                    )}
                                </div>
                                <div>
                                    <h3 className="font-bold text-lg text-gray-800">{item.name}</h3>
                                    <p className="text-sm text-gray-500">Unit Price: ${item.price}</p>
                                    <p className="text-xs text-gray-400">Vendor: {item.vendorName || "Unknown"}</p>
                                </div>
                            </div>

                            {/* QUANTITY CONTROLS */}
                            <div className="flex items-center border border-gray-300 rounded">
                                <button
                                    onClick={() => updateQuantity(item.id, item.quantity - 1)}
                                    disabled={item.quantity <= 1}
                                    className="px-3 py-1 bg-gray-100 hover:bg-gray-200 text-gray-600 font-bold disabled:opacity-50"
                                >
                                    -
                                </button>
                                <span className="px-3 py-1 font-bold text-gray-800 min-w-[2rem] text-center">
                                    {item.quantity}
                                </span>
                                <button
                                    onClick={() => updateQuantity(item.id, item.quantity + 1)}
                                    className="px-3 py-1 bg-gray-100 hover:bg-gray-200 text-green-600 font-bold"
                                >
                                    +
                                </button>
                            </div>

                            {/* Remove Button & Total */}
                            <div className="flex flex-col items-end gap-2 min-w-[80px]">
                                <span className="font-bold text-lg text-gray-900">
                                    ${(item.price * item.quantity).toFixed(2)}
                                </span>
                                <button
                                    onClick={() => removeFromCart(item.id)}
                                    className="text-red-500 hover:text-red-700 text-xs font-semibold underline"
                                >
                                    Remove
                                </button>
                            </div>

                        </div>
                    ))}

                    <div className="flex justify-end">
                        <button onClick={clearCart} className="text-gray-400 text-sm hover:text-red-500 underline mt-2">
                            Clear Entire Cart
                        </button>
                    </div>
                </div>

                {/* RIGHT: Order Summary */}
                <div className="bg-white p-6 rounded-lg shadow h-fit sticky top-4">
                    <h2 className="text-xl font-bold text-gray-800 mb-4">Order Summary</h2>
                    <div className="flex justify-between mb-2 text-gray-600">
                        <span>Subtotal</span>
                        <span>${getCartTotal().toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between mb-4 text-gray-600">
                        <span>Shipping</span>
                        <span>Free</span>
                    </div>
                    <hr className="my-4"/>
                    <div className="flex justify-between text-xl font-bold text-gray-900 mb-6">
                        <span>Total</span>
                        <span>${getCartTotal().toFixed(2)}</span>
                    </div>

                    <button
                        onClick={() => navigate("/checkout")}
                        className="w-full bg-green-600 text-white py-3 rounded-lg font-bold hover:bg-green-700 shadow-md transition"
                    >
                        Proceed to Checkout
                    </button>
                </div>
            </div>
        </div>
    );
};

export default Cart;