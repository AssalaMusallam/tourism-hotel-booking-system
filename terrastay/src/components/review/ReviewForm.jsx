import { useMemo, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { addDays, format, parseISO } from 'date-fns';
import toast from 'react-hot-toast';
import { ShieldCheck } from 'lucide-react';
import { mapReviewError } from '../../api/reviews';
import { useSubmitReview } from '../../hooks/useReviewQueries';
import Button from '../ui/Button';
import Input from '../ui/Input';
import StarRating from '../ui/StarRating';
import ReviewSuccess from './ReviewSuccess';
import styles from './ReviewForm.module.css';

const schema = z.object({
  bookingId: z.coerce.number({ message: 'bookingId is required' }),
  guestEmail: z.string().email('guestEmail must be a valid email'),
  rating: z.coerce.number().min(1, 'Please select a rating').max(5),
  comment: z.string().max(1000, 'comment cannot exceed 1000 characters').optional().or(z.literal('')),
});

const ReviewForm = ({ bookingId, guestEmail, hotelName, checkOut }) => {
  const [submittedReview, setSubmittedReview] = useState(null);
  const mutation = useSubmitReview();
  const reviewDeadline = useMemo(() => {
    if (!checkOut) return '';
    return format(addDays(parseISO(checkOut), 30), 'MMMM d, yyyy');
  }, [checkOut]);

  const form = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      bookingId: Number(bookingId),
      guestEmail: guestEmail || '',
      rating: 0,
      comment: '',
    },
  });

  const comment = form.watch('comment') || '';

  const onSubmit = (values) => {
    mutation.mutate({
      bookingId: Number(values.bookingId),
      guestEmail: values.guestEmail,
      rating: Number(values.rating),
      comment: values.comment?.trim() || undefined,
    }, {
      onSuccess: setSubmittedReview,
      onError: (error) => {
        const mapped = mapReviewError(error);
        if (mapped.field) {
          form.setError(mapped.field, { type: 'server', message: mapped.message });
          return;
        }
        toast.error(mapped.toast);
      },
    });
  };

  if (submittedReview) return <ReviewSuccess review={submittedReview} />;

  return (
    <section className={styles.card}>
      <div className={styles.header}>
        <div>
          <span className={styles.eyebrow}>Hotel review</span>
          <h1>{hotelName}</h1>
        </div>
        <span className={styles.verified}>
          <ShieldCheck size={16} />
          Verified Stay
        </span>
      </div>

      <p className={styles.deadline}>
        You have until {reviewDeadline} to submit your review
      </p>

      <form className={styles.form} onSubmit={form.handleSubmit(onSubmit)}>
        <input type="hidden" {...form.register('bookingId')} />
        <Input
          label="Guest email"
          disabled
          error={form.formState.errors.guestEmail?.message}
          {...form.register('guestEmail')}
        />

        <div className={styles.field}>
          <label className={styles.label}>Rating</label>
          <Controller
            control={form.control}
            name="rating"
            render={({ field }) => (
              <StarRating value={field.value} interactive onChange={field.onChange} size={30} />
            )}
          />
          {form.formState.errors.rating && (
            <span className={styles.err}>{form.formState.errors.rating.message}</span>
          )}
        </div>

        <div className={styles.field}>
          <div className={styles.labelRow}>
            <label className={styles.label} htmlFor="reviewComment">Comment</label>
            <span>{comment.length} / 1000</span>
          </div>
          <textarea
            id="reviewComment"
            className={[styles.textarea, form.formState.errors.comment ? styles.error : ''].filter(Boolean).join(' ')}
            maxLength={1000}
            rows={6}
            placeholder="Share your experience..."
            {...form.register('comment')}
          />
          {form.formState.errors.comment && (
            <span className={styles.err}>{form.formState.errors.comment.message}</span>
          )}
        </div>

        <Button type="submit" variant="primary" size="lg" loading={mutation.isPending}>
          Submit Review
        </Button>
      </form>
    </section>
  );
};

export default ReviewForm;
