import { useQueries } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Heart } from 'lucide-react';
import { getHotelById } from '../api/hotelsApi';
import useFavorites from '../hooks/useFavorites';
import useLanguage from '../hooks/useLanguage';
import HotelCard from '../components/hotel/HotelCard';
import SkeletonCard from '../components/ui/SkeletonCard';
import styles from './FavoritesPage.module.css';

const FavoritesPage = () => {
  const navigate = useNavigate();
  const { favorites } = useFavorites();
  const { t } = useLanguage();
  const hotelQueries = useQueries({
    queries: favorites.map((id) => ({
      queryKey: ['favorites', 'hotel', id],
      queryFn: () => getHotelById(id),
      enabled: !!id,
    })),
  });

  const isLoading = hotelQueries.some((query) => query.isLoading);
  const hotels = hotelQueries.map((query) => query.data).filter(Boolean);

  return (
    <section className={`container ${styles.page}`}>
      <div className={styles.header}>
        <div>
          <span className={styles.eyebrow}>TerraStay</span>
          <h1>{t('favorites')}</h1>
        </div>
        <Heart size={28} />
      </div>

      {favorites.length === 0 ? (
        <div className={styles.empty}>
          <Heart size={42} />
          <h2>{t('noFavorites')}</h2>
          <button type="button" onClick={() => navigate('/search')}>{t('exploreHotels')}</button>
        </div>
      ) : (
        <div className={styles.grid}>
          {isLoading
            ? favorites.map((id) => <SkeletonCard key={id} />)
            : hotels.map((hotel, index) => <HotelCard key={hotel.id} hotel={hotel} index={index} />)}
        </div>
      )}
    </section>
  );
};

export default FavoritesPage;
