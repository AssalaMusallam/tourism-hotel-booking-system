import { useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import api from '../../api/axios';
import { useManagerHotel } from '../../hooks/useManagerHotel';
import { listFromResponse, money, permissionMessage } from './managerUtils';
import styles from './ManagerPages.module.css';

const emptyForm = { name: '', nameEn: '', description: '', descriptionEn: '', basePrice: '', capacity: '', totalUnits: '' };

const ManagerRoomTypes = () => {
  const { hotelId, isLoadingHotelId } = useManagerHotel();
  const [formOpen, setFormOpen] = useState(false);
  const [editingRoom, setEditingRoom] = useState(null);

  const { data = [], isLoading, isError, error, refetch } = useQuery({
    queryKey: ['manager-room-types', hotelId],
    queryFn: () => api.get(`/api/hotels/${hotelId}/room-types`).then((response) => listFromResponse(response.data)),
    enabled: !!hotelId,
    staleTime: 60000,
  });

  const saveMutation = useMutation({
    mutationFn: (values) => editingRoom
      ? api.put(`/api/hotels/${hotelId}/room-types/${editingRoom.id}`, values)
      : api.post(`/api/hotels/${hotelId}/room-types`, values),
    onSuccess: () => {
      toast.success('Room type saved');
      setFormOpen(false);
      setEditingRoom(null);
      refetch();
    },
    onError: (err) => toast.error(permissionMessage(err)),
  });

  const deleteMutation = useMutation({
    mutationFn: (roomTypeId) => api.delete(`/api/hotels/${hotelId}/room-types/${roomTypeId}`),
    onSuccess: () => refetch(),
    onError: (err) => toast.error(permissionMessage(err)),
  });

  if (isLoadingHotelId) return <div className={styles.loading}>Loading...</div>;
  if (!hotelId) return <div className={styles.loading}>Connecting to hotel...</div>;
  if (isLoading) return <div className={styles.loading}>Loading...</div>;
  if (isError) return <div className={styles.empty}>{permissionMessage(error)}</div>;

  return (
    <section className={styles.page}>
      <header className={styles.header}><div><h1>Room Types</h1><p>Manage rooms for your hotel only.</p></div></header>
      <button className={styles.button} onClick={() => { setEditingRoom(null); setFormOpen((value) => !value); }}>Add new room type</button>
      {formOpen && (
        <form className={styles.form} onSubmit={(event) => {
          event.preventDefault();
          const values = Object.fromEntries(new FormData(event.currentTarget));
          saveMutation.mutate(values);
        }}>
          <label>Name AR<input name="name" defaultValue={editingRoom?.name || emptyForm.name} required /></label>
          <label>Name EN<input name="nameEn" defaultValue={editingRoom?.nameEn || emptyForm.nameEn} required /></label>
          <label className={styles.wide}>Description AR<textarea name="description" rows="2" defaultValue={editingRoom?.description || emptyForm.description} /></label>
          <label className={styles.wide}>Description EN<textarea name="descriptionEn" rows="2" defaultValue={editingRoom?.descriptionEn || emptyForm.descriptionEn} /></label>
          <label>Price per night<input name="basePrice" type="number" defaultValue={editingRoom?.basePrice || emptyForm.basePrice} /></label>
          <label>Max guests<input name="capacity" type="number" defaultValue={editingRoom?.capacity || editingRoom?.maxGuests || emptyForm.capacity} /></label>
          <label>Number of rooms<input name="totalUnits" type="number" defaultValue={editingRoom?.totalUnits || editingRoom?.numberOfRooms || emptyForm.totalUnits} /></label>
          <label>Photos<input type="file" multiple /></label>
          <label className={styles.wide}>Amenities<textarea name="amenitiesText" rows="2" /></label>
          <button className={styles.button} disabled={saveMutation.isPending}>Save Room Type</button>
        </form>
      )}
      <div className={styles.panel}>
        {data.length === 0 ? <div className={styles.empty}>No data found</div> : (
          <table className={styles.table}>
            <thead><tr><th>Name</th><th>Price</th><th>Max Guests</th><th>Rooms</th><th>Actions</th></tr></thead>
            <tbody>{data.map((room) => (
              <tr key={room.id}>
                <td>{room.nameEn || room.name}</td>
                <td>{money(room.basePrice)}</td>
                <td>{room.capacity || room.maxGuests}</td>
                <td>{room.totalUnits || room.numberOfRooms}</td>
                <td className={styles.actions}>
                  <button className={styles.secondary} onClick={() => { setEditingRoom(room); setFormOpen(true); }}>Edit</button>
                  <button className={styles.danger} onClick={() => deleteMutation.mutate(room.id)}>Delete</button>
                </td>
              </tr>
            ))}</tbody>
          </table>
        )}
      </div>
    </section>
  );
};

export default ManagerRoomTypes;
