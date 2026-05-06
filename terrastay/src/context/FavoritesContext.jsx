import { createContext, useContext, useMemo, useState } from 'react';
import toast from 'react-hot-toast';

const STORAGE_KEY = 'terrastay_favorites';
export const FavoritesContext = createContext(null);

const readFavorites = () => {
  try {
    const parsed = JSON.parse(localStorage.getItem(STORAGE_KEY) || '{"hotels":[]}');
    return Array.isArray(parsed.hotels) ? parsed.hotels.map(String) : [];
  } catch {
    return [];
  }
};

const writeFavorites = (hotels) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify({ hotels }));
};

export const FavoritesProvider = ({ children }) => {
  const [favorites, setFavorites] = useState(readFavorites);

  const addFavorite = (hotelId) => {
    setFavorites((current) => {
      const id = String(hotelId);
      if (current.includes(id)) return current;
      const next = [...current, id];
      writeFavorites(next);
      toast.success('تمت الإضافة إلى المفضلة');
      return next;
    });
  };

  const removeFavorite = (hotelId) => {
    setFavorites((current) => {
      const next = current.filter((id) => id !== String(hotelId));
      writeFavorites(next);
      toast('تمت الإزالة من المفضلة');
      return next;
    });
  };

  const toggleFavorite = (hotelId) => {
    const id = String(hotelId);
    if (favorites.includes(id)) removeFavorite(id);
    else addFavorite(id);
  };

  const value = useMemo(() => ({
    favorites,
    addFavorite,
    removeFavorite,
    toggleFavorite,
    isFavorite: (hotelId) => favorites.includes(String(hotelId)),
  }), [favorites]);

  return <FavoritesContext.Provider value={value}>{children}</FavoritesContext.Provider>;
};

export const useFavoritesContext = () => {
  const value = useContext(FavoritesContext);
  if (!value) throw new Error('useFavoritesContext must be used within FavoritesProvider');
  return value;
};

export default FavoritesContext;
