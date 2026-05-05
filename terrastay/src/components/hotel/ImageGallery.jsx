import { useState } from 'react';
import { X, ChevronLeft, ChevronRight } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import styles from './ImageGallery.module.css';

const ImageGallery = ({ images = [], name = '' }) => {
  const [lightboxIndex, setLightboxIndex] = useState(null);
  const safeImages = images.length > 0 ? images : [''];

  const openLightbox = (i) => setLightboxIndex(i);
  const closeLightbox = () => setLightboxIndex(null);
  const prev = () => setLightboxIndex((i) => (i - 1 + safeImages.length) % safeImages.length);
  const next = () => setLightboxIndex((i) => (i + 1) % safeImages.length);

  return (
    <>
      <div className={styles.grid}>
        <div className={styles.mainImage} onClick={() => openLightbox(0)}>
          {safeImages[0] ? (
            <img src={safeImages[0]} alt={name} className={styles.img} />
          ) : (
            <div className={styles.placeholder}>{name[0]}</div>
          )}
          <div className={styles.overlay}>
            <span>View All Photos</span>
          </div>
        </div>
        <div className={styles.thumbs}>
          {[1, 2].map((i) => (
            <div key={i} className={styles.thumb} onClick={() => openLightbox(i)}>
              {safeImages[i] ? (
                <img src={safeImages[i]} alt={`${name} ${i + 1}`} className={styles.img} />
              ) : (
                <div className={styles.placeholder} style={{ fontSize: 24 }}>{name[0]}</div>
              )}
            </div>
          ))}
        </div>
      </div>

      <AnimatePresence>
        {lightboxIndex !== null && (
          <motion.div
            className={styles.lightbox}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
          >
            <button className={styles.closeBtn} onClick={closeLightbox} aria-label="Close">
              <X size={24} />
            </button>
            <button className={`${styles.navBtn} ${styles.prevBtn}`} onClick={prev} aria-label="Previous">
              <ChevronLeft size={28} />
            </button>
            <div className={styles.lightboxImage}>
              {safeImages[lightboxIndex] ? (
                <img src={safeImages[lightboxIndex]} alt={`${name} ${lightboxIndex + 1}`} />
              ) : (
                <div className={styles.lightboxPlaceholder}>{name[0]}</div>
              )}
            </div>
            <button className={`${styles.navBtn} ${styles.nextBtn}`} onClick={next} aria-label="Next">
              <ChevronRight size={28} />
            </button>
            <div className={styles.counter}>
              {lightboxIndex + 1} / {safeImages.length}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
};

export default ImageGallery;
