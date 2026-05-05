import styles from './Spinner.module.css';

const Spinner = ({ size = 'md', centered }) => (
  <div className={centered ? styles.centered : ''}>
    <div className={[styles.spinner, styles[size]].join(' ')} />
  </div>
);
export default Spinner;
