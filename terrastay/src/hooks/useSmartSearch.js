import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { searchHotels } from '../api/hotelsApi';

const STORAGE_KEY = 'terrastay_recent_searches';

const readRecent = () => {
  try {
    const parsed = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
};

const saveRecent = (items) => localStorage.setItem(STORAGE_KEY, JSON.stringify(items));

export const useSmartSearch = ({ defaultQuery = '', defaultCity = '', onSearch } = {}) => {
  const navigate = useNavigate();
  const [query, setQuery] = useState(defaultQuery);
  const [city, setCity] = useState(defaultCity);
  const [checkIn, setCheckIn] = useState('');
  const [checkOut, setCheckOut] = useState('');
  const [open, setOpen] = useState(false);
  const [activeIndex, setActiveIndex] = useState(-1);
  const [recent, setRecent] = useState(readRecent);
  const [debouncedQuery, setDebouncedQuery] = useState(defaultQuery);

  useEffect(() => {
    const timeout = window.setTimeout(() => setDebouncedQuery(query.trim()), 300);
    return () => window.clearTimeout(timeout);
  }, [query]);

  const suggestionsQuery = useQuery({
    queryKey: ['smart-search', debouncedQuery],
    queryFn: () => searchHotels({ search: debouncedQuery, q: debouncedQuery, size: 5 }),
    enabled: debouncedQuery.length > 1,
    staleTime: 30_000,
  });

  const filteredRecent = useMemo(() => {
    const value = query.trim().toLowerCase();
    if (!value) return recent;
    return recent.filter((item) => item.query.toLowerCase().includes(value) || item.city?.toLowerCase().includes(value));
  }, [query, recent]);

  const liveHotels = suggestionsQuery.data?.content || [];
  const items = useMemo(() => [
    ...filteredRecent.map((item) => ({ type: 'recent', label: item.query, city: item.city })),
    ...liveHotels.map((hotel) => ({ type: 'hotel', label: hotel.name, city: hotel.city, hotel })),
  ], [filteredRecent, liveHotels]);

  const persist = (label, selectedCity = city) => {
    const clean = (label || selectedCity || '').trim();
    if (!clean) return;
    const nextItem = { query: clean, city: selectedCity || '', at: new Date().toISOString() };
    const next = [nextItem, ...recent.filter((item) => item.query !== clean)].slice(0, 8);
    setRecent(next);
    saveRecent(next);
  };

  const clearHistory = () => {
    setRecent([]);
    localStorage.removeItem(STORAGE_KEY);
  };

  const submit = (item) => {
    const label = typeof item === 'string' ? item : item?.label || query;
    const selectedCity = typeof item === 'object' ? item.city || city : city;
    const params = new URLSearchParams();
    if (label) params.set('q', label);
    if (selectedCity) params.set('city', selectedCity);
    if (checkIn) params.set('checkIn', checkIn);
    if (checkOut) params.set('checkOut', checkOut);
    persist(label, selectedCity);
    setOpen(false);
    if (onSearch) onSearch({ q: label, city: selectedCity, checkIn, checkOut });
    else navigate(`/search?${params.toString()}`);
  };

  const onKeyDown = (event) => {
    if (event.key === 'Escape') {
      setOpen(false);
      return;
    }
    if (event.key === 'ArrowDown') {
      event.preventDefault();
      setOpen(true);
      setActiveIndex((value) => Math.min(items.length - 1, value + 1));
    }
    if (event.key === 'ArrowUp') {
      event.preventDefault();
      setActiveIndex((value) => Math.max(0, value - 1));
    }
    if (event.key === 'Enter') {
      event.preventDefault();
      submit(activeIndex >= 0 ? items[activeIndex] : query);
    }
  };

  return {
    query,
    setQuery,
    city,
    setCity,
    checkIn,
    setCheckIn,
    checkOut,
    setCheckOut,
    open,
    setOpen,
    activeIndex,
    setActiveIndex,
    recent,
    filteredRecent,
    liveHotels,
    items,
    isLoading: suggestionsQuery.isFetching,
    clearHistory,
    submit,
    onKeyDown,
  };
};

export default useSmartSearch;
