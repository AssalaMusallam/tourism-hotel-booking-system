import { useEffect } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import Input from '../ui/Input';
import Select from '../ui/Select';
import StarRating from '../ui/StarRating';
import Button from '../ui/Button';
import { useMinimalAmenities } from '../../hooks/useCatalogQueries';
import styles from './HotelForm.module.css';

// AmenityMinimalDTO: { id, name } only - no category for grouping
// amenityNames in response, amenityIds in request

const CATEGORIES = [
  'CONNECTIVITY','WELLNESS','ENTERTAINMENT','COMFORT','DINING',
  'PARKING','SECURITY','CLEANING','ACCESSIBILITY','OUTDOOR',
];

const schema = z.object({
  name:        z.string().min(1,'Name required').max(200),
  address:     z.string().min(1,'Address required').max(500),
  city:        z.string().min(1,'City required').max(100),
  country:     z.string().min(1,'Country required').max(100),
  description: z.string().max(2000).optional().or(z.literal('')),
  phoneNumber: z.string().max(30).optional().or(z.literal('')),
  email:       z.string().email('Invalid email').max(320).optional().or(z.literal('')),
  websiteUrl:  z.string().max(500).optional().or(z.literal('')),
  rating:      z.coerce.number().min(0).max(5).optional(),
  latitude:    z.coerce.number().min(-90).max(90).optional().or(z.literal('')),
  longitude:   z.coerce.number().min(-180).max(180).optional().or(z.literal('')),
  checkInTime:  z.string().regex(/^\d{2}:\d{2}(:\d{2})?$/, 'HH:MM format').optional().or(z.literal('')),
  checkOutTime: z.string().regex(/^\d{2}:\d{2}(:\d{2})?$/, 'HH:MM format').optional().or(z.literal('')),
  policies:                  z.string().max(2000).optional().or(z.literal('')),
  cancellationPolicySummary: z.string().max(1000).optional().or(z.literal('')),
  status: z.enum(['ACTIVE','INACTIVE']).optional(),
  amenityIds: z.array(z.number()).optional(),
});

const normalizeTime = (t) => {
  if (!t) return '';
  // "HH:mm:ss" → "HH:mm" for display
  return t.substring(0, 5);
};

const toApiTime = (t) => {
  if (!t) return undefined;
  return t.length === 5 ? `${t}:00` : t;
};

const HotelForm = ({ hotel, onSubmit, loading }) => {
  const { data: minimalData } = useMinimalAmenities({ active: true, size: 200 });
  const amenities = minimalData?.content || [];

  const { register, handleSubmit, control, setValue, watch, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      name: hotel?.name || '',
      address: hotel?.address || '',
      city: hotel?.city || '',
      country: hotel?.country || '',
      description: hotel?.description || '',
      phoneNumber: hotel?.phoneNumber || '',
      email: hotel?.email || '',
      websiteUrl: hotel?.websiteUrl || '',
      rating: hotel?.rating || 0,
      latitude: hotel?.latitude || '',
      longitude: hotel?.longitude || '',
      checkInTime: normalizeTime(hotel?.checkInTime) || '',
      checkOutTime: normalizeTime(hotel?.checkOutTime) || '',
      policies: hotel?.policies || '',
      cancellationPolicySummary: hotel?.cancellationPolicySummary || '',
      status: hotel?.status || 'ACTIVE',
      amenityIds: [],
    },
  });

  useEffect(() => {
    if (hotel && amenities.length > 0) {
      // Map names to IDs if we only have names in the response
      const names = hotel.amenityNames ? [...hotel.amenityNames] : [];
      if (names.length > 0) {
        const ids = amenities
          .filter(a => names.includes(a.name))
          .map(a => a.id);
        setValue('amenityIds', ids);
      }
    }
  }, [hotel, amenities, setValue]);

  const selectedIds = watch('amenityIds') || [];

  const toggleAmenity = (id) => {
    const next = selectedIds.includes(id)
      ? selectedIds.filter((x) => x !== id)
      : [...selectedIds, id];
    setValue('amenityIds', next);
  };

  const handleFormSubmit = (data) => {
    const payload = {
      ...data,
      checkInTime:  toApiTime(data.checkInTime)  || undefined,
      checkOutTime: toApiTime(data.checkOutTime) || undefined,
      rating:    data.rating   || undefined,
      latitude:  data.latitude  !== '' ? Number(data.latitude)  : undefined,
      longitude: data.longitude !== '' ? Number(data.longitude) : undefined,
      email:       data.email || undefined,
      phoneNumber: data.phoneNumber || undefined,
      websiteUrl:  data.websiteUrl  || undefined,
      description: data.description || undefined,
      amenityIds:  data.amenityIds?.length ? data.amenityIds : undefined,
    };
    onSubmit(payload);
  };

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} className={styles.form}>
      {/* Basic Info */}
      <h4 className={styles.section}>Basic Information</h4>
      <div className={styles.row2}>
        <Input label="Hotel Name *" error={errors.name?.message} {...register('name')} placeholder="e.g. Grand Jerusalem Hotel" />
        <Select label="Status" options={[{value:'ACTIVE',label:'Active'},{value:'INACTIVE',label:'Inactive'}]} {...register('status')} />
      </div>
      <Input label="Address *" error={errors.address?.message} {...register('address')} placeholder="123 Main Street" />
      <div className={styles.row2}>
        <Input label="City *" error={errors.city?.message} {...register('city')} />
        <Input label="Country *" error={errors.country?.message} {...register('country')} />
      </div>
      <div className={styles.field}>
        <label className={styles.label}>Description</label>
        <textarea className={styles.textarea} rows={3} {...register('description')} placeholder="Hotel description..." />
        {errors.description && <span className={styles.err}>{errors.description.message}</span>}
      </div>

      {/* Star Rating */}
      <div className={styles.field}>
        <label className={styles.label}>Star Rating</label>
        <Controller
          control={control}
          name="rating"
          render={({ field }) => (
            <StarRating value={field.value || 0} interactive onChange={(v) => field.onChange(v)} />
          )}
        />
      </div>

      {/* Contact */}
      <h4 className={styles.section}>Contact</h4>
      <div className={styles.row3}>
        <Input label="Phone" error={errors.phoneNumber?.message} {...register('phoneNumber')} placeholder="+970-2-XXX-XXXX" />
        <Input label="Email" type="email" error={errors.email?.message} {...register('email')} />
        <Input label="Website URL" error={errors.websiteUrl?.message} {...register('websiteUrl')} placeholder="https://" />
      </div>

      {/* Location */}
      <h4 className={styles.section}>Location</h4>
      <div className={styles.row2}>
        <Input label="Latitude (-90 to 90)" type="number" step="any" error={errors.latitude?.message} {...register('latitude')} />
        <Input label="Longitude (-180 to 180)" type="number" step="any" error={errors.longitude?.message} {...register('longitude')} />
      </div>

      {/* Check In/Out */}
      <h4 className={styles.section}>Times</h4>
      <div className={styles.row2}>
        <Input label="Check-in Time (HH:MM)" {...register('checkInTime')} placeholder="14:00" error={errors.checkInTime?.message} />
        <Input label="Check-out Time (HH:MM)" {...register('checkOutTime')} placeholder="12:00" error={errors.checkOutTime?.message} />
      </div>

      {/* Policies */}
      <h4 className={styles.section}>Policies</h4>
      <div className={styles.field}>
        <label className={styles.label}>General Policies</label>
        <textarea className={styles.textarea} rows={3} {...register('policies')} />
      </div>
      <div className={styles.field}>
        <label className={styles.label}>Cancellation Policy Summary</label>
        <textarea className={styles.textarea} rows={2} {...register('cancellationPolicySummary')} />
      </div>

      {/* Amenities — loaded from /api/amenities/minimal */}
      <h4 className={styles.section}>Amenities</h4>
      <div className={styles.amenityGrid}>
        {amenities.map((a) => (
          <label key={a.id} className={styles.amenityCheck}>
            <input
              type="checkbox"
              checked={selectedIds.includes(a.id)}
              onChange={() => toggleAmenity(a.id)}
            />
            <span>{a.name}</span>
          </label>
        ))}
        {amenities.length === 0 && <span className={styles.noAmenities}>No amenities available</span>}
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
