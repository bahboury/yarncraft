import {useContext} from "react";
import {Link, useNavigate} from "react-router-dom";
import {AuthContext} from "../context/AuthContext";

const Navbar = () => {
    const {user, logout} = useContext(AuthContext);
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate("/login");
    };

    // 👇 FIXED: Removed "!user" so only logged-in Customers see the link.
    // This prevents guest users from clicking 'Shop' and crashing the app.
    const isCustomerView = user && user.role === 'CUSTOMER';

    const isVendor = user?.role === 'VENDOR';
    const isAdmin = user?.role === 'ADMIN';

    return (
        <nav className="bg-blue-600 text-white p-4 shadow-md flex justify-between items-center">
            {/* Left: Logo */}
            <Link to="/" className="text-2xl font-bold flex items-center gap-2">
                🧶 YarnCraft
            </Link>

            {/* Right: Links based on Role */}
            <div className="flex gap-4 items-center">

                {/* Shop Link: Visible ONLY to Logged-in Customers */}
                {isCustomerView && (
                    <Link to="/" className="hover:text-blue-200 font-medium">Shop</Link>
                )}

                {/* Cart & Orders: Only visible to Customers */}
                {user && user.role === 'CUSTOMER' && (
                    <>
                        <Link to="/cart" className="hover:text-blue-200 font-bold flex items-center gap-1">
                            Cart 🛒
                        </Link>
                        <Link to="/orders" className="hover:text-blue-200 font-medium">My Orders 📦</Link>
                    </>
                )}

                {/* IF LOGGED IN */}
                {user ? (
                    <>
                        <span className="font-semibold border-l pl-4 border-blue-400 hidden md:block">
                            Hi, {user.name}
                        </span>

                        {/* VENDOR ONLY BUTTONS */}
                        {isVendor && (
                            <Link to="/vendor/dashboard"
                                  className="bg-purple-500 px-3 py-1 rounded hover:bg-purple-600 shadow">
                                Dashboard
                            </Link>
                        )}

                        {/* ADMIN ONLY BUTTONS */}
                        {isAdmin && (
                            <Link to="/admin" className="bg-red-500 px-3 py-1 rounded hover:bg-red-600 shadow">
                                Admin Panel
                            </Link>
                        )}

                        <button
                            onClick={handleLogout}
                            className="bg-white text-blue-600 px-3 py-1 rounded hover:bg-gray-100 font-bold shadow"
                        >
                            Logout
                        </button>
                    </>
                ) : (
                    /* IF NOT LOGGED IN */
                    <>
                        <Link to="/login" className="hover:text-blue-200 font-medium">Login</Link>
                        <Link to="/register"
                              className="bg-green-500 px-3 py-1 rounded hover:bg-green-600 font-bold shadow">
                            Register
                        </Link>
                    </>
                )}
            </div>
        </nav>
    );
};

export default Navbar;