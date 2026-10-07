import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import MainLayout from './layouts/MainLayout'
import Login from './pages/Login'
import Customer from './pages/Customer'
import Supplier from './pages/Supplier'
import Product from './pages/Product'
import User from './pages/User'
import Purchase from './pages/Purchase'
import PurchaseEdit from './pages/PurchaseEdit'
import Sale from './pages/Sale'
import SaleEdit from './pages/SaleEdit'
import Stock from './pages/Stock'

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<MainLayout />}>
          <Route index element={<Navigate to="/stock" replace />} />
          <Route path="customer" element={<Customer />} />
          <Route path="supplier" element={<Supplier />} />
          <Route path="product" element={<Product />} />
          <Route path="user" element={<User />} />
          <Route path="purchase" element={<Purchase />} />
          <Route path="purchase/new" element={<PurchaseEdit />} />
          <Route path="purchase/edit/:poNo" element={<PurchaseEdit />} />
          <Route path="purchase/audit/:poNo" element={<PurchaseEdit />} />
          <Route path="purchase/view/:poNo" element={<PurchaseEdit />} />
          <Route path="sale" element={<Sale />} />
          <Route path="sale/new" element={<SaleEdit />} />
          <Route path="sale/edit/:soNo" element={<SaleEdit />} />
          <Route path="sale/audit/:soNo" element={<SaleEdit />} />
          <Route path="sale/view/:soNo" element={<SaleEdit />} />
          <Route path="stock" element={<Stock />} />
        </Route>
      </Routes>
    </AuthProvider>
  )
}
