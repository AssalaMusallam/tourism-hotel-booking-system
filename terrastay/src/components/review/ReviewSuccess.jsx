import { CheckCircle2, Star } from 'lucide-react';
import { Link } from 'react-router-dom';
import styles from './ReviewSuccess.module.css';

const ReviewSuccess = ({ review }) => (
  <section className={styles.card}>
    <CheckCircle2 className={styles.icon} size={54} />
    <h1>Thank you for your review!</h1>
    <p className={styles.hotel}>{review.hotelName}</p>
    <div className={styles.stars} aria-label={`${review.rating} stars`}>
      {[1, 2, 3, 4, 5].map((star) => (
        <Star
          key={star}
          size={22}
          fill={star <= review.rating ? 'var(--color-warning)' : 'var(--color-border)'}
          color={star <= review.rating ? 'var(--color-warning)' : 'var(--color-border)'}
        />
      ))}
    </div>
    {review.comment && <p className={styles.comment}>{review.comment}</p>}
    <p className={styles.created}>Submitted {review.createdAt}</p>
    <Link className={styles.link} to={`/hotels/${review.hotelId}/reviews`}>
      View Hotel Reviews
    </Link>
  </section>
);

export default ReviewSuccess;
