import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';

import PageLayout from './components/layout/PageLayout';
import ProtectedRoute from './components/layout/ProtectedRoute';
import AdminRoute from './components/layout/AdminRoute';
import AuthRoute from './components/layout/AuthRoute';
import ErrorBoundary from './components/ui/ErrorBoundary';

import HomePage from './pages/HomePage';
import AboutPage from './pages/AboutPage';
import SearchPage from './pages/SearchPage';
import HotelDetailPage from './pages/HotelDetailPage';
import BookingFlowPage from './pages/BookingFlowPage';
import MyBookingsPage from './pages/MyBookingsPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';

import AdminDashboard from './pages/admin/AdminDashboard';
import ManageHotels from './pages/admin/ManageHotels';
import ManageRooms from './pages/admin/ManageRooms';
import AdminBookings from './pages/admin/AdminBookings';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

const App = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 4000,
            success: {
              style: {
                background: 'var(--color-terracotta)',
                color: 'var(--color-text-on-dark)',
                fontFamily: 'var(--font-body)',
                fontSize: '14px',
                fontWeight: '500',
              },
            },
            error: {
              style: {
                background: 'var(--color-red)',
                color: 'var(--color-text-on-dark)',
                fontFamily: 'var(--font-body)',
                fontSize: '14px',
                fontWeight: '500',
              },
            },
            style: {
              background: 'var(--color-beige)',
              color: 'var(--color-text-primary)',
              fontFamily: 'var(--font-body)',
              fontSize: '14px',
            },
          }}
        />
        <ErrorBoundary>
          <Routes>
            <Route path="/" element={<PageLayout><HomePage /></PageLayout>} />
            <Route path="/about" element={<PageLayout><AboutPage /></PageLayout>} />
            <Route path="/search" element={<PageLayout><SearchPage /></PageLayout>} />
            <Route path="/hotels/:id" element={<PageLayout><HotelDetailPage /></PageLayout>} />

            <Route path="/login" element={<AuthRoute><LoginPage /></AuthRoute>} />
            <Route path="/register" element={<AuthRoute><RegisterPage /></AuthRoute>} />

            <Route
              path="/booking/:hotelId"
              element={
                <ProtectedRoute>
                  <PageLayout><BookingFlowPage /></PageLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/my-bookings"
              element={
                <ProtectedRoute>
                  <PageLayout><MyBookingsPage /></PageLayout>
                </ProtectedRoute>
              }
            />

            <Route path="/admin" element={<AdminRoute><AdminDashboard /></AdminRoute>} />
            <Route path="/admin/hotels" element={<AdminRoute><ManageHotels /></AdminRoute>} />
            <Route path="/admin/hotels/:id/rooms" element={<AdminRoute><ManageRooms /></AdminRoute>} />
            <Route path="/admin/bookings" element={<AdminRoute><AdminBookings /></AdminRoute>} />
          </Routes>
        </ErrorBoundary>
      </BrowserRouter>
    </QueryClientProvider>
  );
};

export default App;
