import { ChevronLeft, ChevronRight } from 'lucide-react';
import styles from './Pagination.module.css';

// page is 0-indexed (backend), displayed as 1-indexed
const Pagination = ({ page, totalPages, onPageChange }) => {
  if (totalPages <= 1) return null;
  const current = page; // 0-indexed

  const pages = [];
  for (let i = 0; i < totalPages; i++) {
    if (i === 0 || i === totalPages - 1 || Math.abs(i - current) <= 1) {
      pages.push(i);
    } else if (pages[pages.length - 1] !== '...') {
      pages.push('...');
    }
  }

  return (
    <div className={styles.wrap}>
      <button
        className={styles.nav}
        disabled={current === 0}
        onClick={() => onPageChange(current - 1)}
        aria-label="Previous"
      >
        <ChevronLeft size={16} />
      </button>
      {pages.map((p, i) =>
        p === '...' ? (
          <span key={`e${i}`} className={styles.ellipsis}>…</span>
        ) : (
          <button
            key={p}
            className={[styles.page, p === current ? styles.active : ''].join(' ')}
            onClick={() => onPageChange(p)}
            aria-current={p === current ? 'page' : undefined}
          >
            {p + 1}
          </button>
        )
      )}
      <button
        className={styles.nav}
        disabled={current === totalPages - 1}
        onClick={() => onPageChange(current + 1)}
        aria-label="Next"
      >
        <ChevronRight size={16} />
      </button>
    </div>
  );
};
export default Pagination;
