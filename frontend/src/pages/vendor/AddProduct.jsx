import {useContext, useState} from "react";
import api from "../../api/axios";
import {AuthContext} from "../../context/AuthContext";
import {useNavigate} from "react-router-dom";

const AddProduct = () => {
    const {user} = useContext(AuthContext);
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        name: "",
        description: "",
        price: "",
        category: "YARN", // Default category
        imageUrl: "",
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const handleChange = (e) => {
        const {name, value} = e.target;
        setFormData(prevData => ({
            ...prevData,
            [name]: name === "price" ? parseFloat(value) : value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        if (!user || !user.id) {
            setError("User not logged in or ID not available.");
            setLoading(false);
            return;
        }

        try {
            await api.post("/products", formData);
            alert("Product added successfully!");
            navigate("/vendor/dashboard"); // Redirect to dashboard after adding
        } catch (err) {
            console.error("Error adding product:", err);
            setError(err.response?.data?.message || "Failed to add product.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-8 max-w-2xl mx-auto">
            <h1 className="text-3xl font-bold text-gray-800 mb-6 text-center">Add New Product</h1>

            <form onSubmit={handleSubmit} className="bg-white shadow-lg rounded-lg p-8 border border-gray-200">
                {error && <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4"
                               role="alert">
                    <strong className="font-bold">Error!</strong>
                    <span className="block sm:inline"> {error}</span>
                </div>}

                <div className="mb-4">
                    <label htmlFor="name" className="block text-gray-700 text-sm font-bold mb-2">Product Name</label>
                    <input
                        type="text"
                        id="name"
                        name="name"
                        value={formData.name}
                        onChange={handleChange}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    />
                </div>

                <div className="mb-4">
                    <label htmlFor="description"
                           className="block text-gray-700 text-sm font-bold mb-2">Description</label>
                    <textarea
                        id="description"
                        name="description"
                        value={formData.description}
                        onChange={handleChange}
                        rows="4"
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    ></textarea>
                </div>

                <div className="mb-4">
                    <label htmlFor="price" className="block text-gray-700 text-sm font-bold mb-2">Price</label>
                    <input
                        type="number"
                        id="price"
                        name="price"
                        value={formData.price}
                        onChange={handleChange}
                        step="0.01"
                        min="0"
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    />
                </div>

                <div>
                    <label className="block text-gray-700 font-semibold mb-2">Category</label>
                    <select
                        name="category"
                        onChange={handleChange}
                        className="w-full border border-gray-300 p-3 rounded bg-white focus:ring-2 focus:ring-blue-500 outline-none"
                    >
                        {/* 👇 THESE MUST MATCH THE BACKEND ENUM EXACTLY (Case Sensitive!) */}
                        <option value="YARN">Yarn</option>
                        <option value="CROCHET_HOOKS">Crochet Hooks</option>
                        {/* Check spelling: Plural? */}
                        <option value="KNITTING_NEEDLES">Knitting Needles</option>
                        {/* Check spelling: Plural? */}
                        <option value="ACCESSORIES">Accessories</option>
                        {/* Check spelling */}
                        <option value="PATTERNS">Patterns</option>
                        <option value="CLOTHING">Clothing</option>
                        <option value="BAGS">Bags</option>
                        <option value="HOME_DECOR">Home Decor</option>

                        {/* ❌ DO NOT ADD "KITS" HERE unless you add it to your Java Enum first! */}
                    </select>
                </div>

                <div className="mb-6">
                    <label htmlFor="imageUrl" className="block text-gray-700 text-sm font-bold mb-2">Image URL</label>
                    <input
                        type="url"
                        id="imageUrl"
                        name="imageUrl"
                        value={formData.imageUrl}
                        onChange={handleChange}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                    />
                </div>

                <div className="flex items-center justify-between">
                    <button
                        type="submit"
                        className="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline disabled:opacity-50"
                        disabled={loading}
                    >
                        {loading ? "Adding Product..." : "Add Product"}
                    </button>
                    <button
                        type="button"
                        onClick={() => navigate("/vendor/dashboard")}
                        className="bg-gray-500 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                    >
                        Cancel
                    </button>
                </div>
            </form>
        </div>
    );
};

export default AddProduct;
