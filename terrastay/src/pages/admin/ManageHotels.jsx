import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Plus, Edit, Trash2, BedDouble, Eye, Image as ImageIcon, Search } from 'lucide-react';
import toast from 'react-hot-toast';
import {
  useAdminHotels, useCreateHotel, useUpdateHotel, useDeleteHotel,
} from '../../hooks/useCatalogQueries';
import HotelForm from '../../components/hotel/HotelForm';
import Modal from '../../components/ui/Modal';
import Button from '../../components/ui/Button';
import Spinner from '../../components/ui/Spinner';
import Pagination from '../../components/ui/Pagination';
import Badge, { StatusBadge } from '../../components/ui/Badge';
import StarRating from '../../components/ui/StarRating';
import EmptyState from '../../components/ui/EmptyState';
import Input from '../../components/ui/Input';
import styles from './ManageHotels.module.css';

const ManageHotels = () => {
  const navigate = useNavigate();
  const [modalOpen, setModalOpen] = useState(false);
  const [editHotel, setEditHotel] = useState(null);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const params = {
    page,
    size: 15,
    ...(search && { q: search }),
    ...(statusFilter && { status: statusFilter }),
  };

  const { data, isLoading } = useAdminHotels(params);
  const hotels = data?.content || [];
  const totalPages = data?.totalPages || 0;

  const createMutation = useCreateHotel();
  const updateMutation = useUpdateHotel();
  const deleteMutation = useDeleteHotel();

  const handleSubmit = (formData) => {
    if (editHotel) {
      updateMutation.mutate({ id: editHotel.id, data: formData }, {
        onSuccess: () => {
          toast.success('Hotel updated successfully');
          setModalOpen(false);
          setEditHotel(null);
        },
        onError: (err) => toast.error(err.response?.data?.message || 'Failed to update hotel'),
      });
    } else {
      createMutation.mutate(formData, {
        onSuccess: () => {
          toast.success('Hotel created successfully');
          setModalOpen(false);
        },
        onError: (err) => toast.error(err.response?.data?.message || 'Failed to create hotel'),
      });
    }
  };

  const handleDelete = (hotel) => {
    if (window.confirm(`Delete "${hotel.name}"? This cannot be undone.`)) {
      deleteMutation.mutate(hotel.id, {
        onSuccess: () => toast.success('Hotel deleted'),
        onError: () => toast.error('Failed to delete hotel'),
      });
    }
  };

  return (
    <div className={styles.page}>
      <aside className={styles.sidebar}>
        <nav className={styles.nav}>
          <Link to="/admin" className={styles.navLink}>Dashboard</Link>
          <Link to="/admin/hotels" className={`${styles.navLink} ${styles.active}`}>Manage Hotels</Link>
          <Link to="/admin/amenities" className={styles.navLink}>Manage Amenities</Link>
        </nav>
      </aside>

      <main className={styles.main}>
        <div className={styles.header}>
          <h1>Manage Hotels</h1>
          <Button variant="primary" icon={Plus} onClick={() => { setEditHotel(null); setModalOpen(true); }}>
            Add Hotel
          </Button>
        </div>

        {/* Filters */}
        <div className={styles.filters}>
          <div className={styles.searchBox}>
            <Search size={16} />
            <input
              type="text"
              placeholder="Search hotels..."
              value={search}
              onChange={(e) => { setSearch(e.target.value); setPage(0); }}
              className={styles.searchInput}
            />
          </div>
          <select
            value={statusFilter}
            onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
            className={styles.statusSelect}
          >
            <option value="">All Statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
          </select>
        </div>

        {isLoading ? <Spinner centered /> : hotels.length === 0 ? (
          <EmptyState
            title="No hotels found"
            description="Create your first hotel to get started."
            action={{ label: 'Add Hotel', onClick: () => setModalOpen(true) }}
          />
        ) : (
          <>
            <div className={styles.tableWrap}>
              <table className={styles.table}>
                <thead>
                  <tr>
                    <th>Hotel Name</th>
                    <th>City</th>
                    <th>Rating</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {hotels.map((hotel) => (
                    <tr key={hotel.id} className={styles.row}>
                      <td className={styles.nameCell}>
                        <span className={styles.hotelNameText}>{hotel.name}</span>
                      </td>
                      <td>{hotel.city}, {hotel.country}</td>
                      <td>
                        {hotel.rating != null ? (
                          <StarRating value={hotel.rating} size={14} />
                        ) : '—'}
                      </td>
                      <td><StatusBadge status={hotel.status} /></td>
                      <td>
                        <div className={styles.actions}>
                          <button
                            className={styles.actionBtn}
                            onClick={() => navigate(`/hotels/${hotel.id}`)}
                            title="View"
                          >
                            <Eye size={15} />
                          </button>
                          <button
                            className={styles.actionBtn}
                            onClick={() => { setEditHotel(hotel); setModalOpen(true); }}
                            title="Edit"
                          >
                            <Edit size={15} />
                          </button>
                          <Link
                            to={`/admin/hotels/${hotel.id}/rooms`}
                            className={styles.actionBtn}
                            title="Manage Rooms"
                          >
                            <BedDouble size={15} />
                          </Link>
                          <button
                            className={`${styles.actionBtn} ${styles.deleteBtn}`}
                            onClick={() => handleDelete(hotel)}
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
            <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
          </>
        )}
      </main>

      <Modal
        isOpen={modalOpen}
        onClose={() => { setModalOpen(false); setEditHotel(null); }}
        title={editHotel ? 'Edit Hotel' : 'Add New Hotel'}
        size="xl"
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
