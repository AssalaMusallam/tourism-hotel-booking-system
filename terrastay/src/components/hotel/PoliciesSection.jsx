import { Clock, XCircle, Info } from 'lucide-react';
import styles from './PoliciesSection.module.css';

const PoliciesSection = ({ policies }) => {
  return (
    <div className={styles.container}>
      <div className={styles.item}>
        <div className={styles.icon}><Clock size={20} /></div>
        <div>
          <h4>Check-in / Check-out</h4>
          <p>{policies || 'Check-in: 3:00 PM | Check-out: 12:00 PM'}</p>
        </div>
      </div>
      <div className={styles.item}>
        <div className={styles.icon}><XCircle size={20} /></div>
        <div>
          <h4>Cancellation Policy</h4>
          <p>Free cancellation up to 48 hours before check-in. After that, the first night is non-refundable.</p>
        </div>
      </div>
      <div className={styles.item}>
        <div className={styles.icon}><Info size={20} /></div>
        <div>
          <h4>House Rules</h4>
          <p>No smoking indoors. Pets are not allowed. Quiet hours from 10:00 PM to 8:00 AM.</p>
        </div>
      </div>
    </div>
  );
};

export default PoliciesSection;
