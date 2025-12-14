import {useContext, useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import api from "../api/axios";
import {AuthContext} from "../context/AuthContext";
import {CartContext} from "../context/CartContext";

const ProductDetails = () => {
    const {id} = useParams();
    const navigate = useNavigate();
    const {user} = useContext(AuthContext);
    const {addToCart} = useContext(CartContext);

    const [product, setProduct] = useState(null);
    const [stock, setStock] = useState(0);
    const [loading, setLoading] = useState(true);

    // 👇 NEW: State for the quantity selector
    const [quantity, setQuantity] = useState(1);

    useEffect(() => {
        const fetchDetails = async () => {
            try {
                const prodRes = await api.get(`/products/${id}`);
                setProduct(prodRes.data.data || prodRes.data);

                try {
                    const stockRes = await api.get(`/inventory/available/${id}`);
                    const stockData = stockRes.data.data || stockRes.data;
                    setStock(stockData?.availableStock || 0);
                } catch (err) {
                    setStock(0);
                }

            } catch (error) {
                console.error("Error fetching product:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchDetails();
    }, [id]);

    // 👇 NEW: Helper functions to change quantity
    const increaseQty = () => {
        if (quantity < stock) setQuantity(prev => prev + 1);
    };

    const decreaseQty = () => {
        if (quantity > 1) setQuantity(prev => prev - 1);
    };

    const handleAddToCart = () => {
        if (!user) {
            alert("Please login to add items to your cart!");
            navigate("/login");
            return;
        }
        // 👇 PASS THE QUANTITY HERE
        addToCart(product, quantity);
    };

    if (loading) return <div className="text-center mt-20 text-xl">Loading yarn details... 🧶</div>;
    if (!product) return <div className="text-center mt-20 text-red-500">Product not found!</div>;

    return (
        <div className="max-w-6xl mx-auto p-8 bg-gray-50 min-h-[calc(100vh-64px)]">

            <button onClick={() => navigate(-1)}
                    className="mb-6 text-gray-600 hover:text-blue-600 flex items-center gap-2">
                ← Back to Shop
            </button>

            <div className="bg-white rounded-xl shadow-lg overflow-hidden grid grid-cols-1 md:grid-cols-2">

                {/* Left: Image */}
                <div className="h-96 md:h-full bg-gray-200 flex items-center justify-center">
                    {product.imageUrl ? (
                        <img src={product.imageUrl} alt={product.name} className="w-full h-full object-cover"/>
                    ) : (
                        <span className="text-6xl">🧶</span>
                    )}
                </div>

                {/* Right: Info */}
                <div className="p-8 flex flex-col justify-center">
                    <div
                        className="uppercase tracking-wide text-sm text-indigo-500 font-semibold">{product.category}</div>
                    <h1 className="mt-2 text-4xl font-bold text-gray-900">{product.name}</h1>
                    <p className="mt-4 text-gray-600 text-lg leading-relaxed">{product.description}</p>

                    <div className="mt-8 flex items-center justify-between">
                        <span className="text-3xl font-bold text-gray-900">${product.price}</span>

                        <span
                            className={`px-3 py-1 rounded-full text-sm font-semibold ${stock > 0 ? "bg-green-100 text-green-800" : "bg-red-100 text-red-800"}`}>
              {stock > 0 ? `${stock} In Stock` : "Sold Out"}
            </span>
                    </div>

                    {/* 👇 NEW: Quantity Selector UI */}
                    {stock > 0 && (
                        <div className="mt-8 flex items-center gap-4">
                            <span className="font-semibold text-gray-700">Quantity:</span>
                            <div className="flex items-center border border-gray-300 rounded-lg">
                                <button
                                    onClick={decreaseQty}
                                    className="px-4 py-2 hover:bg-gray-100 text-gray-600 font-bold border-r border-gray-300 disabled:opacity-50"
                                    disabled={quantity <= 1}
                                >
                                    -
                                </button>
                                <span className="px-4 py-2 font-bold text-gray-800 min-w-[3rem] text-center">
                          {quantity}
                      </span>
                                <button
                                    onClick={increaseQty}
                                    className="px-4 py-2 hover:bg-gray-100 text-green-600 font-bold border-l border-gray-300 disabled:opacity-50"
                                    disabled={quantity >= stock}
                                >
                                    +
                                </button>
                            </div>
                        </div>
                    )}

                    <button
                        onClick={handleAddToCart}
                        disabled={stock === 0}
                        className={`mt-6 w-full py-4 rounded-lg font-bold text-lg transition shadow-lg
              ${stock > 0
                            ? "bg-blue-600 text-white hover:bg-blue-700 hover:shadow-xl"
                            : "bg-gray-300 text-gray-500 cursor-not-allowed"
                        }`}
                    >
                        {stock > 0 ? `Add ${quantity} to Cart 🛒` : "Out of Stock 🚫"}
                    </button>

                    <p className="mt-4 text-xs text-center text-gray-400">
                        Sold by: <span
                        className="font-bold text-gray-600">{product.vendorName || "Unknown Vendor"}</span>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default ProductDetails;