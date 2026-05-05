import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Edit, Trash2, ChevronLeft } from 'lucide-react';
import toast from 'react-hot-toast';
import { getHotelById, createRoom, updateRoom, deleteRoom } from '../../api/hotels';
import RoomForm from '../../components/admin/RoomForm';
import Modal from '../../components/ui/Modal';
import Button from '../../components/ui/Button';
import Spinner from '../../components/ui/Spinner';
import { formatPrice } from '../../utils/formatPrice';
import styles from './AdminDashboard.module.css';
import tableStyles from './ManageHotels.module.css';

const ManageRooms = () => {
  const { id: hotelId } = useParams();
  const queryClient = useQueryClient();
  const [modalOpen, setModalOpen] = useState(false);
  const [editRoom, setEditRoom] = useState(null);

  const { data: hotel, isLoading } = useQuery({
    queryKey: ['hotel', hotelId],
    queryFn: () => getHotelById(hotelId),
  });

  const createMutation = useMutation({
    mutationFn: (data) => createRoom(hotelId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['hotel', hotelId] });
      toast.success('Room type added');
      setModalOpen(false);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, ...data }) => updateRoom(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['hotel', hotelId] });
      toast.success('Room updated');
      setModalOpen(false);
      setEditRoom(null);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteRoom,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['hotel', hotelId] });
      toast.success('Room deleted');
    },
  });

  const handleSubmit = (data) => {
    if (editRoom) {
      updateMutation.mutate({ id: editRoom.id, ...data });
    } else {
      createMutation.mutate(data);
    }
  };

  return (
    <div className={styles.page}>
      <aside className={styles.sidebar}>
        <nav className={styles.nav}>
          <Link to="/admin" className={styles.navLink}>Dashboard</Link>
          <Link to="/admin/hotels" className={`${styles.navLink} ${styles.active}`}>Manage Hotels</Link>
          <Link to="/admin/bookings" className={styles.navLink}>All Bookings</Link>
        </nav>
      </aside>

      <main className={styles.main}>
        <div className={styles.header}>
          <div>
            <Link to="/admin/hotels" className={tableStyles.backLink}>
              <ChevronLeft size={16} /> Back to Hotels
            </Link>
            <h1>{hotel?.name || 'Hotel'} — Rooms</h1>
          </div>
          <Button variant="primary" onClick={() => { setEditRoom(null); setModalOpen(true); }}>
            <Plus size={16} /> Add Room Type
          </Button>
        </div>

        {isLoading ? <Spinner centered /> : (
          <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Room Type</th>
                  <th>Bed Type</th>
                  <th>Capacity</th>
                  <th>Price/Night</th>
                  <th>Available</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {(hotel?.rooms || []).map((room) => (
                  <tr key={room.id}>
                    <td className={styles.hotelCell}>{room.type}</td>
                    <td>{room.bedType}</td>
                    <td>{room.capacity} guests</td>
                    <td className={styles.price}>{formatPrice(room.pricePerNight)}</td>
                    <td>{room.available ? '✓ Yes' : '✗ No'}</td>
                    <td>
                      <div className={tableStyles.actions}>
                        <button className={tableStyles.editBtn} onClick={() => { setEditRoom(room); setModalOpen(true); }}>
                          <Edit size={15} />
                        </button>
                        <button
                          className={tableStyles.deleteBtn}
                          onClick={() => {
                            if (window.confirm('Delete this room type?')) deleteMutation.mutate(room.id);
                          }}
                        >
                          <Trash2 size={15} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </main>

      <Modal
        isOpen={modalOpen}
        onClose={() => { setModalOpen(false); setEditRoom(null); }}
        title={editRoom ? 'Edit Room Type' : 'Add Room Type'}
      >
        <RoomForm
          room={editRoom}
          onSubmit={handleSubmit}
          loading={createMutation.isPending || updateMutation.isPending}
        />
      </Modal>
    </div>
  );
};

export default ManageRooms;
