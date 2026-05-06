import { useEffect, useRef } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { Building2, CalendarDays, Clock3, MapPin, Search } from 'lucide-react';
import useSmartSearch from '../../hooks/useSmartSearch';
import styles from './SmartSearchBar.module.css';

const cities = ['القدس', 'بيت لحم', 'رام الله', 'نابلس', 'أريحا', 'الخليل', 'جنين', 'طولكرم', 'قلقيلية', 'طوباس'];

const Highlight = ({ text, query }) => {
  if (!query) return text;
  const index = text.toLowerCase().indexOf(query.toLowerCase());
  if (index < 0) return text;
  return (
    <>
      {text.slice(0, index)}
      <strong>{text.slice(index, index + query.length)}</strong>
      {text.slice(index + query.length)}
    </>
  );
};

const SmartSearchBar = ({ compact = false, onSearch, defaultValues = {} }) => {
  const search = useSmartSearch({
    defaultQuery: defaultValues.q || defaultValues.city || '',
    defaultCity: defaultValues.city || '',
    onSearch,
  });
  const ref = useRef(null);

  useEffect(() => {
    const onPointerDown = (event) => {
      if (ref.current && !ref.current.contains(event.target)) search.setOpen(false);
    };
    document.addEventListener('mousedown', onPointerDown);
    return () => document.removeEventListener('mousedown', onPointerDown);
  }, [search]);

  return (
    <form className={`${styles.form} ${compact ? styles.compact : ''}`} onSubmit={(event) => { event.preventDefault(); search.submit(); }} ref={ref}>
      <div className={styles.queryField}>
        <Search size={20} />
        <input
          value={search.query}
          onChange={(event) => {
            search.setQuery(event.target.value);
            search.setActiveIndex(-1);
            search.setOpen(true);
          }}
          onFocus={() => search.setOpen(true)}
          onKeyDown={search.onKeyDown}
          placeholder="ابحث عن فندق أو مدينة"
        />
        <AnimatePresence>
          {search.open && (
            <motion.div
              className={styles.dropdown}
              initial={{ opacity: 0, y: -8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -8 }}
              transition={{ duration: 0.18 }}
            >
              {search.recent.length === 0 && search.query.length === 0 ? (
                <div className={styles.empty}><Search size={18} /> لا توجد عمليات بحث سابقة</div>
              ) : (
                <>
                  {search.filteredRecent.length > 0 && <div className={styles.sectionLabel}>البحث الأخير</div>}
                  {search.filteredRecent.map((item, index) => (
                    <button
                      type="button"
                      key={`${item.query}-${item.at}`}
                      className={`${styles.suggestion} ${search.activeIndex === index ? styles.active : ''}`}
                      onMouseDown={() => search.submit({ label: item.query, city: item.city })}
                    >
                      <Clock3 size={16} />
                      <span><Highlight text={item.query} query={search.query} /></span>
                      {item.city && <small>{item.city}</small>}
                    </button>
                  ))}
                  {search.liveHotels.length > 0 && <div className={styles.sectionLabel}>نتائج مقترحة</div>}
                  {search.liveHotels.map((hotel, hotelIndex) => {
                    const index = search.filteredRecent.length + hotelIndex;
                    return (
                      <button
                        type="button"
                        key={hotel.id}
                        className={`${styles.suggestion} ${search.activeIndex === index ? styles.active : ''}`}
                        onMouseDown={() => search.submit({ label: hotel.name, city: hotel.city, hotel })}
                      >
                        <Building2 size={16} />
                        <span><Highlight text={hotel.name} query={search.query} /></span>
                        <small>{hotel.city}</small>
                      </button>
                    );
                  })}
                  {search.recent.length > 0 && (
                    <button type="button" className={styles.clear} onMouseDown={search.clearHistory}>مسح السجل</button>
                  )}
                </>
              )}
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      <label className={styles.selectField}>
        <MapPin size={17} />
        <select value={search.city} onChange={(event) => search.setCity(event.target.value)}>
          <option value="">كل المدن</option>
          {cities.map((city) => <option key={city} value={city}>{city}</option>)}
        </select>
      </label>

      <label className={styles.dateField}>
        <CalendarDays size={17} />
        <input type="date" value={search.checkIn} onChange={(event) => search.setCheckIn(event.target.value)} />
        <input type="date" value={search.checkOut} onChange={(event) => search.setCheckOut(event.target.value)} />
      </label>

      <button className={styles.submit} type="submit">ابحث الآن</button>
    </form>
  );
};

export default SmartSearchBar;
