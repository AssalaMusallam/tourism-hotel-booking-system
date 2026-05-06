import { useEffect } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import api from '../../api/axios';
import useAuth from '../../hooks/useAuth';
import { getManagedHotelId } from './managerUtils';
import styles from './ManagerPages.module.css';

const ManagerHotel = () => {
  const { user } = useAuth();
  const form = useForm();
  const query = useQuery({
    queryKey: ['manager', 'hotel', user?.id],
    queryFn: async () => {
      const hotelId = await getManagedHotelId(user);
      try {
        const response = await api.get(`/api/hotels/${hotelId}`);
        return { ...response.data, hotelId, isMock: false };
      } catch {
        // TODO: replace with real API endpoint.
        return { hotelId, name: 'فندقي', nameEn: 'My Hotel', city: 'Jerusalem', address: '', phoneNumber: '', email: '', rating: 4, images: [], isMock: true };
      }
    },
    staleTime: 60000,
  });
  useEffect(() => { if (query.data) form.reset(query.data); }, [form, query.data]);
  const mutation = useMutation({
    mutationFn: (values) => api.put(`/api/hotels/${query.data?.hotelId || query.data?.id}`, values),
    onSuccess: () => toast.success('Hotel saved'),
    onError: () => toast.error('Could not save hotel'),
  });

  return (
    <section className={styles.page}>
      <header className={styles.header}><div><h1>My Hotel</h1><p>Edit hotel info, photos, and amenities.</p></div>{query.data?.isMock && <span className={styles.mock}>Mock data</span>}</header>
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
          <div className={`${styles.photos} ${styles.wide}`}>{(query.data?.images || []).map((image) => <img key={image.id || image.imageUrl} src={image.imageUrl} alt="" />)}</div>
          <button className={styles.button} type="submit">Save</button>
        </form>
      </div>
    </section>
  );
};

export default ManagerHotel;
