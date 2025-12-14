import {useContext, useState} from "react";
import {AuthContext} from "../context/AuthContext";
import api from "../api/axios";
import {Link, useNavigate} from "react-router-dom";

const Login = () => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const {login} = useContext(AuthContext);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            console.log("🔵 Attempting to login..."); // Debug 1

            const response = await api.post("/auth/login", {email, password});

            console.log("📦 SERVER RESPONSE:", response.data); // Debug 2: LOOK AT THIS IN CONSOLE

            // 🕵️ SEARCH FOR THE TOKEN
            // Try path 1: response.data.data.token (Common for wrapped responses)
            // Try path 2: response.data.token (Common for direct responses)
            const token = response.data?.data?.token || response.data?.token;

            if (token) {
                console.log("✅ Token found:", token);
                login(token); // Save it!
                alert("Login Successful!");
                navigate("/");
            } else {
                console.error("❌ Token NOT found in response!");
                alert("Login failed: Server didn't send a token.");
            }

        } catch (error) {
            console.error("🔴 Login Error:", error);
            alert("Login Failed! " + (error.response?.data?.message || "Check console for details"));
        }
    };

    return (
        <div className="min-h-[calc(100vh-64px)] w-full flex justify-center items-center bg-gray-50 px-4">
            <form onSubmit={handleSubmit}
                  className="bg-white p-8 rounded-lg shadow-lg w-full max-w-md border border-gray-200">
                <h2 className="text-3xl font-bold mb-6 text-center text-gray-800">Welcome Back</h2>

                <div className="space-y-4">
                    <div>
                        <label className="block text-gray-700 font-medium mb-1">Email</label>
                        <input
                            type="email"
                            placeholder="Enter your email"
                            className="w-full border border-gray-300 p-3 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                        />
                    </div>

                    <div>
                        <label className="block text-gray-700 font-medium mb-1">Password</label>
                        <input
                            type="password"
                            placeholder="••••••••"
                            className="w-full border border-gray-300 p-3 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />
                    </div>
                </div>

                <button
                    className="w-full bg-blue-600 text-white font-bold p-3 rounded-lg mt-6 hover:bg-blue-700 transition duration-300">
                    Login
                </button>

                <p className="mt-6 text-center text-gray-600">
                    Don't have an account? <Link to="/register" className="text-blue-600 font-semibold hover:underline">Register
                    here</Link>
                </p>
            </form>
        </div>
    );
};

export default Login;