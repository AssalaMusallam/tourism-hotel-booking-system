import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Globe2, MapPin, MessageCircle, Send } from 'lucide-react';
import { useCities, useHotels } from '../hooks/useCatalogQueries';
import HeroSearchBar from '../components/search/HeroSearchBar';
import HotelCard from '../components/hotel/HotelCard';
import SkeletonCard from '../components/ui/SkeletonCard';
import useLanguage from '../hooks/useLanguage';
import styles from './HomePage.module.css';

const destinationImages = [
  'https://images.unsplash.com/photo-1542743408-218cc173cda0?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1601581875309-fafbf2d3ed3a?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1570641963303-92ce4845ed4c?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1539650116574-75c0c6d73f6e?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1523906834658-6e24ef2386f9?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1548013146-72479768bada?auto=format&fit=crop&w=300&q=80',
];

const HomePage = () => {
  const navigate = useNavigate();
  const { t } = useLanguage();
  const { data: citiesList } = useCities();
  const cities = (citiesList?.length ? citiesList : ['القدس', 'بيت لحم', 'أريحا', 'رام الله', 'نابلس', 'الخليل']).slice(0, 6);
  const { data: featuredData, isLoading } = useHotels({ size: 3, hasImage: true });
  const featured = featuredData?.content || [];

  useEffect(() => {
    document.title = 'TerraStay - Palestine Hotel Booking';
  }, []);

  const handleSearch = ({ city, checkIn, checkOut, guests }) => {
    const params = new URLSearchParams();
    if (city) params.set('city', city);
    if (checkIn) params.set('checkIn', checkIn);
    if (checkOut) params.set('checkOut', checkOut);
    if (guests) params.set('guests', guests);
    navigate(`/search?${params.toString()}`);
  };

  return (
    <div className={styles.page}>
      <section className={styles.hero}>
        <div className={styles.heroOverlay} />
        <div className={styles.heroContent}>
          <motion.div initial={{ opacity: 0, y: 24 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5 }} className={styles.heroText}>
            <span className={styles.eyebrow}>TerraStay Palestine</span>
            <h1>{t('homeHeadline')}</h1>
            <p>{t('homeSubheadline')}</p>
          </motion.div>
          <motion.div className={styles.searchWrap} initial={{ opacity: 0, y: 28 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2, duration: 0.5 }}>
            <HeroSearchBar onSearch={handleSearch} />
          </motion.div>
          <div className={styles.quickPills}>
            {['القدس', 'بيت لحم', 'رام الله', 'نابلس', 'أريحا', 'الخليل'].map((quickCity) => (
              <button type="button" key={quickCity} onClick={() => navigate(`/search?city=${encodeURIComponent(quickCity)}`)}>
                {quickCity}
              </button>
            ))}
          </div>
        </div>
      </section>
      <div className="tatreezDivider" />

      <section className={`container ${styles.section}`}>
        <div className={styles.sectionHeader}>
          <h2>{t('popularDestinations')}</h2>
        </div>
        <div className={styles.destinations}>
          {cities.map((city, index) => (
            <motion.button
              type="button"
              key={city}
              className={styles.destinationCard}
              onClick={() => navigate(`/search?city=${encodeURIComponent(city)}`)}
              whileHover={{ y: -4, scale: 1.03 }}
            >
              <img src={destinationImages[index % destinationImages.length]} alt={city} />
              <span>{city}</span>
            </motion.button>
          ))}
        </div>
      </section>

      <section className={styles.featuredBand}>
        <div className={`container ${styles.section}`}>
          <div className={styles.sectionHeader}>
            <h2>{t('featuredHotels')}</h2>
            <button type="button" onClick={() => navigate('/search')}>{t('viewAllHotels')}</button>
          </div>
          <div className={styles.hotelsGrid}>
            {isLoading
              ? Array.from({ length: 3 }).map((_, index) => <SkeletonCard key={index} />)
              : featured.slice(0, 3).map((hotel, index) => <HotelCard key={hotel.id} hotel={hotel} index={index} />)}
          </div>
        </div>
      </section>
      <div className="tatreezDivider" />

      <footer className={styles.footer}>
        <div className="container">
          <div className={styles.footerGrid}>
            <div>
              <h2>TerraStay</h2>
              <p>إقامات فلسطينية مختارة بعناية للمسافرين والضيوف.</p>
            </div>
            <div className={styles.footerLinks}>
              <a href="/search">الفنادق</a>
              <a href="/favorites">المفضلة</a>
              <a href="/settings">الإعدادات</a>
            </div>
            <div className={styles.social}>
              <MapPin size={18} />
              <Globe2 size={18} />
              <MessageCircle size={18} />
              <Send size={18} />
            </div>
          </div>
          <small>© 2026 TerraStay. All rights reserved.</small>
        </div>
      </footer>
    </div>
  );
};

export default HomePage;
