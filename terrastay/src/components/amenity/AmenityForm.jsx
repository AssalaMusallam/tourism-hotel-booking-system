import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import Input from '../ui/Input';
import Select from '../ui/Select';
import Button from '../ui/Button';
import styles from './AmenityForm.module.css';

const CATEGORY_OPTIONS = [
  { value: 'CONNECTIVITY',   label: 'Connectivity' },
  { value: 'WELLNESS',       label: 'Wellness & Health' },
  { value: 'ENTERTAINMENT',  label: 'Entertainment' },
  { value: 'COMFORT',        label: 'Comfort' },
  { value: 'DINING',         label: 'Dining' },
  { value: 'PARKING',        label: 'Parking' },
  { value: 'SECURITY',       label: 'Security' },
  { value: 'CLEANING',       label: 'Cleaning' },
  { value: 'ACCESSIBILITY',  label: 'Accessibility' },
  { value: 'OUTDOOR',        label: 'Outdoor' },
];

const schema = z.object({
  name:        z.string().min(3, 'Name must be 3-100 characters').max(100),
  description: z.string().min(10, 'Description must be 10-500 characters').max(500),
  category:    z.enum(['CONNECTIVITY','WELLNESS','ENTERTAINMENT','COMFORT','DINING','PARKING','SECURITY','CLEANING','ACCESSIBILITY','OUTDOOR'], { message: 'Category required' }),
  premium:     z.boolean().optional(),
  active:      z.boolean().optional(),
});

const AmenityForm = ({ amenity, onSubmit, loading }) => {
  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      name:        amenity?.name || '',
      description: amenity?.description || '',
      category:    amenity?.category || '',
      premium:     amenity?.premium ?? false,
      active:      amenity?.active ?? true,
    },
  });

  const handleFormSubmit = (data) => {
    onSubmit(data);
  };

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} className={styles.form}>
      <Input label="Amenity Name *" error={errors.name?.message} {...register('name')} placeholder="e.g. Free Wi-Fi" />
      <div className={styles.field}>
        <label className={styles.label}>Description *</label>
        <textarea
          className={[styles.textarea, errors.description ? styles.textareaError : ''].filter(Boolean).join(' ')}
          rows={3}
          {...register('description')}
          placeholder="Describe this amenity (10-500 chars)..."
        />
        {errors.description && <span className={styles.err}>{errors.description.message}</span>}
      </div>
      <Select
        label="Category *"
        options={CATEGORY_OPTIONS}
        placeholder="Select category..."
        error={errors.category?.message}
        {...register('category')}
      />
      <div className={styles.checkRow}>
        <label className={styles.checkLabel}>
          <input type="checkbox" {...register('premium')} />
          <span>Premium amenity</span>
        </label>
        <label className={styles.checkLabel}>
          <input type="checkbox" {...register('active')} />
          <span>Active</span>
        </label>
      </div>
      <div className={styles.actions}>
        <Button type="submit" variant="primary" loading={loading}>
          {amenity ? 'Update Amenity' : 'Create Amenity'}
        </Button>
      </div>
    </form>
  );
};
export default AmenityForm;
