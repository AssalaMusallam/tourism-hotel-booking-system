import { useState } from 'react';
import { ChevronDown, ChevronUp, SlidersHorizontal } from 'lucide-react';
import StarRating from '../ui/StarRating';
import Button from '../ui/Button';
import styles from './SearchFilters.module.css';

const SearchFilters = ({ filters, onChange, onApply, onReset }) => {
  const [expanded, setExpanded] = useState(true);

  const updateFilter = (key, value) => {
    onChange({ ...filters, [key]: value });
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
          {/* Min Rating */}
          <div className={styles.section}>
            <h4 className={styles.sectionTitle}>Minimum Rating</h4>
            <StarRating
              value={filters.minRating || 0}
              interactive
              onChange={(v) => updateFilter('minRating', v)}
              size={20}
            />
            {filters.minRating > 0 && (
              <span className={styles.filterLabel}>{filters.minRating}+ stars</span>
            )}
          </div>

          {/* Amenity filter */}
          <div className={styles.section}>
            <h4 className={styles.sectionTitle}>Amenity</h4>
            <input
              type="text"
              value={filters.amenity || ''}
              onChange={(e) => updateFilter('amenity', e.target.value)}
              placeholder="e.g. Wi-Fi, Pool..."
              className={styles.filterInput}
            />
          </div>

          {/* Has Image */}
          <div className={styles.section}>
            <label className={styles.checkLabel}>
              <input
                type="checkbox"
                checked={filters.hasImage || false}
                onChange={(e) => updateFilter('hasImage', e.target.checked || undefined)}
                className={styles.checkbox}
              />
              <span>Has photos only</span>
            </label>
          </div>

          <div className={styles.buttonRow}>
            <Button variant="primary" size="sm" onClick={onApply} fullWidth>
              Apply Filters
            </Button>
            <Button variant="ghost" size="sm" onClick={onReset} fullWidth>
              Reset
            </Button>
          </div>
        </div>
      )}
    </aside>
  );
};

export default SearchFilters;
