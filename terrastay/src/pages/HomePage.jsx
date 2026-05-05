import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Shield, Award, Tag, Headphones, MapPin } from 'lucide-react';
import { useHotels, useCities } from '../hooks/useCatalogQueries';
import HeroSearchBar from '../components/search/HeroSearchBar';
import HotelCard, { HotelCardSkeleton } from '../components/hotel/HotelCard';
import styles from './HomePage.module.css';

const fadeUp = {
  hidden: { opacity: 0, y: 24 },
  visible: (i = 0) => ({ opacity: 1, y: 0, transition: { delay: i * 0.1, duration: 0.5 } }),
};

const HomePage = () => {
  const navigate = useNavigate();
  useEffect(() => { document.title = 'PinkFlow – Find Your Perfect Stay'; }, []);
  const { data: citiesList } = useCities();
  const cities = citiesList || [];

  const { data: featuredData, isLoading } = useHotels({ size: 6 });
  const featured = featuredData?.content || [];

  const features = [
    { icon: Shield, title: 'Local Expertise', desc: 'Deep knowledge of Palestinian culture, holy sites, and the best local experiences.' },
    { icon: Award, title: 'Verified Hotels', desc: 'Every property is personally verified by our team for quality and authenticity.' },
    { icon: Tag, title: 'Best Prices', desc: 'We guarantee the best rates. Find a lower price elsewhere and we\'ll match it.' },
    { icon: Headphones, title: '24/7 Support', desc: 'Round-the-clock customer support in Arabic and English for all your needs.' },
  ];

  const cityColors = [
    '#c17f59', '#8B4A2E', '#A65A3A', '#6B4C3B',
    '#C4784F', '#9E7E6B', '#D8C3A5', '#7A1E1E',
  ];

  const handleSearch = ({ city }) => {
    const params = new URLSearchParams();
    if (city) params.set('city', city);
    navigate(`/search?${params.toString()}`);
  };

  return (
    <div>
      {/* HERO */}
      <section className={styles.hero}>
        <div className={styles.heroBg} />
        <div className={styles.heroContent}>
          <motion.div className={styles.heroText} initial="hidden" animate="visible">
            <motion.span className={styles.heroEyebrow} variants={fadeUp} custom={0}>
              Holy Land Tourism
            </motion.span>
            <motion.h1 className={styles.heroHeadline} variants={fadeUp} custom={1}>
              Discover the Heart<br />of Palestine
            </motion.h1>
            <motion.p className={styles.heroSub} variants={fadeUp} custom={2}>
              Stay in historic hotels across Jerusalem, Bethlehem, Nazareth, and beyond.
              Experience authentic Palestinian hospitality.
            </motion.p>
          </motion.div>
          <motion.div
            className={styles.searchWrap}
            initial={{ opacity: 0, y: 32 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.4, duration: 0.5 }}
          >
            <HeroSearchBar onSearch={handleSearch} />
          </motion.div>
        </div>
      </section>

      {/* CITIES — from real API */}
      {cities.length > 0 && (
        <section className={`${styles.section} container`}>
          <div className={styles.sectionTitle}>
            <h2>Explore by City</h2>
            <p>Discover holy sites, ancient markets, and breathtaking landscapes</p>
          </div>
          <div className={styles.citiesGrid}>
            {cities.slice(0, 8).map((city, i) => (
              <motion.button
                key={city}
                className={styles.cityCard}
                onClick={() => navigate(`/search?city=${encodeURIComponent(city)}`)}
                initial={{ opacity: 0, y: 16 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ delay: i * 0.06, duration: 0.35 }}
                whileHover={{ scale: 1.03 }}
              >
                <div className={styles.cityInitial} style={{ background: cityColors[i % cityColors.length] }}>
                  {city[0]}
                </div>
                <div className={styles.cityInfo}>
                  <span className={styles.cityName}>{city}</span>
                  <MapPin size={12} />
                </div>
              </motion.button>
            ))}
          </div>
        </section>
      )}

      {/* FEATURED HOTELS */}
      <section className={`${styles.section} ${styles.featuredSection}`}>
        <div className="container">
          <div className={styles.sectionTitle}>
            <h2>Featured Hotels</h2>
            <p>Handpicked properties offering exceptional stays in the Holy Land</p>
          </div>
          <div className={styles.hotelsGrid}>
            {isLoading
              ? Array.from({ length: 6 }).map((_, i) => <HotelCardSkeleton key={i} />)
              : featured.map((hotel) => (
                  <HotelCard
                    key={hotel.id}
                    hotel={hotel}
                    onClick={() => navigate(`/hotels/${hotel.id}`)}
                  />
                ))
            }
          </div>
          <div className={styles.viewAllWrap}>
            <button className={styles.viewAllBtn} onClick={() => navigate('/search')}>
              View All Hotels
            </button>
          </div>
        </div>
      </section>

      {/* WHY CHOOSE US */}
      <section className={`${styles.section} container`}>
        <div className={styles.sectionTitle}>
          <h2>Why Choose PinkFlow</h2>
          <p>The trusted platform for pilgrims and travelers visiting the Holy Land</p>
        </div>
        <div className={styles.featuresGrid}>
          {features.map((f, i) => (
            <motion.div
              key={f.title}
              className={styles.featureCard}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: i * 0.1, duration: 0.4 }}
            >
              <div className={styles.featureIcon}>
                <f.icon size={24} />
              </div>
              <h3 className={styles.featureTitle}>{f.title}</h3>
              <p className={styles.featureDesc}>{f.desc}</p>
            </motion.div>
          ))}
        </div>
      </section>

      {/* CTA BANNER */}
      <section className={styles.ctaBanner}>
        <div className={styles.ctaBg} />
        <div className={`container ${styles.ctaContent}`}>
          <h2>Ready to Experience Palestine?</h2>
          <p>Join thousands of travelers who have discovered the magic of the Holy Land with PinkFlow.</p>
          <button className={styles.ctaBtn} onClick={() => navigate('/search')}>
            Find Your Perfect Hotel
          </button>
        </div>
      </section>
    </div>
  );
};

export default HomePage;
