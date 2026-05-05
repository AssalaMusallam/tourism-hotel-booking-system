# TerraStay — Holy Land Tourism Hotel Booking Platform

A production-quality React frontend for booking hotels across Palestine (Jerusalem, Bethlehem, Nazareth, Hebron, and more). Built for SWER354 Advanced Web Technologies at Bethlehem University.

---

## Color Palette

| Variable | Hex | Usage |
|---|---|---|
| `--color-terracotta` | `#A65A3A` | Primary brand, CTAs, active states |
| `--color-terracotta-dark` | `#8B4A2E` | Hover states for primary |
| `--color-terracotta-light` | `#C4784F` | Light accents |
| `--color-beige` | `#D8C3A5` | Borders, dividers, secondary surfaces |
| `--color-beige-light` | `#EDE0CE` | Hover surfaces |
| `--color-ivory` | `#F5F0E6` | Page background, cards |
| `--color-ivory-dark` | `#EDE5D5` | Alternate rows, subtle backgrounds |
| `--color-red` | `#7A1E1E` | Danger, errors, destructive actions |
| `--color-red-light` | `#9B2626` | Hover for red elements |
| `--color-text-primary` | `#2C1810` | Main body text |
| `--color-text-secondary` | `#6B4C3B` | Secondary text |
| `--color-text-muted` | `#9E7E6B` | Placeholders, hints |
| `--color-text-on-dark` | `#F5F0E6` | Text on dark backgrounds |

Inspired by Palestinian tatreez (embroidery) — terracotta reds, warm beiges, soft ivory.

---

## Tech Stack

| Library | Purpose |
|---|---|
| React 18 + Vite | UI framework + build tool |
| React Router DOM v6 | Client-side routing |
| TanStack Query v5 | Data fetching, caching, deduplication |
| React Hook Form + Zod | Form state + schema validation |
| Axios | HTTP client with interceptors |
| Zustand | Global auth state (persisted to localStorage) |
| React Hot Toast | Notifications styled with palette |
| Lucide React | Icons |
| date-fns | Date calculations |
| Framer Motion | Page transitions + micro-animations |

No CSS frameworks. All styles are CSS Modules + plain CSS.

---

## Getting Started

```bash
# Navigate to the frontend directory
cd terrastay

# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

The dev server runs at `http://localhost:5173`.

---

## Environment Variables

Create a `.env` file in the `terrastay/` directory:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

See `.env.example` for reference. The app works fully offline with mock data when the backend is unavailable — all API calls fall back to 10 realistic Palestinian hotel records and 20 mock bookings automatically.

---

## Demo Credentials

| Role | Email | Password |
|---|---|---|
| Admin | `admin@terrastay.ps` | `admin123` |
| Guest | any email | `password` |

---

## Folder Structure

```
src/
├── api/              # Axios instance + endpoint functions (with mock fallbacks)
│   ├── axios.js      # Base instance, auth + 401 interceptors
│   ├── auth.js       # Login, register, logout
│   ├── hotels.js     # Hotel CRUD + availability + room types
│   ├── bookings.js   # Create, list, cancel bookings
│   ├── admin.js      # Admin stats endpoint
│   └── mockData.js   # 10 hotels + 20 bookings offline fallback
├── components/
│   ├── layout/       # Navbar, Footer, PageLayout, ProtectedRoute, AdminRoute
│   ├── ui/           # Button, Input, Select, Modal, Badge, Card, Spinner, etc.
│   ├── search/       # HeroSearchBar, SearchFilters, HotelCard
│   ├── hotel/        # ImageGallery, AmenitiesList, RoomTypeCard, PoliciesSection
│   ├── booking/      # PriceBreakdown, BookingCard
│   └── admin/        # StatsCard, HotelForm, RoomForm, BookingRow
├── pages/
│   ├── HomePage.jsx          # Hero + cities + featured hotels + features
│   ├── AboutPage.jsx
│   ├── SearchPage.jsx        # URL-synced search, filters, pagination, sort
│   ├── HotelDetailPage.jsx   # Gallery, tabs: Overview/Rooms/Amenities/Policies
│   ├── BookingFlowPage.jsx   # 3-step: Details → Payment → Confirmation
│   ├── MyBookingsPage.jsx    # Authenticated booking list + cancel
│   ├── LoginPage.jsx         # Email + password, Enter key submit, return URL
│   ├── RegisterPage.jsx      # Full registration with role selector
│   └── admin/
│       ├── AdminDashboard.jsx  # Stats cards + recent bookings table
│       ├── ManageHotels.jsx    # Hotel CRUD with optimistic deletes
│       ├── ManageRooms.jsx     # Room type management per hotel
│       └── AdminBookings.jsx   # Filterable bookings table with expand
├── hooks/
│   ├── useAuth.js           # Reads from Zustand, exposes isAdmin / isGuest
│   ├── useDebounce.js
│   └── useSearchParams.js   # URL param helpers (getParam, setParam, setParams)
├── store/
│   └── authStore.js         # Zustand: user, token, isAuthenticated
├── utils/
│   ├── formatPrice.js       # Intl.NumberFormat for USD/ILS
│   ├── formatDate.js        # format, formatDateShort, getNights, toInputDate
│   └── cn.js                # className merger
├── constants/
│   ├── cities.js            # Palestinian cities with Arabic labels + colors
│   └── amenities.js         # Amenity list with Lucide icon names
└── styles/
    ├── variables.css         # All CSS custom properties
    ├── reset.css             # CSS reset
    └── global.css            # Typography, skeleton animation, embroidery pattern
```

---

## API Integration

All API calls try the real backend first and fall back to mock data on any network error:

```js
export const getHotels = async (params) => {
  try {
    const { data } = await api.get('/hotels', { params });
    return data;
  } catch {
    // Returns filtered/sorted/paginated mock data
  }
};
```

**Auth flow:**
1. `POST /api/auth/login` → `{ token, user: { id, name, email, role } }`
2. Token stored in `localStorage` as `terrastay_token`
3. Axios request interceptor attaches `Authorization: Bearer <token>`
4. On 401, auth is cleared and user is redirected to `/login`

**Backend endpoints expected:**

```
POST /api/auth/login
POST /api/auth/register
POST /api/auth/logout

GET  /api/hotels             ?city, featured, minPrice, maxPrice, stars, amenities, sort, page, limit
GET  /api/hotels/:id
GET  /api/hotels/:id/availability?checkIn&checkOut&guests
GET  /api/hotels/:id/rooms
POST /api/hotels             (admin)
PUT  /api/hotels/:id         (admin)
DELETE /api/hotels/:id       (admin)

POST /api/hotels/:id/rooms   (admin)
PUT  /api/rooms/:id          (admin)
DELETE /api/rooms/:id        (admin)

POST /api/bookings
GET  /api/bookings/my
GET  /api/bookings/:id
DELETE /api/bookings/:id
GET  /api/admin/bookings     (admin)
GET  /api/admin/stats        (admin)
```

---

## Key UX Patterns

| Pattern | Implementation |
|---|---|
| Deferred Auth | Browse/search/view without login. Login only required on "Book Now" |
| Booking Intent | `sessionStorage.pendingBooking` saves hotel+room before redirect to login |
| Return URL | `location.state.from` preserves intended destination through login |
| URL Search Sync | All search params live in URL query string (shareable links) |
| Skeleton Loading | Animated ivory/beige skeletons — no blank screens |
| Optimistic Updates | Hotel deletes update UI immediately before server confirmation |
| Toast Styles | Success=terracotta, Error=red, Info=beige (palette-matched) |
| Enter Key Submit | All forms submit on Enter (keyboard-first design) |

---

## Routing

| Route | Access | Page |
|---|---|---|
| `/` | Public | HomePage |
| `/about` | Public | AboutPage |
| `/search` | Public | SearchPage |
| `/hotels/:id` | Public | HotelDetailPage |
| `/login` | Redirect if logged in | LoginPage |
| `/register` | Redirect if logged in | RegisterPage |
| `/booking/:hotelId` | Auth required | BookingFlowPage |
| `/my-bookings` | Auth required | MyBookingsPage |
| `/admin` | Admin only | AdminDashboard |
| `/admin/hotels` | Admin only | ManageHotels |
| `/admin/hotels/:id/rooms` | Admin only | ManageRooms |
| `/admin/bookings` | Admin only | AdminBookings |

---

## Screenshots

> Run `npm run dev` and visit these URLs after starting the dev server.

| Page | URL |
|---|---|
| Homepage with hero | `http://localhost:5173/` |
| Search (Jerusalem) | `http://localhost:5173/search?city=Jerusalem` |
| Hotel detail | `http://localhost:5173/hotels/1` |
| Booking wizard | `http://localhost:5173/hotels/1` → Select Room |
| My bookings | `http://localhost:5173/my-bookings` |
| Admin dashboard | `http://localhost:5173/admin` |

---

## Team

| Name | Student ID | Role |
|---|---|---|
| *(Add name)* | *(Add ID)* | Frontend Development |
| *(Add name)* | *(Add ID)* | *(Role)* |

---

*Built with ♥ in Bethlehem, Palestine — SWER354 Advanced Web Technologies, 2025*