import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Globe2, MapPin, MessageCircle, Send } from 'lucide-react';
import { useHotels } from '../hooks/useCatalogQueries';
import HeroSearchBar from '../components/search/HeroSearchBar';
import HotelCard from '../components/hotel/HotelCard';
import SkeletonCard from '../components/ui/SkeletonCard';
import useLanguage from '../hooks/useLanguage';
import styles from './HomePage.module.css';

const cityFallbacks = [
  { name: 'القدس', nameEn: 'Jerusalem' },
  { name: 'بيت لحم', nameEn: 'Bethlehem' },
  { name: 'أريحا', nameEn: 'Jericho' },
  { name: 'رام الله', nameEn: 'Ramallah' },
  { name: 'نابلس', nameEn: 'Nablus' },
  { name: 'الخليل', nameEn: 'Hebron' },
];

const cityNavigationMap = {
  Jerusalem: 'Jerusalem',
  القدس: 'Jerusalem',
  Hebron: 'Hebron',
  الخليل: 'Hebron',
  Bethlehem: 'Bethlehem',
  'بيت لحم': 'Bethlehem',
  Jericho: 'Jericho',
  أريحا: 'Jericho',
  Nablus: 'Nablus',
  نابلس: 'Nablus',
  Tubas: 'Tubas',
  طوباس: 'Tubas',
  Ramallah: 'Ramallah',
  'رام الله': 'Ramallah',
  ...Object.fromEntries(cityFallbacks.map((city) => [city.name, city.nameEn])),
};

const HomePage = () => {
  const navigate = useNavigate();
  const { language, t } = useLanguage();
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

  const handleCityClick = (cityName) => {
    const englishCity = cityNavigationMap[cityName] || cityName;
    navigate(`/search?city=${encodeURIComponent(englishCity)}`);
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
            {cityFallbacks.map((quickCity) => (
              <button type="button" key={quickCity.name} onClick={() => handleCityClick(language === 'en' ? quickCity.nameEn : quickCity.name)}>
                {language === 'en' ? quickCity.nameEn : quickCity.name}
              </button>
            ))}
          </div>
        </div>
      </section>
      <div className="tatreezDivider" />

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
