import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import Input from '../ui/Input';
import Select from '../ui/Select';
import Button from '../ui/Button';
import { CITIES } from '../../constants/cities';
import { AMENITIES } from '../../constants/amenities';
import styles from './HotelForm.module.css';

const schema = z.object({
  name: z.string().min(3, 'Name must be at least 3 characters'),
  city: z.string().min(1, 'Please select a city'),
  address: z.string().min(5, 'Address is required'),
  stars: z.coerce.number().min(1).max(5),
  description: z.string().min(20, 'Description must be at least 20 characters'),
  phone: z.string().min(7, 'Phone is required'),
  email: z.string().email('Invalid email'),
  policies: z.string().optional(),
});

const HotelForm = ({ hotel, onSubmit, loading }) => {
  const { register, handleSubmit, watch, setValue, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: hotel || { stars: 3, amenities: [] },
  });

  const selectedAmenities = watch('amenities') || [];

  const toggleAmenity = (a) => {
    const next = selectedAmenities.includes(a)
      ? selectedAmenities.filter((x) => x !== a)
      : [...selectedAmenities, a];
    setValue('amenities', next);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className={styles.form}>
      <div className={styles.row}>
        <Input label="Hotel Name" error={errors.name?.message} {...register('name')} placeholder="e.g. Dar Al-Sultan Heritage Hotel" />
        <Select
          label="City"
          error={errors.city?.message}
          options={CITIES.map((c) => ({ value: c.value, label: c.label }))}
          placeholder="Select city"
          {...register('city')}
        />
      </div>

      <Input label="Address" error={errors.address?.message} {...register('address')} placeholder="Full street address" />

      <div className={styles.row}>
        <Select
          label="Star Rating"
          error={errors.stars?.message}
          options={[5,4,3,2,1].map((s) => ({ value: s, label: '★'.repeat(s) + ` (${s} Star${s>1?'s':''})` }))}
          {...register('stars')}
        />
        <Input label="Phone" error={errors.phone?.message} {...register('phone')} placeholder="+972-X-XXXXXXX" />
      </div>

      <Input label="Email" type="email" error={errors.email?.message} {...register('email')} placeholder="hotel@example.com" />

      <div className={styles.field}>
        <label className={styles.label}>Description</label>
        <textarea
          {...register('description')}
          rows={4}
          className={`${styles.textarea} ${errors.description ? styles.textareaError : ''}`}
          placeholder="Describe the hotel, location, highlights..."
        />
        {errors.description && <span className={styles.errorMsg}>{errors.description.message}</span>}
      </div>

      <div className={styles.field}>
        <label className={styles.label}>Amenities</label>
        <div className={styles.amenitiesGrid}>
          {AMENITIES.map((a) => (
            <label key={a.value} className={styles.amenityCheck}>
              <input
                type="checkbox"
                checked={selectedAmenities.includes(a.value)}
                onChange={() => toggleAmenity(a.value)}
              />
              {a.label}
            </label>
          ))}
        </div>
      </div>

      <div className={styles.field}>
        <label className={styles.label}>Policies (optional)</label>
        <textarea
          {...register('policies')}
          rows={2}
          className={styles.textarea}
          placeholder="Check-in/out times, cancellation policy..."
        />
      </div>

      <div className={styles.actions}>
        <Button type="submit" variant="primary" loading={loading}>
          {hotel ? 'Update Hotel' : 'Create Hotel'}
        </Button>
      </div>
    </form>
  );
};

export default HotelForm;
