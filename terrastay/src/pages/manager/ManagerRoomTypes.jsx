import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import api from '../../api/axios';
import useAuth from '../../hooks/useAuth';
import { getManagedHotelId, money, normalizePage, withMock } from './managerUtils';
import styles from './ManagerPages.module.css';

const mockRooms = [{ id: 1, name: 'Deluxe Room', nameEn: 'Deluxe Room', basePrice: 160, capacity: 2, totalUnits: 8 }];

const ManagerRoomTypes = () => {
  const { user } = useAuth();
  const qc = useQueryClient();
  const [formOpen, setFormOpen] = useState(false);
  const query = useQuery({
    queryKey: ['manager', 'rooms', user?.id],
    queryFn: async () => {
      const hotelId = await getManagedHotelId(user);
      try {
        const response = await api.get(`/api/hotels/${hotelId}/room-types`);
        return { ...normalizePage(response.data), hotelId };
      } catch {
        // TODO: replace with real API endpoint.
        return { ...withMock(mockRooms), hotelId };
      }
    },
    staleTime: 60000,
  });
  const saveMutation = useMutation({
    mutationFn: (values) => api.post(`/api/hotels/${query.data?.hotelId}/room-types`, values),
    onSuccess: () => { toast.success('Room type saved'); qc.invalidateQueries({ queryKey: ['manager', 'rooms'] }); setFormOpen(false); },
  });
  const deleteMutation = useMutation({ mutationFn: (id) => api.delete(`/api/hotels/${query.data?.hotelId}/room-types/${id}`), onSettled: () => qc.invalidateQueries({ queryKey: ['manager', 'rooms'] }) });

  return (
    <section className={styles.page}>
      <header className={styles.header}><div><h1>Room Types</h1><p>Manage rooms for your hotel only.</p></div>{query.data?.isMock && <span className={styles.mock}>Mock data</span>}</header>
      <button className={styles.button} onClick={() => setFormOpen((value) => !value)}>Add new room type</button>
      {formOpen && <form className={styles.form} onSubmit={(event) => { event.preventDefault(); saveMutation.mutate(Object.fromEntries(new FormData(event.currentTarget))); }}>
        <label>Name AR<input name="name" required /></label><label>Name EN<input name="nameEn" required /></label>
        <label className={styles.wide}>Description AR<textarea name="description" rows="2" /></label><label className={styles.wide}>Description EN<textarea name="descriptionEn" rows="2" /></label>
        <label>Price per night<input name="basePrice" type="number" /></label><label>Max guests<input name="capacity" type="number" /></label><label>Number of rooms<input name="totalUnits" type="number" /></label><label>Photos<input type="file" multiple /></label>
        <label className={styles.wide}>Amenities<textarea name="amenitiesText" rows="2" /></label><button className={styles.button}>Save Room Type</button>
      </form>}
      <div className={styles.panel}><table className={styles.table}><thead><tr><th>Name</th><th>Price</th><th>Max Guests</th><th>Rooms</th><th>Actions</th></tr></thead><tbody>{(query.data?.items || []).map((room) => <tr key={room.id}><td>{room.nameEn || room.name}</td><td>{money(room.basePrice)}</td><td>{room.capacity || room.maxGuests}</td><td>{room.totalUnits || room.numberOfRooms}</td><td className={styles.actions}><button className={styles.secondary} onClick={() => setFormOpen(true)}>Edit</button><button className={styles.danger} onClick={() => deleteMutation.mutate(room.id)}>Delete</button></td></tr>)}</tbody></table></div>
    </section>
  );
};

export default ManagerRoomTypes;
