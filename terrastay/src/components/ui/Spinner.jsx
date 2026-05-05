import styles from './Spinner.module.css';

const Spinner = ({ size = 'md', centered = false }) => {
  return (
    <div className={centered ? styles.centered : ''}>
      <div className={`${styles.spinner} ${styles[size]}`} />
    </div>
  );
};

export default Spinner;
