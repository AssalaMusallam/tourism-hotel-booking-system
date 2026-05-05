import { useState } from 'react';
import { ChevronDown, ChevronUp, SlidersHorizontal } from 'lucide-react';
import { AMENITIES } from '../../constants/amenities';
import Button from '../ui/Button';
import styles from './SearchFilters.module.css';

const SearchFilters = ({ filters, onChange }) => {
  const [expanded, setExpanded] = useState(true);

  const updateFilter = (key, value) => {
    onChange({ ...filters, [key]: value });
  };

  const toggleStar = (star) => {
    const current = filters.stars || [];
    const next = current.includes(star)
      ? current.filter((s) => s !== star)
      : [...current, star];
    updateFilter('stars', next);
  };

  const toggleAmenity = (amenity) => {
    const current = filters.amenities || [];
    const next = current.includes(amenity)
      ? current.filter((a) => a !== amenity)
      : [...current, amenity];
    updateFilter('amenities', next);
  };

  const resetFilters = () => {
    onChange({ minPrice: 50, maxPrice: 500, stars: [], amenities: [] });
  };

  return (
    <aside className={styles.sidebar}>
      <div className={styles.header}>
        <div className={styles.headerTitle}>
          <SlidersHorizontal size={16} />
          <span>Filters</span>
        </div>
        <button
          className={styles.toggleBtn}
          onClick={() => setExpanded((v) => !v)}
          aria-label="Toggle filters"
        >
          {expanded ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
        </button>
      </div>

      {expanded && (
        <div className={styles.body}>
          {/* Price Range */}
          <div className={styles.section}>
            <h4 className={styles.sectionTitle}>Price per Night</h4>
            <div className={styles.priceLabels}>
              <span>${filters.minPrice || 50}</span>
              <span>${filters.maxPrice || 500}</span>
            </div>
            <input
              type="range"
              min={50}
              max={500}
              step={10}
              value={filters.maxPrice || 500}
              onChange={(e) => updateFilter('maxPrice', Number(e.target.value))}
              className={styles.range}
              aria-label="Max price"
            />
          </div>

          {/* Star Rating */}
          <div className={styles.section}>
            <h4 className={styles.sectionTitle}>Star Rating</h4>
            <div className={styles.stars}>
              {[5, 4, 3, 2].map((star) => (
                <label key={star} className={styles.checkLabel}>
                  <input
                    type="checkbox"
                    checked={(filters.stars || []).includes(star)}
                    onChange={() => toggleStar(star)}
                    className={styles.checkbox}
                  />
                  <span>{'★'.repeat(star)}</span>
                </label>
              ))}
            </div>
          </div>

          {/* Amenities */}
          <div className={styles.section}>
            <h4 className={styles.sectionTitle}>Amenities</h4>
            <div className={styles.amenities}>
              {AMENITIES.map((a) => (
                <label key={a.value} className={styles.checkLabel}>
                  <input
                    type="checkbox"
                    checked={(filters.amenities || []).includes(a.value)}
                    onChange={() => toggleAmenity(a.value)}
                    className={styles.checkbox}
                  />
                  <span>{a.label}</span>
                </label>
              ))}
            </div>
          </div>

          <Button variant="ghost" onClick={resetFilters} fullWidth size="sm">
            Clear All Filters
          </Button>
        </div>
      )}
    </aside>
  );
};

export default SearchFilters;
