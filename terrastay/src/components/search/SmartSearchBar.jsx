import { useEffect, useRef } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { Building2, CalendarDays, Clock3, MapPin, Search } from 'lucide-react';
import useSmartSearch from '../../hooks/useSmartSearch';
import useLanguage from '../../hooks/useLanguage';
import { useLocalizedField } from '../../hooks/useLocalizedField';
import styles from './SmartSearchBar.module.css';

const cities = [
  { name: 'القدس', nameEn: 'Jerusalem' },
  { name: 'بيت لحم', nameEn: 'Bethlehem' },
  { name: 'رام الله', nameEn: 'Ramallah' },
  { name: 'نابلس', nameEn: 'Nablus' },
  { name: 'أريحا', nameEn: 'Jericho' },
  { name: 'الخليل', nameEn: 'Hebron' },
  { name: 'جنين', nameEn: 'Jenin' },
  { name: 'طولكرم', nameEn: 'Tulkarm' },
  { name: 'قلقيلية', nameEn: 'Qalqilya' },
  { name: 'طوباس', nameEn: 'Tubas' },
];

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
  const { language, t } = useLanguage();
  const lf = useLocalizedField();
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
          placeholder={t('searchPlaceholder')}
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
                <div className={styles.empty}><Search size={18} /> {t('recentSearches')}</div>
              ) : (
                <>
                  {search.filteredRecent.length > 0 && <div className={styles.sectionLabel}>{t('recentSearches')}</div>}
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
                  {search.liveHotels.length > 0 && <div className={styles.sectionLabel}>{t('suggestedResults')}</div>}
                  {search.liveHotels.map((hotel, hotelIndex) => {
                    const index = search.filteredRecent.length + hotelIndex;
                    return (
                      <button
                        type="button"
                        key={hotel.id}
                        className={`${styles.suggestion} ${search.activeIndex === index ? styles.active : ''}`}
                        onMouseDown={() => search.submit({ label: lf(hotel, 'name'), city: lf(hotel, 'city'), hotel })}
                      >
                        <Building2 size={16} />
                        <span><Highlight text={lf(hotel, 'name')} query={search.query} /></span>
                        <small>{lf(hotel, 'city')}</small>
                      </button>
                    );
                  })}
                  {search.recent.length > 0 && (
                    <button type="button" className={styles.clear} onMouseDown={search.clearHistory}>{t('clearHistory')}</button>
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
          <option value="">{t('allCities')}</option>
          {cities.map((city) => <option key={city.name} value={city.name}>{language === 'en' ? city.nameEn : city.name}</option>)}
        </select>
      </label>

      <label className={styles.dateField}>
        <CalendarDays size={17} />
        <input type="date" lang={language === 'en' ? 'en' : 'ar'} placeholder={language === 'en' ? 'mm/dd/yyyy' : 'يوم/شهر/سنة'} value={search.checkIn} onChange={(event) => search.setCheckIn(event.target.value)} />
        <input type="date" lang={language === 'en' ? 'en' : 'ar'} placeholder={language === 'en' ? 'mm/dd/yyyy' : 'يوم/شهر/سنة'} value={search.checkOut} onChange={(event) => search.setCheckOut(event.target.value)} />
      </label>

      <button className={styles.submit} type="submit">{t('searchBtn')}</button>
    </form>
  );
};

export default SmartSearchBar;
