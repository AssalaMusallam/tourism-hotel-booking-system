import styles from './StatsCard.module.css';

const StatsCard = ({ title, value, icon: Icon, trend, color = 'terracotta' }) => {
  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <span className={styles.title}>{title}</span>
        <div className={`${styles.iconWrap} ${styles[color]}`}>
          <Icon size={20} />
        </div>
      </div>
      <div className={styles.value}>{value}</div>
      {trend && <div className={styles.trend}>{trend}</div>}
    </div>
  );
};

export default StatsCard;
