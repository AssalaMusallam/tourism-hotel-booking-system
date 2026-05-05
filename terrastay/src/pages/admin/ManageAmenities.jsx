import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Plus, Edit, Trash2, ToggleLeft, ToggleRight, RotateCcw, Search } from 'lucide-react';
import toast from 'react-hot-toast';
import {
  useAmenities, useCreateAmenity, useUpdateAmenity,
  useDeleteAmenity, useToggleAmenityStatus,
} from '../../hooks/useCatalogQueries';
import { restoreAmenity } from '../../api/amenitiesApi';
import { useQueryClient } from '@tanstack/react-query';
import AmenityForm from '../../components/amenity/AmenityForm';
import Modal from '../../components/ui/Modal';
import Button from '../../components/ui/Button';
import Spinner from '../../components/ui/Spinner';
import Pagination from '../../components/ui/Pagination';
import Badge from '../../components/ui/Badge';
import EmptyState from '../../components/ui/EmptyState';
import styles from './ManageHotels.module.css';

const CATEGORY_LABELS = {
  CONNECTIVITY: 'Connectivity',
  WELLNESS: 'Wellness & Health',
  ENTERTAINMENT: 'Entertainment',
  COMFORT: 'Comfort',
  DINING: 'Dining',
  PARKING: 'Parking',
  SECURITY: 'Security',
  CLEANING: 'Cleaning',
  ACCESSIBILITY: 'Accessibility',
  OUTDOOR: 'Outdoor',
};

const ManageAmenities = () => {
  const queryClient = useQueryClient();
  const [modalOpen, setModalOpen] = useState(false);
  const [editAmenity, setEditAmenity] = useState(null);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [activeFilter, setActiveFilter] = useState('');

  const params = {
    page,
    size: 20,
    ...(search && { q: search }),
    ...(categoryFilter && { category: categoryFilter }),
    ...(activeFilter !== '' && { active: activeFilter === 'true' }),
  };

  const { data, isLoading } = useAmenities(params);
  const amenities = data?.content || [];
  const totalPages = data?.totalPages || 0;

  const createMutation = useCreateAmenity();
  const updateMutation = useUpdateAmenity();
  const deleteMutation = useDeleteAmenity();
  const toggleMutation = useToggleAmenityStatus();

  const handleSubmit = (formData) => {
    if (editAmenity) {
      updateMutation.mutate({ id: editAmenity.id, data: formData }, {
        onSuccess: () => {
          toast.success('Amenity updated');
          setModalOpen(false);
          setEditAmenity(null);
        },
        onError: (e) => toast.error(e.response?.data?.message || 'Failed to update'),
      });
    } else {
      createMutation.mutate(formData, {
        onSuccess: () => {
          toast.success('Amenity created');
          setModalOpen(false);
        },
        onError: (e) => toast.error(e.response?.data?.message || 'Failed to create'),
      });
    }
  };

  const handleToggleActive = (amenity) => {
    toggleMutation.mutate({ id: amenity.id, active: !amenity.active }, {
      onSuccess: () => toast.success(`Amenity ${amenity.active ? 'deactivated' : 'activated'}`),
      onError: () => toast.error('Failed to change status'),
    });
  };

  const handleSoftDelete = (amenity) => {
    if (window.confirm(`Soft delete "${amenity.name}"? This will deactivate it.`)) {
      deleteMutation.mutate(amenity.id, {
        onSuccess: () => toast.success('Amenity soft deleted'),
        onError: () => toast.error('Failed to delete'),
      });
    }
  };

  const handleRestore = async (amenity) => {
    try {
      await restoreAmenity(amenity.id);
      queryClient.invalidateQueries({ queryKey: ['amenities'] });
      toast.success('Amenity restored');
    } catch {
      toast.error('Failed to restore');
    }
  };

  return (
    <div className={styles.page}>
      <aside className={styles.sidebar}>
        <nav className={styles.nav}>
          <Link to="/admin" className={styles.navLink}>Dashboard</Link>
          <Link to="/admin/hotels" className={styles.navLink}>Manage Hotels</Link>
          <Link to="/admin/amenities" className={`${styles.navLink} ${styles.active}`}>Manage Amenities</Link>
        </nav>
      </aside>

      <main className={styles.main}>
        <div className={styles.header}>
          <h1>Manage Amenities</h1>
          <Button variant="primary" icon={Plus} onClick={() => { setEditAmenity(null); setModalOpen(true); }}>
            Add Amenity
          </Button>
        </div>

        {/* Filters */}
        <div className={styles.filters}>
          <div className={styles.searchBox}>
            <Search size={16} />
            <input
              type="text"
              placeholder="Search amenities..."
              value={search}
              onChange={(e) => { setSearch(e.target.value); setPage(0); }}
              className={styles.searchInput}
            />
          </div>
          <select
            value={categoryFilter}
            onChange={(e) => { setCategoryFilter(e.target.value); setPage(0); }}
            className={styles.statusSelect}
          >
            <option value="">All Categories</option>
            {Object.entries(CATEGORY_LABELS).map(([val, label]) => (
              <option key={val} value={val}>{label}</option>
            ))}
          </select>
          <select
            value={activeFilter}
            onChange={(e) => { setActiveFilter(e.target.value); setPage(0); }}
            className={styles.statusSelect}
          >
            <option value="">All Status</option>
            <option value="true">Active</option>
            <option value="false">Inactive</option>
          </select>
        </div>

        {isLoading ? <Spinner centered /> : amenities.length === 0 ? (
          <EmptyState
            title="No amenities found"
            description="Create amenities to assign to hotels and rooms."
            action={{ label: 'Add Amenity', onClick: () => setModalOpen(true) }}
          />
        ) : (
          <>
            <div className={styles.tableWrap}>
              <table className={styles.table}>
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Category</th>
                    <th>Premium</th>
                    <th>Active</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {amenities.map((amenity) => (
                    <tr key={amenity.id} className={styles.row}>
                      <td className={styles.nameCell}>
                        <span className={styles.hotelNameText}>{amenity.name}</span>
                        <div style={{ fontSize: 12, color: 'var(--color-text-muted)', marginTop: 2 }}>
                          {amenity.description?.substring(0, 60)}{amenity.description?.length > 60 ? '...' : ''}
                        </div>
                      </td>
                      <td>
                        <Badge variant="category">{CATEGORY_LABELS[amenity.category] || amenity.category}</Badge>
                      </td>
                      <td>
                        {amenity.premium ? (
                          <Badge variant="premium">Premium</Badge>
                        ) : (
                          <span style={{ color: 'var(--color-text-muted)', fontSize: 13 }}>—</span>
                        )}
                      </td>
                      <td>
                        <Badge variant={amenity.active ? 'active' : 'inactive'}>
                          {amenity.active ? 'Active' : 'Inactive'}
                        </Badge>
                      </td>
                      <td>
                        <div className={styles.actions}>
                          <button
                            className={styles.actionBtn}
                            onClick={() => { setEditAmenity(amenity); setModalOpen(true); }}
                            title="Edit"
                          >
                            <Edit size={15} />
                          </button>
                          <button
                            className={styles.actionBtn}
                            onClick={() => handleToggleActive(amenity)}
                            title={amenity.active ? 'Deactivate' : 'Activate'}
                          >
                            {amenity.active ? <ToggleRight size={15} /> : <ToggleLeft size={15} />}
                          </button>
                          {!amenity.active && (
                            <button
                              className={styles.actionBtn}
                              onClick={() => handleRestore(amenity)}
                              title="Restore"
                            >
                              <RotateCcw size={15} />
                            </button>
                          )}
                          <button
                            className={`${styles.actionBtn} ${styles.deleteBtn}`}
                            onClick={() => handleSoftDelete(amenity)}
                            title="Soft Delete"
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
        onClose={() => { setModalOpen(false); setEditAmenity(null); }}
        title={editAmenity ? 'Edit Amenity' : 'Add New Amenity'}
        size="md"
      >
        <AmenityForm
          amenity={editAmenity}
          onSubmit={handleSubmit}
          loading={createMutation.isPending || updateMutation.isPending}
        />
      </Modal>
    </div>
  );
};

export default ManageAmenities;
