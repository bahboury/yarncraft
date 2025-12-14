import {useContext, useEffect, useState} from "react"; // 👈 Add useContext and useEffect
import api from "../api/axios";
import {Link, useNavigate} from "react-router-dom"; // 👈 Add useNavigate
import {AuthContext} from "../context/AuthContext"; // 👈 Add AuthContext

const Home = () => {
    const {user} = useContext(AuthContext); // Get user context
    const navigate = useNavigate(); // Initialize navigate
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);

    // 👇 CRITICAL FIX: Redirect Vendors/Admins to their specific page
    useEffect(() => {
        if (user && user.role === 'VENDOR') {
            navigate('/vendor/dashboard');
        } else if (user && user.role === 'ADMIN') {
            navigate('/admin');
        }
        // If not a VENDOR/ADMIN, proceed to fetch products
        if (!user || user.role === 'CUSTOMER') {
            fetchProducts();
        }
    }, [user, navigate]);


    const fetchProducts = async () => {
        try {
            const response = await api.get("/products");
            setProducts(response.data.data || []);
        } catch (error) {
            console.error("Error fetching products:", error);
        } finally {
            setLoading(false);
        }
    };


    if (loading) return <div className="text-center mt-10">Loading yarn... 🧶</div>;

    return (
        <div className="p-8 bg-gray-50 min-h-screen">
            <h1 className="text-3xl font-bold text-blue-800 mb-6 text-center">Marketplace</h1>

            {products.length === 0 ? (
                <div className="text-center text-gray-500">
                    <p className="text-xl">No products found yet.</p>
                    <p className="text-sm mt-2">Vendors need to add some yarn!</p>
                </div>
            ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
                    {products.map((product) => (
                        <div key={product.id} className="bg-white p-4 rounded-lg shadow hover:shadow-lg transition">
                            <div
                                className="h-48 bg-gray-200 rounded mb-4 overflow-hidden flex items-center justify-center">
                                {product.imageUrl ? (
                                    <img src={product.imageUrl} alt={product.name}
                                         className="h-full w-full object-cover"/>
                                ) : (
                                    <span className="text-4xl">🧶</span>
                                )}
                            </div>

                            <h3 className="font-bold text-lg">{product.name}</h3>
                            <p className="text-gray-500 text-sm mb-2">{product.category}</p>

                            <div className="flex justify-between items-center mt-2">
                                <span className="text-green-600 font-bold">${product.price}</span>

                                <Link
                                    to={`/product/${product.id}`}
                                    className="bg-blue-600 text-white px-3 py-1 rounded text-sm hover:bg-blue-700"
                                >
                                    View
                                </Link>

                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default Home;