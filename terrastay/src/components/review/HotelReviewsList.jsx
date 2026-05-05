import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';
import { ShieldCheck } from 'lucide-react';
import api from '../../api/axios';
import Button from '../ui/Button';
import StarRating from '../ui/StarRating';
import styles from './HotelReviewsList.module.css';

const fetchHotelReviews = async (hotelId, page) => {
  const response = await api.get(`/api/hotels/${hotelId}/reviews`, {
    params: { page, size: 10, sort: 'createdAt' },
  });
  return response.data;
};

const initials = (name = '') => {
  const parts = name.trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return '?';
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase();
};

const monthYear = (value) => {
  if (!value) return '';
  return format(new Date(value), 'MMMM yyyy');
};

const SkeletonCard = () => (
  <article className={styles.card}>
    <div className="skeleton" style={{ width: 44, height: 44, borderRadius: '50%' }} />
    <div className={styles.body}>
      <div className="skeleton" style={{ width: '38%', height: 18 }} />
      <div className="skeleton" style={{ width: '24%', height: 16 }} />
      <div className="skeleton" style={{ width: '88%', height: 54 }} />
    </div>
  </article>
);

const HotelReviewsList = ({ hotelId }) => {
  const [page, setPage] = useState(0);
  const [reviews, setReviews] = useState([]);

  const { data, isLoading, isFetching } = useQuery({
    queryKey: ['hotel-reviews', hotelId, page],
    queryFn: () => fetchHotelReviews(hotelId, page),
    enabled: Boolean(hotelId),
    staleTime: 2 * 60 * 1000,
  });

  useEffect(() => {
    if (!data) return;
    setReviews((current) => {
      const merged = page === 0 ? data.content || [] : [...current, ...(data.content || [])];
      const seen = new Set();
      return merged.filter((review) => {
        if (seen.has(review.id)) return false;
        seen.add(review.id);
        return true;
      });
    });
  }, [data, page]);

  const visibleReviews = reviews.length ? reviews : data?.content || [];
  const hasNext = Boolean(data?.hasNext);

  if (isLoading && page === 0) {
    return (
      <section className={styles.list}>
        {[0, 1, 2].map((item) => <SkeletonCard key={item} />)}
      </section>
    );
  }

  if (visibleReviews.length === 0) {
    return <section className={styles.empty}>No reviews yet</section>;
  }

  return (
    <section className={styles.wrap}>
      <div className={styles.list}>
        {visibleReviews.map((review) => (
          <article key={review.id} className={styles.card}>
            <div className={styles.avatar} aria-hidden="true">{initials(review.guestName)}</div>
            <div className={styles.body}>
              <div className={styles.top}>
                <div>
                  <h3 className={styles.name}>{review.guestName}</h3>
                  <div className={styles.meta}>
                    <StarRating value={review.rating} size={16} />
                    <span>{monthYear(review.createdAt)}</span>
                  </div>
                </div>
                {review.verifiedStay && (
                  <span className={styles.verified}>
                    <ShieldCheck size={14} />
                    Verified Stay
                  </span>
                )}
              </div>
              {review.comment?.trim() && <p className={styles.comment}>{review.comment}</p>}
            </div>
          </article>
        ))}
      </div>

      {hasNext && (
        <div className={styles.actions}>
          <Button variant="secondary" onClick={() => setPage((value) => value + 1)} loading={isFetching}>
            Load More
          </Button>
        </div>
      )}
    </section>
  );
};

export default HotelReviewsList;
