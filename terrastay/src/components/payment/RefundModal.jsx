import { useState } from 'react';
import Modal from '../ui/Modal';
import Button from '../ui/Button';
import styles from './RefundModal.module.css';

const RefundModal = ({ isOpen, onClose, payment, onRefund, loading }) => {
  const [reason, setReason] = useState('');
  const remaining = 500 - reason.length;

  const submit = (event) => {
    event.preventDefault();
    onRefund(reason.trim(), () => setReason(''));
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Request refund" size="sm">
      <form className={styles.form} onSubmit={submit}>
        <p className={styles.copy}>
          Refund payment <strong>#{payment?.id}</strong> for {payment?.currency} {Number(payment?.amount || 0).toFixed(2)}.
        </p>
        <label className={styles.label} htmlFor="refundReason">Reason</label>
        <textarea
          id="refundReason"
          value={reason}
          maxLength={500}
          rows={4}
          onChange={(event) => setReason(event.target.value)}
          className={styles.textarea}
        />
        <span className={styles.counter}>{remaining} characters left</span>
        <div className={styles.actions}>
          <Button type="button" variant="ghost" onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="primary" loading={loading}>Confirm Refund</Button>
        </div>
      </form>
    </Modal>
  );
};

export default RefundModal;
