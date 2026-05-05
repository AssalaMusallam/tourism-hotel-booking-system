import { motion } from 'framer-motion';
import { MapPin, Heart, Globe } from 'lucide-react';
import styles from './AboutPage.module.css';

const AboutPage = () => {
  return (
    <div>
      <section className={styles.hero}>
        <div className="container">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className={styles.heroContent}
          >
            <h1>About PinkFlow</h1>
            <p>Connecting the world to the heart and soul of Palestine since 2020</p>
          </motion.div>
        </div>
      </section>

      <section className={`${styles.section} container`}>
        <div className={styles.missionGrid}>
          <div>
            <h2>Our Mission</h2>
            <p>
              PinkFlow was born from a simple belief: that Palestine's rich history, vibrant culture,
              and warm hospitality deserve to be experienced by travelers from around the world.
              We partner with authentic local hotels to bring you meaningful stays in Jerusalem,
              Bethlehem, Nazareth, Hebron, and beyond.
            </p>
            <p style={{ marginTop: 16 }}>
              رسالتنا هي ربط المسافرين بأصالة فلسطين وكرم ضيافتها الرائعة.
            </p>
          </div>
          <div className={styles.missionVisual}>
            <div className={styles.missionCard}>
              <MapPin size={32} className={styles.missionIcon} />
              <h3>10+ Cities</h3>
              <p>Across Palestine</p>
            </div>
            <div className={styles.missionCard}>
              <Heart size={32} className={styles.missionIcon} />
              <h3>50+ Hotels</h3>
              <p>Verified properties</p>
            </div>
            <div className={styles.missionCard}>
              <Globe size={32} className={styles.missionIcon} />
              <h3>5,000+</h3>
              <p>Happy travelers</p>
            </div>
          </div>
        </div>
      </section>

      <section className={styles.paletteBanner}>
        <div className="container">
          <h2 style={{ color: 'var(--color-text-on-dark)', textAlign: 'center' }}>Our Roots</h2>
          <p style={{ color: 'rgba(245,240,230,0.8)', textAlign: 'center', marginTop: 12, maxWidth: 600, margin: '12px auto 0' }}>
            Our colors are inspired by the iconic tatreez (Palestinian embroidery): terracotta reds,
            warm beiges, and soft ivory — the palette of the Holy Land itself.
          </p>
        </div>
      </section>
    </div>
  );
};

export default AboutPage;
