import { useEffect, useRef, useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import {
  Bell,
  CalendarCheck,
  ChevronDown,
  Heart,
  LayoutDashboard,
  LogOut,
  Menu,
  Search,
  Settings,
  UserCircle,
  X,
} from 'lucide-react';
import { AnimatePresence, motion } from 'framer-motion';
import toast from 'react-hot-toast';
import useAuth from '../../hooks/useAuth';
import useLanguage from '../../hooks/useLanguage';
import { useMyWaitingList } from '../../hooks/useWaitingList';
import CurrencySelector from '../CurrencySelector';
import ThemeToggle from '../ui/ThemeToggle';
import LanguageToggle from '../ui/LanguageToggle';
import SearchDropdown from '../search/SearchDropdown';
import styles from './Navbar.module.css';

const popularCities = ['القدس', 'بيت لحم', 'أريحا', 'رام الله', 'نابلس', 'الخليل'];

const Navbar = () => {
  const { isAuthenticated, isAdmin, isManager, user, logout } = useAuth();
  const { t } = useLanguage();
  const waitingListQuery = useMyWaitingList(0);
  const [scrolled, setScrolled] = useState(false);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [searchOpen, setSearchOpen] = useState(false);
  const [query, setQuery] = useState('');
  const [activeIndex, setActiveIndex] = useState(-1);
  const [recent, setRecent] = useState(() => {
    try { return JSON.parse(localStorage.getItem('terrastay_recent_searches') || '[]'); }
    catch { return []; }
  });
  const dropdownRef = useRef(null);
  const searchRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 80);
    window.addEventListener('scroll', handleScroll);
    handleScroll();
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) setDropdownOpen(false);
      if (searchRef.current && !searchRef.current.contains(event.target)) setSearchOpen(false);
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const displayName = user?.fullName || user?.name || 'User';
  const initials = displayName.split(' ').filter(Boolean).slice(0, 2).map((part) => part[0]?.toUpperCase()).join('') || 'U';
  const hasWaitingNotification = (waitingListQuery.data?.content || []).some((entry) => entry.status === 'NOTIFIED');
  const suggestions = [...recent, ...popularCities.filter((city) => city.includes(query) && !recent.includes(city))];

  const submitSearch = (value = query) => {
    const city = value.trim();
    const nextRecent = city ? [city, ...recent.filter((item) => item !== city)].slice(0, 5) : recent;
    setRecent(nextRecent);
    localStorage.setItem('terrastay_recent_searches', JSON.stringify(nextRecent));
    setSearchOpen(false);
    navigate(city ? `/search?city=${encodeURIComponent(city)}` : '/search');
  };

  const handleLogout = () => {
    logout();
    setDropdownOpen(false);
    setDrawerOpen(false);
    navigate('/');
    toast.success('تم تسجيل الخروج');
  };

  const handleSearchKeyDown = (event) => {
    if (event.key === 'Escape') setSearchOpen(false);
    if (event.key === 'ArrowDown') {
      event.preventDefault();
      setSearchOpen(true);
      setActiveIndex((index) => Math.min(suggestions.length - 1, index + 1));
    }
    if (event.key === 'ArrowUp') {
      event.preventDefault();
      setActiveIndex((index) => Math.max(0, index - 1));
    }
    if (event.key === 'Enter') {
      event.preventDefault();
      submitSearch(activeIndex >= 0 ? suggestions[activeIndex] : query);
    }
  };

  const menuLinks = [
    { to: '/my-bookings', label: t('myBookings'), icon: CalendarCheck, show: !isManager },
    { to: '/favorites', label: t('favorites'), icon: Heart, show: true },
    { to: '/settings', label: t('settings'), icon: Settings, show: true },
    { to: '/dashboard', label: t('adminPanel'), icon: LayoutDashboard, show: isAdmin || isManager },
    { to: '/my-waiting-list', label: 'قائمة الانتظار', icon: Bell, show: isAuthenticated && !isManager },
  ].filter((item) => item.show);

  return (
    <>
      <nav className={`${styles.navbar} ${scrolled ? styles.scrolled : ''}`}>
        <div className={styles.container}>
          <Link to="/" className={styles.logo} aria-label="TerraStay home">
            <span className={styles.logoMark}>T</span>
            <span className={styles.logoText}>TerraStay</span>
          </Link>

          <div className={styles.centerSearch} ref={searchRef}>
            <Search size={16} />
            <input
              value={query}
              onChange={(event) => {
                setQuery(event.target.value);
                setActiveIndex(-1);
                setSearchOpen(true);
              }}
              onFocus={() => setSearchOpen(true)}
              onKeyDown={handleSearchKeyDown}
              placeholder={t('searchHotels')}
            />
            <SearchDropdown
              open={searchOpen}
              recent={recent.filter((item) => !query || item.includes(query))}
              popular={popularCities.filter((city) => city.includes(query) && !recent.includes(city))}
              activeIndex={activeIndex}
              onSelect={(value) => {
                setQuery(value);
                submitSearch(value);
              }}
              onClearRecent={() => {
                setRecent([]);
                localStorage.removeItem('terrastay_recent_searches');
              }}
            />
          </div>

          <div className={styles.actions}>
            <CurrencySelector />
            <LanguageToggle />
            <ThemeToggle />
            <Link to="/my-waiting-list" className={styles.bell} aria-label="Notifications">
              <Bell size={18} />
              {hasWaitingNotification && <span />}
            </Link>

            {!isAuthenticated ? (
              <>
                <Link to="/login" className={styles.loginBtn}>{t('login')}</Link>
                <Link to="/register" className={styles.registerBtn}>{t('register')}</Link>
              </>
            ) : (
              <div className={styles.userMenu} ref={dropdownRef}>
                <button className={styles.avatar} onClick={() => setDropdownOpen((value) => !value)} aria-expanded={dropdownOpen}>
                  <span className={styles.avatarInitial}>{initials}</span>
                  <span className={styles.avatarName}>{displayName.split(' ')[0]}</span>
                  <ChevronDown size={14} />
                </button>
                <AnimatePresence>
                  {dropdownOpen && (
                    <motion.div className={styles.dropdown} initial={{ opacity: 0, y: -8 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -8 }}>
                      <div className={styles.dropdownHeader}>
                        <strong>{displayName}</strong>
                        <span>{user?.email}</span>
                      </div>
                      {menuLinks.map(({ to, label, icon: Icon }) => (
                        <Link key={to} to={to} className={styles.dropdownItem} onClick={() => setDropdownOpen(false)}>
                          <Icon size={16} /> {label}
                        </Link>
                      ))}
                      <button className={`${styles.dropdownItem} ${styles.logoutItem}`} onClick={handleLogout}>
                        <LogOut size={16} /> {t('logout')}
                      </button>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>
            )}

            <button className={styles.hamburger} onClick={() => setDrawerOpen(true)} aria-label="Open menu">
              <Menu size={22} />
            </button>
          </div>
        </div>
      </nav>

      <AnimatePresence>
        {drawerOpen && (
          <>
            <motion.div className={styles.drawerOverlay} initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} onClick={() => setDrawerOpen(false)} />
            <motion.div className={styles.drawer} initial={{ x: '100%' }} animate={{ x: 0 }} exit={{ x: '100%' }} transition={{ type: 'tween', duration: 0.25 }}>
              <div className={styles.drawerHeader}>
                <span className={styles.logoText}>TerraStay</span>
                <button onClick={() => setDrawerOpen(false)} aria-label="Close menu"><X size={22} /></button>
              </div>
              <div className={styles.drawerLinks}>
                <CurrencySelector mobile />
                <LanguageToggle />
                <div className={styles.drawerTheme}><ThemeToggle /><span>Theme</span></div>
                <NavLink to="/search" className={styles.drawerLink} onClick={() => setDrawerOpen(false)}>{t('searchHotels')}</NavLink>
                {isAuthenticated && menuLinks.map(({ to, label }) => (
                  <NavLink key={to} to={to} className={styles.drawerLink} onClick={() => setDrawerOpen(false)}>{label}</NavLink>
                ))}
              </div>
              <div className={styles.drawerActions}>
                {!isAuthenticated ? (
                  <>
                    <Link to="/login" className={styles.drawerLoginBtn} onClick={() => setDrawerOpen(false)}>{t('login')}</Link>
                    <Link to="/register" className={styles.drawerRegisterBtn} onClick={() => setDrawerOpen(false)}>{t('register')}</Link>
                  </>
                ) : (
                  <button className={styles.drawerLogout} onClick={handleLogout}><LogOut size={16} /> {t('logout')}</button>
                )}
              </div>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </>
  );
};

export default Navbar;
