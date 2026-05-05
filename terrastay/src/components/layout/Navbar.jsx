import { useState, useEffect, useRef } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { Menu, X, ChevronDown, LogOut, LayoutDashboard, CalendarCheck } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import useAuth from '../../hooks/useAuth';
import toast from 'react-hot-toast';
import styles from './Navbar.module.css';

const Navbar = () => {
  const { isAuthenticated, isAdmin, isManager, user, logout } = useAuth();
  const [scrolled, setScrolled] = useState(false);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 10);
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = () => {
    logout();
    setDropdownOpen(false);
    setDrawerOpen(false);
    navigate('/');
    toast.success('Logged out successfully');
  };

  const navLinks = [
    { to: '/search', label: 'Search Hotels' },
  ];

  const displayName = user?.fullName || user?.name || 'User';

  return (
    <>
      <nav className={`${styles.navbar} ${scrolled ? styles.scrolled : ''}`}>
        <div className={styles.container}>
          <Link to="/" className={styles.logo}>
            <span className={styles.logoText}>TerraStay</span>
          </Link>

          <div className={styles.links}>
            {navLinks.map((l) => (
              <NavLink
                key={l.to}
                to={l.to}
                className={({ isActive }) => `${styles.link} ${isActive ? styles.active : ''}`}
              >
                {l.label}
              </NavLink>
            ))}
            {isAuthenticated && !isManager && (
              <NavLink
                to="/my-bookings"
                className={({ isActive }) => `${styles.link} ${isActive ? styles.active : ''}`}
              >
                My Bookings
              </NavLink>
            )}
            {isManager && (
              <NavLink
                to="/admin"
                className={({ isActive }) => `${styles.link} ${isActive ? styles.active : ''}`}
              >
                Admin Panel
              </NavLink>
            )}
          </div>

          <div className={styles.actions}>
            {!isAuthenticated ? (
              <>
                <Link to="/login" className={styles.loginBtn}>Login</Link>
                <Link to="/register" className={styles.registerBtn}>Register</Link>
              </>
            ) : (
              <div className={styles.userMenu} ref={dropdownRef}>
                <button
                  className={styles.avatar}
                  onClick={() => setDropdownOpen((v) => !v)}
                  aria-expanded={dropdownOpen}
                >
                  <span className={styles.avatarInitial}>
                    {displayName[0]?.toUpperCase() || 'U'}
                  </span>
                  <span className={styles.avatarName}>{displayName.split(' ')[0]}</span>
                  <ChevronDown size={14} />
                </button>
                <AnimatePresence>
                  {dropdownOpen && (
                    <motion.div
                      className={styles.dropdown}
                      initial={{ opacity: 0, y: -8 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0, y: -8 }}
                      transition={{ duration: 0.15 }}
                    >
                      <div className={styles.dropdownHeader}>
                        <strong>{displayName}</strong>
                        <span>{user?.email}</span>
                        <span className={styles.roleBadge}>{user?.role}</span>
                      </div>
                      <div className={styles.dropdownDivider} />
                      {isManager ? (
                        <Link to="/admin" className={styles.dropdownItem} onClick={() => setDropdownOpen(false)}>
                          <LayoutDashboard size={15} /> Dashboard
                        </Link>
                      ) : (
                        <Link to="/my-bookings" className={styles.dropdownItem} onClick={() => setDropdownOpen(false)}>
                          <CalendarCheck size={15} /> My Bookings
                        </Link>
                      )}
                      <button className={`${styles.dropdownItem} ${styles.logoutItem}`} onClick={handleLogout}>
                        <LogOut size={15} /> Logout
                      </button>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>
            )}

            <button
              className={styles.hamburger}
              onClick={() => setDrawerOpen(true)}
              aria-label="Open menu"
            >
              <Menu size={22} />
            </button>
          </div>
        </div>
      </nav>

      {/* Mobile drawer */}
      <AnimatePresence>
        {drawerOpen && (
          <>
            <motion.div
              className={styles.drawerOverlay}
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setDrawerOpen(false)}
            />
            <motion.div
              className={styles.drawer}
              initial={{ x: '100%' }}
              animate={{ x: 0 }}
              exit={{ x: '100%' }}
              transition={{ type: 'tween', duration: 0.25 }}
            >
              <div className={styles.drawerHeader}>
                <span className={styles.logoText}>TerraStay</span>
                <button onClick={() => setDrawerOpen(false)} aria-label="Close menu">
                  <X size={22} />
                </button>
              </div>
              <div className={styles.drawerLinks}>
                {navLinks.map((l) => (
                  <NavLink
                    key={l.to}
                    to={l.to}
                    className={({ isActive }) => `${styles.drawerLink} ${isActive ? styles.active : ''}`}
                    onClick={() => setDrawerOpen(false)}
                  >
                    {l.label}
                  </NavLink>
                ))}
                {isAuthenticated && !isManager && (
                  <NavLink to="/my-bookings" className={styles.drawerLink} onClick={() => setDrawerOpen(false)}>
                    My Bookings
                  </NavLink>
                )}
                {isManager && (
                  <NavLink to="/admin" className={styles.drawerLink} onClick={() => setDrawerOpen(false)}>
                    Admin Panel
                  </NavLink>
                )}
              </div>
              <div className={styles.drawerActions}>
                {!isAuthenticated ? (
                  <>
                    <Link to="/login" className={styles.drawerLoginBtn} onClick={() => setDrawerOpen(false)}>Login</Link>
                    <Link to="/register" className={styles.drawerRegisterBtn} onClick={() => setDrawerOpen(false)}>Register</Link>
                  </>
                ) : (
                  <button className={styles.drawerLogout} onClick={handleLogout}>
                    <LogOut size={16} /> Logout
                  </button>
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
