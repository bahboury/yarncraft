import {useState} from "react";
import api from "../api/axios";
import {Link, useNavigate} from "react-router-dom";

const Register = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        name: "",
        email: "",
        password: "",
        role: "CUSTOMER",
    });

    const handleChange = (e) => {
        setFormData({...formData, [e.target.name]: e.target.value});
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await api.post("/auth/register", formData);
            alert("Registration Successful! Please Login.");
            navigate("/login");
        } catch (error) {
            alert("Registration Failed! " + (error.response?.data?.message || "Error occurred"));
        }
    };

    return (
        // CONTAINER: Full width, Full height, Centered Content, Light Background
        <div className="min-h-[calc(100vh-64px)] w-full flex justify-center items-center bg-gray-50 px-4">

            {/* CARD: White background, shadow, max width for desktop, full width for mobile */}
            <form onSubmit={handleSubmit}
                  className="bg-white p-8 rounded-lg shadow-lg w-full max-w-md border border-gray-200">

                <h2 className="text-3xl font-bold mb-6 text-center text-gray-800">Join YarnCraft</h2>

                <div className="space-y-4">
                    <div>
                        <label className="block text-gray-700 font-medium mb-1">Full Name</label>
                        <input
                            name="name"
                            type="text"
                            placeholder="e.g. John Doe"
                            className="w-full border border-gray-300 p-3 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div>
                        <label className="block text-gray-700 font-medium mb-1">Email Address</label>
                        <input
                            name="email"
                            type="email"
                            placeholder="name@example.com"
                            className="w-full border border-gray-300 p-3 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div>
                        <label className="block text-gray-700 font-medium mb-1">Password</label>
                        <input
                            name="password"
                            type="password"
                            placeholder="••••••••"
                            className="w-full border border-gray-300 p-3 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div>
                        <label className="block text-gray-700 font-medium mb-1">I want to be a:</label>
                        <select
                            name="role"
                            value={formData.role}
                            onChange={handleChange}
                            className="w-full border border-gray-300 p-3 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                        >
                            <option value="CUSTOMER">Customer (I want to buy)</option>
                            <option value="VENDOR">Vendor (I want to sell)</option>
                        </select>
                    </div>
                </div>

                <button
                    className="w-full bg-blue-600 text-white font-bold p-3 rounded-lg mt-6 hover:bg-blue-700 transition duration-300">
                    Create Account
                </button>

                <p className="mt-6 text-center text-gray-600">
                    Already have an account? <Link to="/login" className="text-blue-600 font-semibold hover:underline">Login
                    here</Link>
                </p>
            </form>
        </div>
    );
};

export default Register;