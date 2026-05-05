import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import Input from '../ui/Input';
import Select from '../ui/Select';
import Button from '../ui/Button';
import { useMinimalAmenities } from '../../hooks/useCatalogQueries';
import styles from './HotelForm.module.css';

// BedType enum from backend: KING | QUEEN | TWIN
const BED_TYPES = [
  { value: 'TWIN',     label: 'Twin Beds' },
  { value: 'QUEEN',    label: 'Queen Bed' },
  { value: 'KING',     label: 'King Bed' },
];

// RoomTypeStatus from backend
const STATUSES = [
  { value: 'ACTIVE',   label: 'Active' },
  { value: 'INACTIVE', label: 'Inactive' },
];

const schema = z.object({
  name:        z.string().min(1, 'Name required').max(80),
  capacity:    z.coerce.number().min(1, 'Min 1'),
  bedType:     z.enum(['KING','QUEEN','TWIN'], { message: 'Required' }),
  bedCount:    z.coerce.number().min(1, 'Min 1'),
  maxAdults:   z.coerce.number().min(1, 'Min 1'),
  maxChildren: z.coerce.number().min(0, 'Min 0'),
  basePrice:   z.coerce.number().min(0, 'Min 0'),
  totalUnits:  z.coerce.number().min(0, 'Min 0'),
  description: z.string().max(2000).optional().or(z.literal('')),
  policies:    z.string().max(4000).optional().or(z.literal('')),
  status:      z.enum(['ACTIVE','INACTIVE']).optional(),
  amenityIds:  z.array(z.number()).optional(),
});

const RoomTypeForm = ({ hotelId, room, onSubmit, loading }) => {
  const { data: minimalData } = useMinimalAmenities({ active: true, size: 200 });
  const amenities = minimalData?.content || [];

  const { register, handleSubmit, watch, setValue, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      name:        room?.name || '',
      capacity:    room?.capacity || 2,
      bedType:     room?.bedType || '',
      bedCount:    room?.bedCount || 1,
      maxAdults:   room?.maxAdults || 2,
      maxChildren: room?.maxChildren || 0,
      basePrice:   room?.basePrice || '',
      totalUnits:  room?.totalUnits || 0,
      description: room?.description || '',
      policies:    room?.policies || '',
      status:      room?.status || 'ACTIVE',
      amenityIds:  room?.amenityIds ? [...room.amenityIds] : [],
    },
  });

  const selectedIds = watch('amenityIds') || [];

  const toggleAmenity = (id) => {
    const next = selectedIds.includes(id)
      ? selectedIds.filter((x) => x !== id)
      : [...selectedIds, id];
    setValue('amenityIds', next);
  };

  const handleFormSubmit = (data) => {
    // CRITICAL: hotelId MUST be in the request body per RoomTypeRequestDto @NotNull
    onSubmit({
      ...data,
      hotelId: Number(hotelId),
      basePrice:   String(data.basePrice),
      description: data.description || undefined,
      policies:    data.policies    || undefined,
      amenityIds:  data.amenityIds?.length ? data.amenityIds : undefined,
    });
  };

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} className={styles.form}>
      <div className={styles.row2}>
        <Input label="Room Name *" error={errors.name?.message} {...register('name')} placeholder="Deluxe King Room" />
        <Select label="Status" options={STATUSES} error={errors.status?.message} {...register('status')} />
      </div>
      <div className={styles.row3}>
        <Select label="Bed Type *" options={BED_TYPES} placeholder="Select..." error={errors.bedType?.message} {...register('bedType')} />
        <Input label="Bed Count *" type="number" min={1} error={errors.bedCount?.message} {...register('bedCount')} />
        <Input label="Capacity *" type="number" min={1} error={errors.capacity?.message} {...register('capacity')} />
      </div>
      <div className={styles.row3}>
        <Input label="Max Adults *" type="number" min={1} error={errors.maxAdults?.message} {...register('maxAdults')} />
        <Input label="Max Children *" type="number" min={0} error={errors.maxChildren?.message} {...register('maxChildren')} />
        <Input label="Total Units *" type="number" min={0} error={errors.totalUnits?.message} {...register('totalUnits')} />
      </div>
      <Input label="Base Price / Night (USD) *" type="number" min={0} step="0.01" error={errors.basePrice?.message} {...register('basePrice')} />

      <div className={styles.field}>
        <label className={styles.label}>Description</label>
        <textarea className={styles.textarea} rows={2} {...register('description')} />
      </div>
      <div className={styles.field}>
        <label className={styles.label}>Policies</label>
        <textarea className={styles.textarea} rows={2} {...register('policies')} />
      </div>

      <h4 className={styles.section}>Amenities</h4>
      <div className={styles.amenityGrid}>
        {amenities.map((a) => (
          <label key={a.id} className={styles.amenityCheck}>
            <input type="checkbox" checked={selectedIds.includes(a.id)} onChange={() => toggleAmenity(a.id)} />
            <span>{a.name}</span>
          </label>
        ))}
        {amenities.length === 0 && <span className={styles.noAmenities}>No amenities available</span>}
      </div>

      <div className={styles.actions}>
        <Button type="submit" variant="primary" loading={loading}>
          {room ? 'Update Room' : 'Add Room Type'}
        </Button>
      </div>
    </form>
  );
};
export default RoomTypeForm;
