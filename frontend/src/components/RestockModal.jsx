import React, {useState} from 'react';
import api from '../api/axios';

const RestockModal = ({product, onClose, onRestockSuccess}) => {
    // We use the product's ID to know which item to restock
    const [quantity, setQuantity] = useState(10); // Default to 10 for convenience
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const handleRestock = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        const restockQuantity = parseInt(quantity);

        if (restockQuantity <= 0 || isNaN(restockQuantity)) {
            setError("Quantity must be a positive number.");
            setLoading(false);
            return;
        }

        try {
            // Call the RESTOCK endpoint you built in InventoryController:
            // PUT /api/inventory/restock/{productId}?quantity={quantity}
            await api.put(`/inventory/restock/${product.id}?quantity=${restockQuantity}`);

            alert(`Successfully restocked ${product.name} with ${restockQuantity} units!`);

            onRestockSuccess(); // Tell the dashboard to refresh
            onClose(); // Close the modal

        } catch (err) {
            console.error("Restock failed:", err);
            setError(err.response?.data?.message || "Failed to restock inventory.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-75 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6">

                <h2 className="text-2xl font-bold mb-4 text-gray-800">Restock Inventory: {product.name}</h2>
                <p className="text-gray-600 mb-4">Current Price: ${product.price}</p>

                {error && (
                    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-2 rounded mb-4 text-sm">
                        {error}
                    </div>
                )}

                <form onSubmit={handleRestock} className="space-y-4">
                    <div>
                        <label htmlFor="quantity" className="block text-sm font-medium text-gray-700 mb-1">
                            Units to Add
                        </label>
                        <input
                            type="number"
                            id="quantity"
                            value={quantity}
                            onChange={(e) => setQuantity(e.target.value)}
                            min="1"
                            required
                            className="w-full border p-2 rounded-md focus:ring-blue-500 focus:border-blue-500"
                        />
                    </div>

                    <div className="flex justify-end space-x-3 pt-4">
                        <button
                            type="button"
                            onClick={onClose}
                            className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-200 rounded-md hover:bg-gray-300"
                            disabled={loading}
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className="px-4 py-2 text-sm font-medium text-white bg-green-600 rounded-md hover:bg-green-700 disabled:bg-gray-400"
                            disabled={loading}
                        >
                            {loading ? 'Restocking...' : `Add ${quantity} Units`}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default RestockModal;