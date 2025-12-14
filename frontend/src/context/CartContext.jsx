import {createContext, useEffect, useState} from "react";

export const CartContext = createContext();

export const CartProvider = ({children}) => {
    // Load cart from LocalStorage
    const [cart, setCart] = useState(() => {
        const savedCart = localStorage.getItem("yarn_cart");
        return savedCart ? JSON.parse(savedCart) : [];
    });

    useEffect(() => {
        localStorage.setItem("yarn_cart", JSON.stringify(cart));
    }, [cart]);

    // Add Item
    const addToCart = (product, amount = 1) => {
        setCart((prevCart) => {
            const existingItem = prevCart.find((item) => item.id === product.id);
            if (existingItem) {
                return prevCart.map((item) =>
                    item.id === product.id ? {...item, quantity: item.quantity + amount} : item
                );
            }
            return [...prevCart, {...product, quantity: amount}];
        });
        alert(`Added ${amount} item(s) to Cart! 🛒`);
    };

    // 👇 NEW: Update Quantity directly (for the Cart Page)
    const updateQuantity = (productId, newQuantity) => {
        if (newQuantity < 1) return; // Stop if user tries to go below 1
        setCart((prevCart) => prevCart.map((item) =>
            item.id === productId ? {...item, quantity: newQuantity} : item
        ));
    };

    // Remove Item
    const removeFromCart = (productId) => {
        setCart((prevCart) => prevCart.filter((item) => item.id !== productId));
    };

    // Clear Cart
    const clearCart = () => {
        setCart([]);
    };

    const getCartTotal = () => {
        return cart.reduce((total, item) => total + item.price * item.quantity, 0);
    };

    return (
        <CartContext.Provider value={{
            cart,
            addToCart,
            updateQuantity, // 👈 Don't forget to export this!
            removeFromCart,
            clearCart,
            getCartTotal
        }}>
            {children}
        </CartContext.Provider>
    );
};