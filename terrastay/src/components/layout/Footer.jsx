import { Link } from 'react-router-dom';
import { Share2, Camera, AtSign, MessageCircle } from 'lucide-react';
import styles from './Footer.module.css';

const Footer = () => {
  return (
    <footer className={styles.footer}>
      <div className={styles.container}>
        <div className={styles.grid}>
          <div className={styles.col}>
            <Link to="/" className={styles.logo}>TerraStay</Link>
            <p className={styles.tagline}>
              Connecting Pilgrims & Travelers to the Heart of Palestine
            </p>
            <div className={styles.socials}>
              <a href="#" aria-label="Facebook" className={styles.social}><Share2 size={18} /></a>
              <a href="#" aria-label="Instagram" className={styles.social}><Camera size={18} /></a>
              <a href="#" aria-label="Twitter" className={styles.social}><AtSign size={18} /></a>
              <a href="#" aria-label="WhatsApp" className={styles.social}><MessageCircle size={18} /></a>
            </div>
          </div>

          <div className={styles.col}>
            <h4 className={styles.colTitle}>Quick Links</h4>
            <nav className={styles.navList}>
              <Link to="/">Home</Link>
              <Link to="/search">Search Hotels</Link>
              <Link to="/about">About Us</Link>
              <Link to="/about">Contact</Link>
            </nav>
          </div>

          <div className={styles.col}>
            <h4 className={styles.colTitle}>For Hotels</h4>
            <nav className={styles.navList}>
              <Link to="/register">Register Your Property</Link>
              <Link to="/login">Admin Login</Link>
              <Link to="/about">Partnership Program</Link>
              <Link to="/about">Help & Support</Link>
            </nav>
          </div>
        </div>

        <div className={styles.bottom}>
          <span>© 2025 TerraStay. Made with ♥ in Bethlehem, Palestine.</span>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
