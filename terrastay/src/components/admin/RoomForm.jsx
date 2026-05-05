import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import Input from '../ui/Input';
import Select from '../ui/Select';
import Button from '../ui/Button';
import styles from './HotelForm.module.css';

const schema = z.object({
  type: z.string().min(3, 'Room type name required'),
  bedType: z.string().min(2, 'Bed type required'),
  capacity: z.coerce.number().min(1).max(10),
  pricePerNight: z.coerce.number().min(1, 'Price is required'),
});

const bedTypes = [
  { value: 'Single', label: 'Single Bed' },
  { value: 'Double', label: 'Double Bed' },
  { value: 'Queen', label: 'Queen Bed' },
  { value: 'King', label: 'King Bed' },
  { value: 'Twin', label: 'Twin Beds' },
  { value: 'Two Queens', label: 'Two Queen Beds' },
  { value: 'Various', label: 'Various' },
];

const RoomForm = ({ room, onSubmit, loading }) => {
  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: room || { capacity: 2 },
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className={styles.form}>
      <Input label="Room Type Name" error={errors.type?.message} {...register('type')} placeholder="e.g. Deluxe Suite" />
      <div className={styles.row}>
        <Select label="Bed Type" options={bedTypes} error={errors.bedType?.message} placeholder="Select bed type" {...register('bedType')} />
        <Input label="Max Capacity" type="number" min={1} max={10} error={errors.capacity?.message} {...register('capacity')} />
      </div>
      <Input label="Price per Night (USD)" type="number" min={1} error={errors.pricePerNight?.message} {...register('pricePerNight')} placeholder="e.g. 150" />
      <div className={styles.actions}>
        <Button type="submit" variant="primary" loading={loading}>
          {room ? 'Update Room' : 'Add Room Type'}
        </Button>
      </div>
    </form>
  );
};

export default RoomForm;
