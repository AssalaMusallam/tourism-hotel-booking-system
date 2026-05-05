import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Edit, Trash2, BedDouble } from 'lucide-react';
import toast from 'react-hot-toast';
import { getHotels, createHotel, updateHotel, deleteHotel } from '../../api/hotels';
import HotelForm from '../../components/admin/HotelForm';
import Modal from '../../components/ui/Modal';
import Button from '../../components/ui/Button';
import Spinner from '../../components/ui/Spinner';
import styles from './AdminDashboard.module.css';
import tableStyles from './ManageHotels.module.css';

const ManageHotels = () => {
  const queryClient = useQueryClient();
  const [modalOpen, setModalOpen] = useState(false);
  const [editHotel, setEditHotel] = useState(null);

  const { data, isLoading } = useQuery({
    queryKey: ['admin-hotels'],
    queryFn: () => getHotels({ limit: 100 }),
  });

  const hotels = data?.data || [];

  const createMutation = useMutation({
    mutationFn: createHotel,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-hotels'] });
      toast.success('Hotel created successfully');
      setModalOpen(false);
    },
    onError: () => toast.error('Failed to create hotel'),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, ...data }) => updateHotel(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-hotels'] });
      toast.success('Hotel updated successfully');
      setModalOpen(false);
      setEditHotel(null);
    },
    onError: () => toast.error('Failed to update hotel'),
  });

  const deleteMutation = useMutation({
    mutationFn: deleteHotel,
    onMutate: async (id) => {
      await queryClient.cancelQueries({ queryKey: ['admin-hotels'] });
      const prev = queryClient.getQueryData(['admin-hotels']);
      queryClient.setQueryData(['admin-hotels'], (old) => ({
        ...old,
        data: old?.data?.filter((h) => h.id !== id),
      }));
      return { prev };
    },
    onError: (_, __, ctx) => {
      queryClient.setQueryData(['admin-hotels'], ctx.prev);
      toast.error('Failed to delete hotel');
    },
    onSettled: () => queryClient.invalidateQueries({ queryKey: ['admin-hotels'] }),
    onSuccess: () => toast.success('Hotel deleted'),
  });

  const handleSubmit = (data) => {
    if (editHotel) {
      updateMutation.mutate({ id: editHotel.id, ...data });
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
          <h1>Manage Hotels</h1>
          <Button variant="primary" onClick={() => { setEditHotel(null); setModalOpen(true); }}>
            <Plus size={16} /> Add Hotel
          </Button>
        </div>

        {isLoading ? <Spinner centered /> : (
          <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Hotel Name</th>
                  <th>City</th>
                  <th>Stars</th>
                  <th>Rooms</th>
                  <th>Price/Night</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {hotels.map((hotel) => (
                  <tr key={hotel.id} className={tableStyles.row}>
                    <td className={styles.hotelCell}>{hotel.name}</td>
                    <td>{hotel.city}</td>
                    <td>{'★'.repeat(hotel.stars)}</td>
                    <td>{hotel.rooms?.length || 0}</td>
                    <td className={styles.price}>${hotel.pricePerNight}</td>
                    <td>
                      <div className={tableStyles.actions}>
                        <Link to={`/admin/hotels/${hotel.id}/rooms`} className={tableStyles.actionLink} title="Manage Rooms">
                          <BedDouble size={15} />
                        </Link>
                        <button
                          className={tableStyles.editBtn}
                          onClick={() => { setEditHotel(hotel); setModalOpen(true); }}
                          title="Edit"
                        >
                          <Edit size={15} />
                        </button>
                        <button
                          className={tableStyles.deleteBtn}
                          onClick={() => {
                            if (window.confirm(`Delete ${hotel.name}?`)) {
                              deleteMutation.mutate(hotel.id);
                            }
                          }}
                          title="Delete"
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
        onClose={() => { setModalOpen(false); setEditHotel(null); }}
        title={editHotel ? 'Edit Hotel' : 'Add New Hotel'}
        size="lg"
      >
        <HotelForm
          hotel={editHotel}
          onSubmit={handleSubmit}
          loading={createMutation.isPending || updateMutation.isPending}
        />
      </Modal>
    </div>
  );
};

export default ManageHotels;
