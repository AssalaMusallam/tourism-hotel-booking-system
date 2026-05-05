import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';

import PageLayout from './components/layout/PageLayout';
import ProtectedRoute from './components/layout/ProtectedRoute';
import AdminRoute from './components/layout/AdminRoute';
import AuthRoute from './components/layout/AuthRoute';
import ErrorBoundary from './components/ui/ErrorBoundary';

import HomePage from './pages/HomePage';
import SearchPage from './pages/SearchPage';
import HotelDetailPage from './pages/HotelDetailPage';
import AvailabilityPage from './pages/AvailabilityPage';
import BookingFlowPage from './pages/BookingFlowPage';
import BookingConfirmationPage from './pages/BookingConfirmationPage';
import MyBookingsPage from './pages/MyBookingsPage';
import PaymentPage from './pages/PaymentPage';
import PaymentReceiptPage from './pages/PaymentReceiptPage';
import PaymentHistoryPage from './pages/PaymentHistoryPage';
import ReviewPage from './pages/ReviewPage';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';

import AdminDashboard from './pages/admin/AdminDashboard';
import ManageHotels from './pages/admin/ManageHotels';
import ManageRooms from './pages/admin/ManageRooms';
import ManageAmenities from './pages/admin/ManageAmenities';

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
                fontFamily: "'Inter', sans-serif",
                fontSize: '14px',
                fontWeight: '500',
              },
            },
            error: {
              style: {
                background: 'var(--color-red)',
                color: 'var(--color-text-on-dark)',
                fontFamily: "'Inter', sans-serif",
                fontSize: '14px',
                fontWeight: '500',
              },
            },
            style: {
              background: 'var(--color-beige)',
              color: 'var(--color-text-primary)',
              fontFamily: "'Inter', sans-serif",
              fontSize: '14px',
            },
          }}
        />
        <ErrorBoundary>
          <Routes>
            {/* Public pages with navbar/footer */}
            <Route path="/" element={<PageLayout><HomePage /></PageLayout>} />
            <Route path="/search" element={<PageLayout><SearchPage /></PageLayout>} />
            <Route path="/hotels/:id" element={<PageLayout><HotelDetailPage /></PageLayout>} />
            <Route path="/availability" element={<PageLayout><AvailabilityPage /></PageLayout>} />
            <Route path="/hotels/:hotelId/availability" element={<PageLayout><AvailabilityPage /></PageLayout>} />

            <Route
              path="/hotels/:hotelId/rooms/:roomTypeId/book"
              element={<ProtectedRoute><PageLayout><BookingFlowPage /></PageLayout></ProtectedRoute>}
            />
            <Route
              path="/bookings/confirmation/:id"
              element={<ProtectedRoute><PageLayout><BookingConfirmationPage /></PageLayout></ProtectedRoute>}
            />
            <Route
              path="/bookings/my"
              element={<ProtectedRoute><PageLayout><MyBookingsPage /></PageLayout></ProtectedRoute>}
            />
            <Route
              path="/bookings/:bookingId/pay"
              element={<ProtectedRoute><PageLayout><PaymentPage /></PageLayout></ProtectedRoute>}
            />
            <Route
              path="/payments/:id/receipt"
              element={<ProtectedRoute><PageLayout><PaymentReceiptPage /></PageLayout></ProtectedRoute>}
            />
            <Route
              path="/bookings/:bookingId/payments"
              element={<ProtectedRoute><PageLayout><PaymentHistoryPage /></PageLayout></ProtectedRoute>}
            />
            <Route
              path="/bookings/:bookingId/review"
              element={<ProtectedRoute><PageLayout><ReviewPage /></PageLayout></ProtectedRoute>}
            />
            <Route
              path="/my-bookings"
              element={<ProtectedRoute><PageLayout><MyBookingsPage /></PageLayout></ProtectedRoute>}
            />

            {/* Auth pages (redirect away if logged in) */}
            <Route path="/login" element={<AuthRoute><LoginPage /></AuthRoute>} />
            <Route path="/register" element={<AuthRoute><RegisterPage /></AuthRoute>} />

            {/* Admin pages (ADMIN or MANAGER required) */}
            <Route path="/admin" element={<AdminRoute><AdminDashboard /></AdminRoute>} />
            <Route path="/admin/hotels" element={<AdminRoute><ManageHotels /></AdminRoute>} />
            <Route path="/admin/hotels/:hotelId/rooms" element={<AdminRoute><ManageRooms /></AdminRoute>} />
            <Route path="/admin/amenities" element={<AdminRoute><ManageAmenities /></AdminRoute>} />
          </Routes>
        </ErrorBoundary>
      </BrowserRouter>
    </QueryClientProvider>
  );
};

export default App;
