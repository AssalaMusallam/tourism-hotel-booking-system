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
import { LanguageProvider } from './context/LanguageContext';
import { FavoritesProvider } from './context/FavoritesContext';
import AdminLayout from './components/layout/AdminLayout';

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
import FavoritesPage from './pages/FavoritesPage';
import SettingsPage from './pages/SettingsPage';
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
import RoleRequestsPage from './pages/admin/RoleRequestsPage';
import AdminPlaceholder from './pages/admin/placeholders/AdminPlaceholder';
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
          <LanguageProvider>
          <ThemeProvider>
          <CurrencyProvider>
          <FavoritesProvider>
          <Toaster
            position="top-right"
            toastOptions={{
              duration: 4000,
              success: {
                style: {
                  background: 'var(--color-success)',
                  color: 'var(--color-text-on-primary)',
                  fontFamily: "'Inter', sans-serif",
                  fontSize: '14px',
                  fontWeight: '500',
                },
              },
              error: {
                style: {
                  background: 'var(--color-danger)',
                  color: 'var(--color-text-on-primary)',
                  fontFamily: "'Inter', sans-serif",
                  fontSize: '14px',
                  fontWeight: '500',
                },
              },
              style: {
                background: 'var(--color-surface)',
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
              path="/favorites"
              element={<PageLayout><FavoritesPage /></PageLayout>}
            />
            <Route
              path="/settings"
              element={<PageLayout><SettingsPage /></PageLayout>}
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
            <Route path="/admin/hotels/new" element={<AdminRoute><AdminLayout title="إضافة فندق"><AdminPlaceholder title="إضافة فندق" /></AdminLayout></AdminRoute>} />
            <Route path="/admin/room-types" element={<AdminRoute><AdminLayout title="أنواع الغرف"><AdminPlaceholder title="أنواع الغرف" /></AdminLayout></AdminRoute>} />
            <Route path="/admin/room-types/new" element={<AdminRoute><AdminLayout title="إضافة غرفة"><AdminPlaceholder title="إضافة غرفة" /></AdminLayout></AdminRoute>} />
            <Route path="/admin/waiting-list" element={<AdminRoute><AdminLayout title="قائمة الانتظار"><AdminPlaceholder title="قائمة الانتظار" /></AdminLayout></AdminRoute>} />
            <Route path="/admin/payments" element={<AdminRoute><AdminLayout title="المدفوعات"><AdminPlaceholder title="المدفوعات" /></AdminLayout></AdminRoute>} />
            <Route path="/admin/reviews" element={<AdminRoute><AdminLayout title="التقييمات"><AdminPlaceholder title="التقييمات" /></AdminLayout></AdminRoute>} />
            <Route path="/admin/settings" element={<AdminRoute><AdminLayout title="الإعدادات"><SettingsPage /></AdminLayout></AdminRoute>} />
            <Route path="/admin" element={<AdminRoute><AdminLayout title="لوحة التحكم"><AdminDashboard /></AdminLayout></AdminRoute>} />
            <Route path="/admin/hotels" element={<AdminRoute><AdminLayout title="الفنادق"><ManageHotels /></AdminLayout></AdminRoute>} />
            <Route path="/admin/hotels/:hotelId/rooms" element={<AdminRoute><AdminLayout title="أنواع الغرف"><ManageRooms /></AdminLayout></AdminRoute>} />
            <Route path="/admin/amenities" element={<AdminRoute><AdminLayout title="وسائل الراحة"><ManageAmenities /></AdminLayout></AdminRoute>} />
            <Route path="/admin/bookings" element={<AdminRoute><AdminLayout title="الحجوزات"><AdminBookings /></AdminLayout></AdminRoute>} />
            <Route path="/admin/users" element={<AdminRoute><AdminLayout title="المستخدمون"><ManageUsers /></AdminLayout></AdminRoute>} />
            <Route path="/admin/hotels/:hotelId/managers" element={<AdminRoute><AdminLayout title="مديرو الفندق"><HotelManagersPage /></AdminLayout></AdminRoute>} />
            <Route
              path="/admin/notifications"
              element={<ProtectedRoute roles={['ADMIN']}><AdminLayout title="الإشعارات"><NotificationsPage /></AdminLayout></ProtectedRoute>}
            />
            <Route path="/admin/notifications/:id" element={<AdminRoute><AdminLayout title="تفاصيل الإشعار"><NotificationDetailPage /></AdminLayout></AdminRoute>} />
            <Route path="/admin/role-requests" element={<AdminRoute><AdminLayout title="طلبات الأدوار"><RoleRequestsPage /></AdminLayout></AdminRoute>} />

            {/* Dashboard aliases requested by the production spec */}
            <Route path="/dashboard" element={<AdminRoute><AdminLayout title="لوحة التحكم"><AdminDashboard /></AdminLayout></AdminRoute>} />
            <Route path="/dashboard/hotels" element={<AdminRoute><AdminLayout title="الفنادق"><ManageHotels /></AdminLayout></AdminRoute>} />
            <Route path="/dashboard/hotels/:hotelId/rooms" element={<AdminRoute><AdminLayout title="أنواع الغرف"><ManageRooms /></AdminLayout></AdminRoute>} />
            <Route path="/dashboard/bookings" element={<AdminRoute><AdminLayout title="الحجوزات"><AdminBookings /></AdminLayout></AdminRoute>} />
            <Route path="/dashboard/users" element={<AdminRoute><AdminLayout title="المستخدمون"><ManageUsers /></AdminLayout></AdminRoute>} />
            <Route path="/dashboard/hotels/:hotelId/managers" element={<AdminRoute><AdminLayout title="مديرو الفندق"><HotelManagersPage /></AdminLayout></AdminRoute>} />
            <Route
              path="/dashboard/notifications"
              element={<ProtectedRoute roles={['ADMIN']}><AdminLayout title="الإشعارات"><NotificationsPage /></AdminLayout></ProtectedRoute>}
            />
            <Route path="/admin/pricing-rules" element={<AdminRoute><AdminLayout title="قواعد التسعير"><ManagePricingRules /></AdminLayout></AdminRoute>} />
            <Route path="/dashboard/pricing-rules" element={<AdminRoute><AdminLayout title="قواعد التسعير"><ManagePricingRules /></AdminLayout></AdminRoute>} />
            <Route
              path="/dashboard/reports"
              element={<ProtectedRoute roles={['ADMIN']}><AdminLayout title="التقارير"><AdminReportsPage /></AdminLayout></ProtectedRoute>}
            />
          </Routes>
          </ErrorBoundary>
          </FavoritesProvider>
          </CurrencyProvider>
          </ThemeProvider>
          </LanguageProvider>
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
};

export default App;
