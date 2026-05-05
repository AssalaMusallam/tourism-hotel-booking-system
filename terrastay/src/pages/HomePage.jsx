import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { Shield, Award, Tag, Headphones } from 'lucide-react';
import { getHotels } from '../api/hotels';
import HeroSearchBar from '../components/search/HeroSearchBar';
import HotelCard, { HotelCardSkeleton } from '../components/search/HotelCard';
import { CITIES } from '../constants/cities';
import styles from './HomePage.module.css';

const fadeUp = {
  hidden: { opacity: 0, y: 24 },
  visible: (i = 0) => ({ opacity: 1, y: 0, transition: { delay: i * 0.1, duration: 0.5 } }),
};

const HomePage = () => {
  const navigate = useNavigate();

  const { data: featuredData, isLoading } = useQuery({
    queryKey: ['hotels', 'featured'],
    queryFn: () => getHotels({ featured: true }),
    staleTime: 5 * 60 * 1000,
  });

  const featured = featuredData?.data || [];

  const features = [
    { icon: Shield, title: 'Local Expertise', desc: 'Deep knowledge of Palestinian culture, holy sites, and the best local experiences.' },
    { icon: Award, title: 'Verified Hotels', desc: 'Every property is personally verified by our team for quality and authenticity.' },
    { icon: Tag, title: 'Best Prices', desc: 'We guarantee the best rates. Find a lower price elsewhere and we\'ll match it.' },
    { icon: Headphones, title: '24/7 Support', desc: 'Round-the-clock customer support in Arabic and English for all your needs.' },
  ];

  return (
    <div>
      {/* HERO */}
      <section className={styles.hero}>
        <div className={`${styles.heroBg} embroidery-pattern`} />
        <div className={styles.heroContent}>
          <motion.div
            className={styles.heroText}
            initial="hidden"
            animate="visible"
          >
            <motion.span className={styles.heroEyebrow} variants={fadeUp} custom={0}>
              Holy Land Tourism
            </motion.span>
            <motion.h1 className={styles.heroHeadline} variants={fadeUp} custom={1}>
              Discover the Heart
              <br />
              of Palestine
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
            <HeroSearchBar />
          </motion.div>
        </div>
      </section>

      {/* CITIES */}
      <section className={`${styles.section} container`}>
        <div className="section-title">
          <h2>Explore by City</h2>
          <p>Discover holy sites, ancient markets, and breathtaking landscapes across Palestine</p>
        </div>
        <div className={styles.citiesGrid}>
          {CITIES.map((city, i) => (
            <motion.button
              key={city.value}
              className={styles.cityCard}
              onClick={() => navigate(`/search?city=${city.value}`)}
              initial={{ opacity: 0, y: 16 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: i * 0.06, duration: 0.35 }}
              whileHover={{ scale: 1.03 }}
            >
              <div className={styles.cityInitial} style={{ background: city.color }}>
                {city.initial}
              </div>
              <div className={styles.cityInfo}>
                <span className={styles.cityName}>{city.label}</span>
                <span className={styles.cityAr}>{city.labelAr}</span>
              </div>
            </motion.button>
          ))}
        </div>
      </section>

      {/* FEATURED HOTELS */}
      <section className={`${styles.section} ${styles.featuredSection}`}>
        <div className="container">
          <div className="section-title">
            <h2>Featured Hotels</h2>
            <p>Handpicked properties offering exceptional stays in the Holy Land</p>
          </div>
          <div className={styles.hotelsGrid}>
            {isLoading
              ? Array.from({ length: 6 }).map((_, i) => <HotelCardSkeleton key={i} />)
              : featured.slice(0, 6).map((hotel, i) => (
                  <HotelCard key={hotel.id} hotel={hotel} index={i} />
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
        <div className="section-title">
          <h2>Why Choose TerraStay</h2>
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
        <div className={`${styles.ctaBg} embroidery-pattern`} />
        <div className={`container ${styles.ctaContent}`}>
          <h2>Ready to Experience Palestine?</h2>
          <p>Join thousands of travelers who have discovered the magic of the Holy Land with TerraStay.</p>
          <button className={styles.ctaBtn} onClick={() => navigate('/search')}>
            Find Your Perfect Hotel
          </button>
        </div>
      </section>
    </div>
  );
};

export default HomePage;
