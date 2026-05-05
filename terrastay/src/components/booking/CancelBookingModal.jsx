import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import Modal from '../ui/Modal';
import Button from '../ui/Button';
import styles from './CancelBookingModal.module.css';

const schema = z.object({
  reason: z.string().trim().min(1, 'Please enter a cancellation reason'),
});

const CancelBookingModal = ({ booking, isOpen, onClose, onConfirm, loading }) => {
  const { register, handleSubmit, reset, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { reason: '' },
  });

  const submit = (values) => {
    onConfirm(values.reason, () => reset());
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Cancel booking" size="sm">
      <form className={styles.form} onSubmit={handleSubmit(submit)}>
        <p className={styles.copy}>
          Tell us why you are cancelling booking <strong>#{booking?.id}</strong>.
        </p>
        <label className={styles.label} htmlFor="cancelReason">Cancellation reason</label>
        <textarea
          id="cancelReason"
          className={[styles.textarea, errors.reason ? styles.error : ''].filter(Boolean).join(' ')}
          rows={4}
          {...register('reason')}
        />
        {errors.reason && <span className={styles.err}>{errors.reason.message}</span>}
        <div className={styles.actions}>
          <Button type="button" variant="ghost" onClick={onClose}>Keep Booking</Button>
          <Button type="submit" variant="danger" loading={loading}>Cancel Booking</Button>
        </div>
      </form>
    </Modal>
  );
};

export default CancelBookingModal;
