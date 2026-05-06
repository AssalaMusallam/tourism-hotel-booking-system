import { useEffect } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import api from '../../api/axios';
import { useManagerHotel } from '../../hooks/useManagerHotel';
import { permissionMessage } from './managerUtils';
import styles from './ManagerPages.module.css';

const ManagerHotel = () => {
  const form = useForm();
  const { hotelId, isLoadingHotelId } = useManagerHotel();

  const { data: hotel, isLoading, isError, error } = useQuery({
    queryKey: ['manager-hotel-detail', hotelId],
    queryFn: () => api.get(`/api/hotels/${hotelId}`).then((response) => response.data),
    enabled: !!hotelId,
    staleTime: 60000,
  });

  useEffect(() => {
    if (hotel) {
      form.reset({
        name: hotel.name || '',
        nameEn: hotel.nameEn || '',
        description: hotel.description || hotel.descriptionAr || '',
        descriptionEn: hotel.descriptionEn || '',
        city: hotel.city || '',
        address: hotel.address || '',
        phoneNumber: hotel.phoneNumber || hotel.phone || '',
        email: hotel.email || '',
        rating: hotel.rating || hotel.starRating || hotel.stars || 3,
      });
    }
  }, [form, hotel]);

  const mutation = useMutation({
    mutationFn: (values) => api.put(`/api/admin/hotels/${hotelId}`, values),
    onSuccess: () => toast.success('Hotel saved'),
    onError: (err) => {
      console.error('Save failed:', err.response?.data);
      toast.error(permissionMessage(err));
    },
  });

  if (isLoadingHotelId) return <div className={styles.loading}>Loading...</div>;
  if (!hotelId) return <div className={styles.loading}>Connecting to hotel...</div>;
  if (isLoading) return <div className={styles.loading}>Loading...</div>;
  if (isError) return <div className={styles.empty}>{permissionMessage(error)}</div>;

  return (
    <section className={styles.page}>
      <header className={styles.header}><div><h1>My Hotel</h1><p>Edit hotel info, photos, and amenities.</p></div></header>
      <div className={styles.panel}>
        <form className={styles.form} onSubmit={form.handleSubmit((values) => mutation.mutate(values))}>
          <label>Name AR<input {...form.register('name')} /></label>
          <label>Name EN<input {...form.register('nameEn')} /></label>
          <label className={styles.wide}>Description AR<textarea rows="3" {...form.register('description')} /></label>
          <label className={styles.wide}>Description EN<textarea rows="3" {...form.register('descriptionEn')} /></label>
          <label>City<input {...form.register('city')} /></label>
          <label>Address<input {...form.register('address')} /></label>
          <label>Phone<input {...form.register('phoneNumber')} /></label>
          <label>Email<input {...form.register('email')} /></label>
          <label>Star Rating<input type="number" min="1" max="5" step="0.1" {...form.register('rating')} /></label>
          <label>Upload photos<input type="file" multiple /></label>
          <label className={styles.wide}>Amenities<textarea rows="2" placeholder="Comma-separated amenity names" {...form.register('amenitiesText')} /></label>
          <div className={`${styles.photos} ${styles.wide}`}>{(hotel?.images || []).map((image) => <img key={image.id || image.imageUrl} src={image.imageUrl} alt="" />)}</div>
          <button className={styles.button} type="submit" disabled={mutation.isPending}>Save</button>
        </form>
      </div>
    </section>
  );
};

export default ManagerHotel;
