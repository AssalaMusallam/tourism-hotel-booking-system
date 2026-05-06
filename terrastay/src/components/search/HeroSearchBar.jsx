import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Calendar, MapPin, Search, Users } from 'lucide-react';
import { addDays, format } from 'date-fns';
import { useCities } from '../../hooks/useCatalogQueries';
import useLanguage from '../../hooks/useLanguage';
import SearchDropdown from './SearchDropdown';
import styles from './HeroSearchBar.module.css';

const toInputDate = (d) => format(d, 'yyyy-MM-dd');
const RECENT_KEY = 'terrastay_recent_searches';

const HeroSearchBar = ({ compact = false, onSearch, defaultValues = {} }) => {
  const navigate = useNavigate();
  const { t, language } = useLanguage();
  const { data: citiesList } = useCities();
  const fallbackCities = ['القدس', 'بيت لحم', 'أريحا', 'رام الله', 'نابلس', 'الخليل'];
  const cities = citiesList?.length ? citiesList : fallbackCities;

  const [city, setCity] = useState(defaultValues.city || '');
  const [checkIn, setCheckIn] = useState(defaultValues.checkIn || toInputDate(addDays(new Date(), 1)));
  const [checkOut, setCheckOut] = useState(defaultValues.checkOut || toInputDate(addDays(new Date(), 4)));
  const [guests, setGuests] = useState(defaultValues.guests || 2);
  const [suggestions, setSuggestions] = useState(cities.slice(0, 6));
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [activeIndex, setActiveIndex] = useState(-1);
  const [recent, setRecent] = useState(() => {
    try { return JSON.parse(localStorage.getItem(RECENT_KEY) || '[]'); }
    catch { return []; }
  });
  const suggestRef = useRef(null);

  useEffect(() => {
    const handler = (event) => {
      if (suggestRef.current && !suggestRef.current.contains(event.target)) {
        setShowSuggestions(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  useEffect(() => {
    if (!city) setSuggestions(cities.slice(0, 6));
  }, [citiesList]);

  const persistRecent = (value) => {
    if (!value) return;
    const next = [value, ...recent.filter((item) => item !== value)].slice(0, 5);
    setRecent(next);
    localStorage.setItem(RECENT_KEY, JSON.stringify(next));
  };

  const handleCityInput = (value) => {
    setCity(value);
    setActiveIndex(-1);
    const normalized = value.trim().toLowerCase();
    setSuggestions(
      normalized
        ? cities.filter((item) => item.toLowerCase().includes(normalized)).slice(0, 8)
        : cities.slice(0, 6)
    );
    setShowSuggestions(true);
  };

  const handleSearch = (event) => {
    event.preventDefault();
    if (checkOut && checkIn && checkOut <= checkIn) return;

    const params = new URLSearchParams();
    if (city) params.set('city', city);
    if (checkIn) params.set('checkIn', checkIn);
    if (checkOut) params.set('checkOut', checkOut);
    if (guests) params.set('guests', guests);
    persistRecent(city);

    if (onSearch) onSearch({ city, checkIn, checkOut, guests });
    else navigate(`/search?${params.toString()}`);
  };

  const filteredRecent = recent.filter((item) => !city || item.toLowerCase().includes(city.toLowerCase()));
  const popular = (suggestions.length ? suggestions : cities.slice(0, 6)).filter((item) => !filteredRecent.includes(item));
  const dropdownItems = [...filteredRecent, ...popular];

  const handleKeyDown = (event) => {
    if (event.key === 'Escape') {
      setShowSuggestions(false);
      return;
    }
    if (event.key === 'ArrowDown') {
      event.preventDefault();
      setShowSuggestions(true);
      setActiveIndex((index) => Math.min(dropdownItems.length - 1, index + 1));
    }
    if (event.key === 'ArrowUp') {
      event.preventDefault();
      setActiveIndex((index) => Math.max(0, index - 1));
    }
    if (event.key === 'Enter' && activeIndex >= 0 && dropdownItems[activeIndex]) {
      event.preventDefault();
      const selected = dropdownItems[activeIndex];
      setCity(selected);
      setShowSuggestions(false);
      persistRecent(selected);
    }
  };

  return (
    <form onSubmit={handleSearch} className={`${styles.form} ${compact ? styles.compact : ''}`}>
      <div className={styles.field} ref={suggestRef}>
        <MapPin size={16} className={styles.icon} />
        <input
          type="text"
          value={city}
          onChange={(event) => handleCityInput(event.target.value)}
          onFocus={() => {
            setSuggestions(city ? cities.filter((item) => item.toLowerCase().includes(city.toLowerCase())).slice(0, 8) : cities.slice(0, 6));
            setShowSuggestions(true);
          }}
          onKeyDown={handleKeyDown}
          className={styles.textInput}
          placeholder={t('destination')}
          aria-label={t('destination')}
        />
        <SearchDropdown
          open={showSuggestions}
          recent={filteredRecent}
          popular={popular}
          activeIndex={activeIndex}
          onSelect={(value) => {
            setCity(value);
            setShowSuggestions(false);
            persistRecent(value);
          }}
          onClearRecent={() => {
            setRecent([]);
            localStorage.removeItem(RECENT_KEY);
          }}
        />
      </div>

      <div className={styles.divider} />

      <div className={styles.field}>
        <Calendar size={16} className={styles.icon} />
        <div className={styles.dateGroup}>
          <input
            type="date"
            value={checkIn}
            onChange={(event) => setCheckIn(event.target.value)}
            min={toInputDate(new Date())}
            className={styles.dateInput}
            aria-label={t('checkIn')}
          />
          <span className={styles.dateSep}>{language === 'ar' ? '<' : '>'}</span>
          <input
            type="date"
            value={checkOut}
            onChange={(event) => setCheckOut(event.target.value)}
            min={checkIn || toInputDate(addDays(new Date(), 1))}
            className={styles.dateInput}
            aria-label={t('checkOut')}
          />
        </div>
      </div>

      <div className={styles.divider} />

      <div className={styles.field}>
        <Users size={16} className={styles.icon} />
        <div className={styles.guestControl}>
          <button type="button" onClick={() => setGuests((value) => Math.max(1, value - 1))} className={styles.guestBtn} aria-label="Decrease guests">-</button>
          <span className={styles.guestCount}>{guests} {t('guests')}</span>
          <button type="button" onClick={() => setGuests((value) => Math.min(10, value + 1))} className={styles.guestBtn} aria-label="Increase guests">+</button>
        </div>
      </div>

      <button type="submit" className={styles.searchBtn}>
        <Search size={18} />
        <span>{t('search')}</span>
      </button>
    </form>
  );
};

export default HeroSearchBar;
