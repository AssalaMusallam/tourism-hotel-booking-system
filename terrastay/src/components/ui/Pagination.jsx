import { ChevronLeft, ChevronRight } from 'lucide-react';
import styles from './Pagination.module.css';

const Pagination = ({ page, totalPages, onPageChange }) => {
  if (totalPages <= 1) return null;

  const pages = Array.from({ length: totalPages }, (_, i) => i + 1);
  const visiblePages = pages.filter(
    (p) => p === 1 || p === totalPages || Math.abs(p - page) <= 1
  );

  const renderPages = [];
  let prev = null;
  for (const p of visiblePages) {
    if (prev && p - prev > 1) {
      renderPages.push(<span key={`ellipsis-${p}`} className={styles.ellipsis}>…</span>);
    }
    renderPages.push(
      <button
        key={p}
        className={`${styles.pageBtn} ${p === page ? styles.active : ''}`}
        onClick={() => onPageChange(p)}
        aria-label={`Page ${p}`}
        aria-current={p === page ? 'page' : undefined}
      >
        {p}
      </button>
    );
    prev = p;
  }

  return (
    <div className={styles.container}>
      <button
        className={styles.navBtn}
        onClick={() => onPageChange(page - 1)}
        disabled={page === 1}
        aria-label="Previous page"
      >
        <ChevronLeft size={18} />
      </button>
      {renderPages}
      <button
        className={styles.navBtn}
        onClick={() => onPageChange(page + 1)}
        disabled={page === totalPages}
        aria-label="Next page"
      >
        <ChevronRight size={18} />
      </button>
    </div>
  );
};

export default Pagination;
