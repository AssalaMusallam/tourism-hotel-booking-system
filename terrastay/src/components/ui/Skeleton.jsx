import styles from './Skeleton.module.css';

const Skeleton = ({ className = '', variant = 'block', style }) => (
  <span className={`${styles.skeleton} ${styles[variant] || ''} ${className}`} style={style} />
);

export default Skeleton;
