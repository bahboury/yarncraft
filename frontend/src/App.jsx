import {Route, Routes} from "react-router-dom";
import Navbar from "./components/Navbar.jsx";
import Home from "./pages/Home";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";

// Vendor Pages
import Dashboard from "./pages/vendor/Dashboard.jsx";
import AddProduct from "./pages/vendor/AddProduct.jsx";

// Customer Pages
import ProductDetails from "./pages/ProductDetails.jsx";
import Cart from "./pages/customer/Cart.jsx";
import Checkout from "./pages/customer/Checkout.jsx";
import Orders from "./pages/customer/Orders.jsx";
import AdminPanel from "./pages/admin/AdminPanel.jsx";
import VendorAnalytics from "./pages/admin/VendorAnalytics.jsx";

function App() {
    return (
        <>
            <Navbar/>
            <div className="container mx-auto p-4">
                <Routes>
                    {/* Public Routes */}
                    <Route path="/login" element={<Login/>}/>
                    <Route path="/register" element={<Register/>}/>
                    <Route path="/" element={<Home/>}/>
                    <Route path="/product/:id" element={<ProductDetails/>}/>

                    {/* Vendor Routes */}
                    <Route path="/vendor/dashboard" element={<Dashboard/>}/>
                    <Route path="/vendor/add-product" element={<AddProduct/>}/>

                    {/* Customer Routes */}
                    <Route path="/cart" element={<Cart/>}/>
                    <Route path="/checkout" element={<Checkout/>}/>
                    <Route path="/orders" element={<Orders/>}/>

                    <Route path="/admin" element={<AdminPanel/>}/>
                    <Route path="/admin/analytics" element={<VendorAnalytics/>}/>
                </Routes>
            </div>
        </>
    );
}

export default App;