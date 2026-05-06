import { Clock, Flame, MapPin, X } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import styles from './SearchDropdown.module.css';

const SearchDropdown = ({
  open,
  recent = [],
  popular = [],
  activeIndex = -1,
  onSelect,
  onClearRecent,
}) => {
  const items = [
    ...recent.map((value) => ({ value, type: 'recent' })),
    ...popular.map((value) => ({ value, type: 'popular' })),
  ];

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          className={styles.dropdown}
          initial={{ opacity: 0, y: -8, height: 0 }}
          animate={{ opacity: 1, y: 0, height: 'auto' }}
          exit={{ opacity: 0, y: -8, height: 0 }}
          transition={{ duration: 0.18 }}
        >
          {recent.length > 0 && (
            <div className={styles.header}>
              <span><Clock size={14} /> عمليات بحث حديثة</span>
              <button type="button" onClick={onClearRecent} aria-label="Clear recent searches">
                <X size={13} />
              </button>
            </div>
          )}
          {items.length === 0 ? (
            <button type="button" className={styles.item} onMouseDown={() => onSelect('القدس')}>
              <MapPin size={15} /> القدس
            </button>
          ) : (
            items.map((item, index) => (
              <button
                type="button"
                key={`${item.type}-${item.value}`}
                className={`${styles.item} ${index === activeIndex ? styles.active : ''}`}
                onMouseDown={() => onSelect(item.value)}
              >
                {item.type === 'recent' ? <Clock size={15} /> : <Flame size={15} />}
                {item.value}
              </button>
            ))
          )}
        </motion.div>
      )}
    </AnimatePresence>
  );
};

export default SearchDropdown;
