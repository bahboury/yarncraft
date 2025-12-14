import {createContext, useEffect, useState} from "react";
import api from "../api/axios"; // Import our bridge

export const AuthContext = createContext();

export const AuthProvider = ({children}) => {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(localStorage.getItem("token"));
    const [loading, setLoading] = useState(true); // New: Wait until we know who user is

    useEffect(() => {
        const fetchUser = async () => {
            if (token) {
                try {
                    console.log("🔍 Token found! Asking backend who I am..."); // DEBUG LOG 1

                    const response = await api.get("/users/me");

                    console.log("📦 RAW BACKEND RESPONSE:", response); // DEBUG LOG 2
                    console.log("👤 USER DATA:", response.data);      // DEBUG LOG 3

                    // TRY THIS FIX: Check if data is nested inside 'data' or usually direct
                    // If your backend returns { status: "success", data: { name: "Alice" } } -> use response.data.data
                    // If your backend returns { name: "Alice" } -> use response.data

                    const userData = response.data.data || response.data;

                    console.log("✅ SETTING USER TO:", userData); // DEBUG LOG 4
                    setUser(userData);

                } catch (error) {
                    console.error("❌ Error fetching user:", error);
                    // logout(); // Comment this out temporarily so we can see the error!
                }
            }
            setLoading(false);
        };

        fetchUser(); // No need for .then(r => ) if you're not doing anything with the result
    }, [token]);

    const login = (newToken) => {
        localStorage.setItem("token", newToken);
        setToken(newToken);
    };

    const logout = () => {
        localStorage.removeItem("token");
        setToken(null);
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{user, login, logout, loading}}>
            {!loading && children} {/* Don't show app until we know who user is */}
        </AuthContext.Provider>
    );
};