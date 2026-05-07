import { useEffect, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import api from '../../api/axios';
import { listFromResponse, money, permissionMessage } from './managerUtils';
import styles from './ManagerPages.module.css';

const emptyForm = {
  name: '',
  nameEn: '',
  description: '',
  descriptionEn: '',
  basePrice: '',
  capacity: '',
  maxAdults: '',
  maxChildren: '0',
  totalUnits: '',
  bedType: 'QUEEN',
  bedCount: '1',
  status: 'ACTIVE',
};

const extractHotelId = (data) => (
  data?.id ||
  data?.hotelId ||
  data?.managedHotelId ||
  data?.managedHotels?.[0]?.id ||
  data?.hotels?.[0]?.id ||
  listFromResponse(data)?.[0]?.id ||
  null
);

const getManagerHotel = async () => {
  try {
    const response = await api.get('/api/manager/my-hotel');
    return response.data;
  } catch (firstError) {
    if (firstError?.response?.status && firstError.response.status !== 404) throw firstError;
  }

  try {
    const response = await api.get('/api/manager/hotels');
    return response.data;
  } catch (secondError) {
    if (secondError?.response?.status && secondError.response.status !== 404) throw secondError;
  }

  const response = await api.get('/api/users/me');
  return response.data;
};

const toNumber = (value, fallback = 0) => {
  const number = Number(value);
  return Number.isFinite(number) ? number : fallback;
};

const ManagerRoomTypes = () => {
  const queryClient = useQueryClient();
  const [hotelId, setHotelId] = useState(null);
  const [formOpen, setFormOpen] = useState(false);
  const [editingRoom, setEditingRoom] = useState(null);

  const { data: managerHotelData, isLoading: isLoadingHotel, isError: isHotelError, error: hotelError } = useQuery({
    queryKey: ['manager-room-types-hotel'],
    queryFn: getManagerHotel,
    staleTime: 5 * 60 * 1000,
    retry: 1,
  });

  useEffect(() => {
    const nextHotelId = extractHotelId(managerHotelData);
    if (nextHotelId) setHotelId(nextHotelId);
  }, [managerHotelData]);

  const { data: amenities = [], isLoading: isLoadingAmenities } = useQuery({
    queryKey: ['manager-room-type-amenities'],
    queryFn: () => api.get('/api/amenities')
      .then((response) => {
        console.log('[ManagerRoomTypes] amenities response:', response.data);
        const list = Array.isArray(response.data) ? response.data : (response.data?.content ?? []);
        return list.filter((amenity) => amenity.active !== false);
      }),
    staleTime: 60000,
  });

  const { data = [], isLoading, isError, error, refetch } = useQuery({
    queryKey: ['manager-room-types', hotelId],
    queryFn: () => api.get(`/api/hotels/${hotelId}/room-types`).then((response) => listFromResponse(response.data)),
    enabled: !!hotelId,
    staleTime: 60000,
  });

  const saveMutation = useMutation({
    mutationFn: (values) => {
      if (!hotelId) throw new Error('Missing hotel ID');
      console.log('Saving to hotel:', hotelId, 'payload:', values);
      return editingRoom
        ? api.put(`/api/hotels/${hotelId}/room-types/${editingRoom.id}`, values, {
          headers: { 'Content-Type': 'application/json' },
        })
        : api.post(`/api/hotels/${hotelId}/room-types`, values, {
          headers: { 'Content-Type': 'application/json' },
        });
    },
    onSuccess: () => {
      toast.success(editingRoom ? 'Room type saved' : 'Room type added successfully');
      setFormOpen(false);
      setEditingRoom(null);
      queryClient.invalidateQueries({ queryKey: ['manager-room-types', hotelId] });
      refetch();
    },
    onError: (err) => {
      console.error('Save failed:', err.response?.data || err);
      toast.error(permissionMessage(err));
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (roomTypeId) => api.delete(`/api/hotels/${hotelId}/room-types/${roomTypeId}`),
    onSuccess: () => refetch(),
    onError: (err) => toast.error(permissionMessage(err)),
  });

  if (isLoadingHotel) return <div className={styles.loading}>Loading...</div>;
  if (isHotelError) return <div className={styles.empty}>{permissionMessage(hotelError)}</div>;
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
          const formData = new FormData(event.currentTarget);
          const amenityIds = formData.getAll('amenityIds').map((id) => Number(id));
          const maxAdults = toNumber(formData.get('maxAdults'), 1);
          const maxChildren = toNumber(formData.get('maxChildren'), 0);
          const capacity = toNumber(formData.get('capacity'), maxAdults + maxChildren);
          const values = {
            hotelId,
            name: formData.get('name')?.trim(),
            description: formData.get('description')?.trim(),
            basePrice: toNumber(formData.get('basePrice')),
            maxAdults,
            maxChildren,
            totalUnits: toNumber(formData.get('totalUnits')),
            bedType: formData.get('bedType'),
            bedCount: toNumber(formData.get('bedCount'), 1),
            status: formData.get('status'),
            amenityIds,
            capacity,
          };
          console.log('Full payload:', JSON.stringify(values, null, 2));
          saveMutation.mutate(values);
        }}>
          <label>Name AR<input name="name" defaultValue={editingRoom?.name || emptyForm.name} required /></label>
          <label>Name EN<input name="nameEn" defaultValue={editingRoom?.nameEn || emptyForm.nameEn} required /></label>
          <label className={styles.wide}>Description AR<textarea name="description" rows="2" defaultValue={editingRoom?.description || emptyForm.description} required /></label>
          <label className={styles.wide}>Description EN<textarea name="descriptionEn" rows="2" defaultValue={editingRoom?.descriptionEn || emptyForm.descriptionEn} required /></label>
          <label>Price per night<input name="basePrice" type="number" min="0" step="0.01" defaultValue={editingRoom?.basePrice || emptyForm.basePrice} required /></label>
          <label>Capacity<input name="capacity" type="number" min="1" defaultValue={editingRoom?.capacity || editingRoom?.maxGuests || emptyForm.capacity} /></label>
          <label>Max Adults<input name="maxAdults" type="number" min="1" defaultValue={editingRoom?.maxAdults || emptyForm.maxAdults} /></label>
          <label>Max Children<input name="maxChildren" type="number" min="0" defaultValue={editingRoom?.maxChildren ?? emptyForm.maxChildren} /></label>
          <label>Total Units<input name="totalUnits" type="number" min="0" defaultValue={editingRoom?.totalUnits || editingRoom?.numberOfRooms || emptyForm.totalUnits} required /></label>
          <label>Bed Type
            <select name="bedType" defaultValue={editingRoom?.bedType || emptyForm.bedType}>
              <option value="KING">KING</option>
              <option value="QUEEN">QUEEN</option>
              <option value="TWIN">TWIN</option>
              <option value="SINGLE">SINGLE</option>
            </select>
          </label>
          <label>Bed Count<input name="bedCount" type="number" min="1" defaultValue={editingRoom?.bedCount || emptyForm.bedCount} /></label>
          <label>Status
            <select name="status" defaultValue={editingRoom?.status || emptyForm.status}>
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
            </select>
          </label>
          <label>Photos<input type="file" multiple /></label>
          <fieldset className={styles.wide}>
            <legend>Amenities</legend>
            {isLoadingAmenities ? <span>Loading amenities...</span> : amenities.map((amenity) => {
              const selectedIds = (editingRoom?.amenities || []).map((item) => item.id);
              return (
                <label key={amenity.id}>
                  <input
                    type="checkbox"
                    name="amenityIds"
                    value={amenity.id}
                    defaultChecked={selectedIds.includes(amenity.id)}
                  />
                  {amenity.nameEn || amenity.name}
                </label>
              );
            })}
            {!isLoadingAmenities && amenities.length === 0 && <span>No amenities available</span>}
          </fieldset>
          <button type="submit" className={styles.button} disabled={saveMutation.isPending}>Save Room Type</button>
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
