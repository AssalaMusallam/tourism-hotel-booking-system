import { useQuery } from '@tanstack/react-query';
import api from '../../api/axios';
import StarRating from '../ui/StarRating';
import styles from './RatingSummaryWidget.module.css';

const rows = [
  { stars: 5, countKey: 'fiveStars', percentKey: 'fiveStarPercent', color: 'green' },
  { stars: 4, countKey: 'fourStars', percentKey: 'fourStarPercent', color: 'lime' },
  { stars: 3, countKey: 'threeStars', percentKey: 'threeStarPercent', color: 'yellow' },
  { stars: 2, countKey: 'twoStars', percentKey: 'twoStarPercent', color: 'orange' },
  { stars: 1, countKey: 'oneStar', percentKey: 'oneStarPercent', color: 'red' },
];

const fetchRatingSummary = async (hotelId) => {
  const response = await api.get(`/api/hotels/${hotelId}/reviews/summary`);
  return response.data;
};

const RatingSummaryWidget = ({ hotelId }) => {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['rating-summary', hotelId],
    queryFn: () => fetchRatingSummary(hotelId),
    enabled: Boolean(hotelId),
    staleTime: 5 * 60 * 1000,
  });

  if (isLoading) {
    return (
      <section className={styles.card}>
        <div className={styles.left}>
          <div className="skeleton" style={{ width: 92, height: 56 }} />
          <div className="skeleton" style={{ width: 140, height: 20 }} />
        </div>
        <div className={styles.right}>
          {rows.map((row) => <div key={row.stars} className="skeleton" style={{ height: 24 }} />)}
        </div>
      </section>
    );
  }

  if (isError || !data || Number(data.totalReviews || 0) === 0) {
    return (
      <section className={styles.empty}>
        No reviews yet
      </section>
    );
  }

  return (
    <section className={styles.card} aria-label={`Rating summary for ${data.hotelName}`}>
      <div className={styles.left}>
        <strong className={styles.average}>{Number(data.averageRating || 0).toFixed(1)}</strong>
        <StarRating value={data.averageRating || 0} size={22} />
        <span className={styles.total}>{data.totalReviews} reviews</span>
      </div>

      <div className={styles.right}>
        {rows.map((row) => {
          const percent = Number(data[row.percentKey] || 0);
          const count = Number(data[row.countKey] || 0);
          return (
            <div key={row.stars} className={styles.barRow}>
              <span className={styles.starLabel}>{row.stars}★</span>
              <div className={styles.track}>
                <div
                  className={[styles.fill, styles[row.color]].join(' ')}
                  style={{ width: `${Math.min(100, Math.max(0, percent))}%` }}
                />
              </div>
              <span className={styles.percent}>{Math.round(percent)}%</span>
              <span className={styles.count}>{count}</span>
            </div>
          );
        })}
      </div>
    </section>
  );
};

export default RatingSummaryWidget;
