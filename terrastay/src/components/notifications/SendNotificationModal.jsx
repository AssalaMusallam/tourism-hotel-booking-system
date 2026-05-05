import { useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import {
  NOTIFICATION_TYPES,
  REFERENCE_TYPES,
} from '../../api/notifications';
import { useSendCustomNotification, useSendNotification } from '../../hooks/useNotifications';
import { useFormErrors } from '../../hooks/useFormErrors';
import useAuth from '../../hooks/useAuth';
import Button from '../ui/Button';
import ErrorBanner from '../ui/ErrorBanner';
import Input from '../ui/Input';
import Modal from '../ui/Modal';
import Select from '../ui/Select';
import styles from './SendNotificationModal.module.css';

const typeOptions = NOTIFICATION_TYPES.map((type) => ({ value: type, label: type.replaceAll('_', ' ') }));
const referenceOptions = REFERENCE_TYPES.map((type) => ({ value: type, label: type }));

const optionalString = z.string().optional().or(z.literal(''));

const templatedSchema = z.object({
  recipientEmail: z.string().email('Enter a valid email'),
  recipientName: z.string().trim().min(1, 'Recipient name is required'),
  type: z.string().min(1, 'Type is required'),
  referenceId: optionalString,
  referenceType: optionalString,
  hotelName: optionalString,
  roomType: optionalString,
  checkInDate: optionalString,
  checkOutDate: optionalString,
  totalAmount: optionalString,
  bookingReference: optionalString,
  cancellationReason: optionalString,
  paymentMethod: optionalString,
});

const customSchema = z.object({
  recipientEmail: z.string().email('Enter a valid email'),
  recipientName: z.string().trim().min(1, 'Recipient name is required'),
  subject: z.string().trim().min(1, 'Subject is required'),
  body: z.string().trim().min(1, 'Body is required'),
});

const templateFieldsFor = (type) => {
  if (type === 'BOOKING_CONFIRMED') {
    return ['hotelName', 'roomType', 'checkInDate', 'checkOutDate', 'totalAmount', 'bookingReference'];
  }
  if (type === 'BOOKING_CANCELLED') {
    return ['hotelName', 'bookingReference', 'cancellationReason'];
  }
  if (['PAYMENT_SUCCESS', 'PAYMENT_FAILED', 'PAYMENT_REFUNDED'].includes(type)) {
    return ['totalAmount', 'paymentMethod'];
  }
  return [];
};

const toPayload = (values) => {
  const payload = { ...values };
  Object.keys(payload).forEach((key) => {
    if (payload[key] === '') payload[key] = null;
  });
  if (payload.referenceId) payload.referenceId = Number(payload.referenceId);
  return payload;
};

const SendNotificationModal = ({ isOpen, onClose }) => {
  const { isAdmin } = useAuth();
  const [tab, setTab] = useState('templated');
  const [banner, setBanner] = useState('');
  const templatedForm = useForm({
    resolver: zodResolver(templatedSchema),
    defaultValues: { type: 'BOOKING_CONFIRMED' },
  });
  const customForm = useForm({
    resolver: zodResolver(customSchema),
  });
  const templatedErrors = useFormErrors(templatedForm);
  const customErrors = useFormErrors(customForm);
  const sendMutation = useSendNotification();
  const customMutation = useSendCustomNotification();
  const selectedType = templatedForm.watch('type');
  const visibleTemplateFields = useMemo(() => templateFieldsFor(selectedType), [selectedType]);

  useEffect(() => {
    if (!isOpen) return;
    setBanner('');
    setTab('templated');
    templatedForm.reset({ type: 'BOOKING_CONFIRMED' });
    customForm.reset();
  }, [isOpen]);

  const closeAfterSuccess = (message) => {
    toast.success(message);
    onClose();
  };

  const submitTemplated = (values) => {
    setBanner('');
    sendMutation.mutate(toPayload(values), {
      onSuccess: () => closeAfterSuccess('Notification sent'),
      onError: (error) => setBanner(templatedErrors.applyServerErrors(error).bannerMessage),
    });
  };

  const submitCustom = (values) => {
    setBanner('');
    customMutation.mutate(values, {
      onSuccess: () => closeAfterSuccess('Custom notification sent'),
      onError: (error) => setBanner(customErrors.applyServerErrors(error).bannerMessage),
    });
  };

  const renderTemplateField = (name) => {
    const dateField = name === 'checkInDate' || name === 'checkOutDate';
    const labels = {
      hotelName: 'Hotel name',
      roomType: 'Room type',
      checkInDate: 'Check-in date',
      checkOutDate: 'Check-out date',
      totalAmount: 'Total amount',
      bookingReference: 'Booking reference',
      cancellationReason: 'Cancellation reason',
      paymentMethod: 'Payment method',
    };

    return (
      <Input
        key={name}
        label={labels[name]}
        type={dateField ? 'date' : 'text'}
        error={templatedForm.formState.errors[name]?.message}
        {...templatedForm.register(name)}
      />
    );
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Send Notification" size="xl">
      <div className={styles.tabs}>
        <button
          type="button"
          className={`${styles.tab} ${tab === 'templated' ? styles.tabActive : ''}`}
          onClick={() => { setTab('templated'); setBanner(''); }}
        >
          Templated
        </button>
        {isAdmin && (
          <button
            type="button"
            className={`${styles.tab} ${tab === 'custom' ? styles.tabActive : ''}`}
            onClick={() => { setTab('custom'); setBanner(''); }}
          >
            Custom
          </button>
        )}
      </div>

      <ErrorBanner message={banner} onDismiss={() => setBanner('')} />

      {tab === 'templated' && (
        <form className={styles.form} onSubmit={templatedForm.handleSubmit(submitTemplated)} noValidate>
          <div className={styles.grid}>
            <Input
              label="Recipient email"
              type="email"
              error={templatedForm.formState.errors.recipientEmail?.message}
              {...templatedForm.register('recipientEmail')}
            />
            <Input
              label="Recipient name"
              error={templatedForm.formState.errors.recipientName?.message}
              {...templatedForm.register('recipientName')}
            />
            <Select
              label="Type"
              options={typeOptions}
              error={templatedForm.formState.errors.type?.message}
              {...templatedForm.register('type')}
            />
            <Input
              label="Reference ID"
              type="number"
              min="1"
              error={templatedForm.formState.errors.referenceId?.message}
              {...templatedForm.register('referenceId')}
            />
            <Select
              label="Reference type"
              placeholder="None"
              options={referenceOptions}
              error={templatedForm.formState.errors.referenceType?.message}
              {...templatedForm.register('referenceType')}
            />
          </div>
          {visibleTemplateFields.length > 0 && (
            <>
              <p className={styles.sectionTitle}>Template data</p>
              <div className={styles.grid}>
                {visibleTemplateFields.map(renderTemplateField)}
              </div>
            </>
          )}
          <div className={styles.actions}>
            <Button variant="ghost" onClick={onClose}>Cancel</Button>
            <Button type="submit" loading={sendMutation.isPending}>Send</Button>
          </div>
        </form>
      )}

      {tab === 'custom' && isAdmin && (
        <form className={styles.form} onSubmit={customForm.handleSubmit(submitCustom)} noValidate>
          <div className={styles.grid}>
            <Input
              label="Recipient email"
              type="email"
              error={customForm.formState.errors.recipientEmail?.message}
              {...customForm.register('recipientEmail')}
            />
            <Input
              label="Recipient name"
              error={customForm.formState.errors.recipientName?.message}
              {...customForm.register('recipientName')}
            />
          </div>
          <Input
            label="Subject"
            error={customForm.formState.errors.subject?.message}
            {...customForm.register('subject')}
          />
          <div className={styles.field}>
            <label className={styles.label}>Body</label>
            <textarea
              className={styles.textarea}
              placeholder="<p>Hello...</p>"
              {...customForm.register('body')}
            />
            {customForm.formState.errors.body && (
              <span className={styles.err}>{customForm.formState.errors.body.message}</span>
            )}
          </div>
          <div className={styles.actions}>
            <Button variant="ghost" onClick={onClose}>Cancel</Button>
            <Button type="submit" loading={customMutation.isPending}>Send</Button>
          </div>
        </form>
      )}
    </Modal>
  );
};

export default SendNotificationModal;
