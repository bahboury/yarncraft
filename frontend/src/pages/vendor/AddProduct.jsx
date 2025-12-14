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
        stockQuantity: "", // 👈 Added Stock field
        category: "YARN", // Default category
        imageUrl: "",
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const handleChange = (e) => {
        const {name, value} = e.target;
        // Parse numbers correctly for price and stock
        let parsedValue = value;
        if (name === "price") parsedValue = parseFloat(value);
        if (name === "stockQuantity") parsedValue = parseInt(value, 10);

        setFormData(prevData => ({
            ...prevData,
            [name]: parsedValue
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
            navigate("/vendor/dashboard");
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

                {/* Name */}
                <div className="mb-4">
                    <label htmlFor="name" className="block text-gray-700 text-sm font-bold mb-2">Product Name</label>
                    <input
                        type="text"
                        name="name"
                        value={formData.name}
                        onChange={handleChange}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                        placeholder="e.g. Merino Wool Skein"
                    />
                </div>

                {/* Description */}
                <div className="mb-4">
                    <label htmlFor="description" className="block text-gray-700 text-sm font-bold mb-2">Description</label>
                    <textarea
                        name="description"
                        value={formData.description}
                        onChange={handleChange}
                        rows="4"
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                        placeholder="Describe the texture, weight, and color..."
                    ></textarea>
                </div>

                <div className="flex gap-4 mb-4">
                    {/* Price */}
                    <div className="w-1/2">
                        <label htmlFor="price" className="block text-gray-700 text-sm font-bold mb-2">Price ($)</label>
                        <input
                            type="number"
                            name="price"
                            value={formData.price}
                            onChange={handleChange}
                            step="0.01"
                            min="0"
                            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                            required
                        />
                    </div>

                    {/* Stock Quantity (NEW) */}
                    <div className="w-1/2">
                        <label htmlFor="stockQuantity" className="block text-gray-700 text-sm font-bold mb-2">Initial Stock</label>
                        <input
                            type="number"
                            name="stockQuantity"
                            value={formData.stockQuantity}
                            onChange={handleChange}
                            min="1"
                            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                            required
                        />
                    </div>
                </div>

                {/* Category (UPDATED) */}
                <div className="mb-4">
                    <label className="block text-gray-700 text-sm font-bold mb-2">Category</label>
                    <select
                        name="category"
                        value={formData.category}
                        onChange={handleChange} // Needs to handle event, not just value
                        className="w-full border border-gray-300 p-2 rounded bg-white focus:outline-none focus:shadow-outline"
                    >
                        <option value="YARN">Yarn</option>
                        <option value="CROCHET_HOOKS">Crochet Hooks</option>
                        <option value="KNITTING_NEEDLES">Knitting Needles</option>
                        <option value="ACCESSORIES">Accessories</option>
                        <option value="TOOLS">Tools</option>
                    </select>
                </div>

                {/* Image URL */}
                <div className="mb-6">
                    <label htmlFor="imageUrl" className="block text-gray-700 text-sm font-bold mb-2">Image URL</label>
                    <input
                        type="url"
                        name="imageUrl"
                        value={formData.imageUrl}
                        onChange={handleChange}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        placeholder="https://example.com/image.jpg"
                    />
                </div>

                {/* Buttons */}
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