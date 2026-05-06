import Skeleton from './Skeleton';
import styles from './SkeletonCard.module.css';

const SkeletonCard = () => (
  <article className={styles.card}>
    <Skeleton className={styles.media} />
    <div className={styles.body}>
      <Skeleton variant="text" style={{ width: '42%' }} />
      <Skeleton variant="text" style={{ width: '78%' }} />
      <Skeleton variant="text" style={{ width: '55%' }} />
      <div className={styles.footer}>
        <Skeleton variant="text" style={{ width: 90 }} />
        <Skeleton style={{ width: 96, height: 38, borderRadius: 'var(--radius-md)' }} />
      </div>
    </div>
  </article>
);

export default SkeletonCard;
