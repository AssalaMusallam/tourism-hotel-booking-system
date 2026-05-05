import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';

import PageLayout from './components/layout/PageLayout';
import ProtectedRoute from './components/layout/ProtectedRoute';
import AdminRoute from './components/layout/AdminRoute';
import AuthRoute from './components/layout/AuthRoute';
import ErrorBoundary from './components/ui/ErrorBoundary';
import { AuthProvider } from './context/AuthContext';
import { CurrencyProvider } from './context/CurrencyContext';
import { ThemeProvider } from './context/ThemeContext';

import HomePage from './pages/HomePage';
import SearchPage from './pages/SearchPage';
import HotelDetailPage from './pages/HotelDetailPage';
import AvailabilityPage from './pages/AvailabilityPage';
import BookingFlowPage from './pages/BookingFlowPage';
import BookingConfirmationPage from './pages/BookingConfirmationPage';
import MyBookingsPage from './pages/MyBookingsPage';
import MyWaitingListPage from './pages/MyWaitingListPage';
import PaymentPage from './pages/PaymentPage';
import PaymentReceiptPage from './pages/PaymentReceiptPage';
import PaymentHistoryPage from './pages/PaymentHistoryPage';
import ReviewPage from './pages/ReviewPage';
import ProfilePage from './pages/ProfilePage';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';

import AdminDashboard from './pages/admin/AdminDashboard';
import ManageHotels from './pages/admin/ManageHotels';
import ManageRooms from './pages/admin/ManageRooms';
import ManageAmenities from './pages/admin/ManageAmenities';
import AdminBookings from './pages/admin/AdminBookings';
import ManageUsers from './pages/admin/ManageUsers';
import HotelManagersPage from './pages/admin/HotelManagersPage';
import ManagePricingRules from './pages/admin/ManagePricingRules';
import HotelAvailabilityPage from './pages/hotels/HotelAvailabilityPage';
import AdminReportsPage from './pages/admin/AdminReportsPage';
import NotificationsPage from './pages/admin/notifications/NotificationsPage';
import NotificationDetailPage from './pages/admin/notifications/NotificationDetailPage';
import UnauthorizedPage from './pages/UnauthorizedPage';

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
        <AuthProvider>
          <ThemeProvider>
          <CurrencyProvider>
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
            <Route path="/hotels" element={<PageLayout><SearchPage /></PageLayout>} />
            <Route path="/hotels/:id" element={<PageLayout><HotelDetailPage /></PageLayout>} />
            <Route path="/availability" element={<PageLayout><AvailabilityPage /></PageLayout>} />
            <Route path="/hotels/:hotelId/availability" element={<PageLayout><HotelAvailabilityPage /></PageLayout>} />

            <Route
              path="/hotels/:hotelId/rooms/:roomTypeId/book"
              element={<ProtectedRoute roles={['GUEST']} requireActive><PageLayout><BookingFlowPage /></PageLayout></ProtectedRoute>}
            />
            <Route
              path="/profile"
              element={<ProtectedRoute><PageLayout><ProfilePage /></PageLayout></ProtectedRoute>}
            />
            <Route
              path="/bookings/confirmation/:id"
              element={<ProtectedRoute><PageLayout><BookingConfirmationPage /></PageLayout></ProtectedRoute>}
            />
            <Route
              path="/bookings/my"
              element={<ProtectedRoute roles={['GUEST']}><PageLayout><MyBookingsPage /></PageLayout></ProtectedRoute>}
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
              element={<ProtectedRoute roles={['GUEST']}><PageLayout><MyBookingsPage /></PageLayout></ProtectedRoute>}
            />
            <Route
              path="/my-waiting-list"
              element={<ProtectedRoute roles={['GUEST']}><PageLayout><MyWaitingListPage /></PageLayout></ProtectedRoute>}
            />

            {/* Auth pages (redirect away if logged in) */}
            <Route path="/login" element={<AuthRoute><LoginPage /></AuthRoute>} />
            <Route path="/register" element={<AuthRoute><RegisterPage /></AuthRoute>} />
            <Route path="/unauthorized" element={<PageLayout><UnauthorizedPage /></PageLayout>} />

            {/* Admin pages (ADMIN or MANAGER required) */}
            <Route path="/admin" element={<AdminRoute><AdminDashboard /></AdminRoute>} />
            <Route path="/admin/hotels" element={<AdminRoute><ManageHotels /></AdminRoute>} />
            <Route path="/admin/hotels/:hotelId/rooms" element={<AdminRoute><ManageRooms /></AdminRoute>} />
            <Route path="/admin/amenities" element={<AdminRoute><ManageAmenities /></AdminRoute>} />
            <Route path="/admin/bookings" element={<AdminRoute><AdminBookings /></AdminRoute>} />
            <Route path="/admin/users" element={<AdminRoute><ManageUsers /></AdminRoute>} />
            <Route path="/admin/hotels/:hotelId/managers" element={<AdminRoute><HotelManagersPage /></AdminRoute>} />
            <Route
              path="/admin/notifications"
              element={<ProtectedRoute roles={['ADMIN']}><NotificationsPage /></ProtectedRoute>}
            />
            <Route path="/admin/notifications/:id" element={<AdminRoute><NotificationDetailPage /></AdminRoute>} />

            {/* Dashboard aliases requested by the production spec */}
            <Route path="/dashboard" element={<AdminRoute><AdminDashboard /></AdminRoute>} />
            <Route path="/dashboard/hotels" element={<AdminRoute><ManageHotels /></AdminRoute>} />
            <Route path="/dashboard/hotels/:hotelId/rooms" element={<AdminRoute><ManageRooms /></AdminRoute>} />
            <Route path="/dashboard/bookings" element={<AdminRoute><AdminBookings /></AdminRoute>} />
            <Route path="/dashboard/users" element={<AdminRoute><ManageUsers /></AdminRoute>} />
            <Route path="/dashboard/hotels/:hotelId/managers" element={<AdminRoute><HotelManagersPage /></AdminRoute>} />
            <Route
              path="/dashboard/notifications"
              element={<ProtectedRoute roles={['ADMIN']}><NotificationsPage /></ProtectedRoute>}
            />
            <Route path="/admin/pricing-rules" element={<AdminRoute><ManagePricingRules /></AdminRoute>} />
            <Route path="/dashboard/pricing-rules" element={<AdminRoute><ManagePricingRules /></AdminRoute>} />
            <Route
              path="/dashboard/reports"
              element={<ProtectedRoute roles={['ADMIN']}><AdminReportsPage /></ProtectedRoute>}
            />
          </Routes>
          </ErrorBoundary>
          </CurrencyProvider>
          </ThemeProvider>
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
};

export default App;
