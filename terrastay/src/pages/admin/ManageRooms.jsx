import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { addDays, format } from 'date-fns';
import { Plus, Edit, Trash2, ChevronLeft, Settings, BedDouble, Users } from 'lucide-react';
import toast from 'react-hot-toast';
import {
  useRoomsByHotel, useCreateRoom, useUpdateRoom, useDeleteRoom, useChangeRoomStatus, useAdminHotel
} from '../../hooks/useCatalogQueries';
import RoomTypeForm from '../../components/hotel/RoomTypeForm';
import Modal from '../../components/ui/Modal';
import Button from '../../components/ui/Button';
import Spinner from '../../components/ui/Spinner';
import Pagination from '../../components/ui/Pagination';
import Badge, { StatusBadge } from '../../components/ui/Badge';
import EmptyState from '../../components/ui/EmptyState';
import AdminWaitingListPanel from '../../components/waitingList/AdminWaitingListPanel';
import { useWaitingListCount } from '../../hooks/useWaitingList';
import styles from './ManageHotels.module.css';

const BED_LABELS = {
  TWIN: 'Twin',
  QUEEN: 'Queen',
  KING: 'King',
};

const today = format(new Date(), 'yyyy-MM-dd');
const tomorrow = format(addDays(new Date(), 1), 'yyyy-MM-dd');

const WaitingCountBadge = ({ roomTypeId, onClick }) => {
  const { data } = useWaitingListCount(roomTypeId, today, tomorrow);
  return (
    <button className={styles.waitingBadge} onClick={onClick} title="Open waiting list">
      Waiting: {data?.waitingCount ?? 0}
    </button>
  );
};

const ManageRooms = () => {
  const { hotelId } = useParams();
  const [modalOpen, setModalOpen] = useState(false);
  const [editRoom, setEditRoom] = useState(null);
  const [statusModal, setStatusModal] = useState(null);
  const [newStatus, setNewStatus] = useState('ACTIVE');
  const [waitingRoom, setWaitingRoom] = useState(null);
  const [page, setPage] = useState(0);

  // Fetch hotel name via admin hook
  const { data: hotel } = useAdminHotel(hotelId);

  // No status filter for admin — show all rooms
  const { data, isLoading } = useRoomsByHotel(hotelId, { page, size: 20 });
  const rooms = data?.content || [];
  const totalPages = data?.totalPages || 0;

  const createMutation = useCreateRoom();
  const updateMutation = useUpdateRoom();
  const deleteMutation = useDeleteRoom();
  const statusMutation = useChangeRoomStatus();

  const handleSubmit = (formData) => {
    if (editRoom) {
      updateMutation.mutate({ hotelId, id: editRoom.id, data: formData }, {
        onSuccess: () => {
          toast.success('Room updated');
          setModalOpen(false);
          setEditRoom(null);
        },
        onError: (e) => toast.error(e.response?.data?.message || 'Failed to update'),
      });
    } else {
      createMutation.mutate({ hotelId, data: formData }, {
        onSuccess: () => {
          toast.success('Room type added');
          setModalOpen(false);
        },
        onError: (e) => toast.error(e.response?.data?.message || 'Failed to create'),
      });
    }
  };

  const handleDelete = (room) => {
    if (window.confirm(`Delete "${room.name}"?`)) {
      deleteMutation.mutate({ hotelId, id: room.id }, {
        onSuccess: () => toast.success('Room deleted'),
        onError: () => toast.error('Failed to delete'),
      });
    }
  };

  const handleStatusChange = () => {
    if (!statusModal) return;
    statusMutation.mutate({ hotelId, id: statusModal.id, status: newStatus }, {
      onSuccess: () => {
        toast.success('Status updated');
        setStatusModal(null);
      },
      onError: () => toast.error('Failed to change status'),
    });
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
          <div>
            <Link to="/admin/hotels" style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 13, color: 'var(--color-terracotta)', marginBottom: 8, textDecoration: 'none' }}>
              <ChevronLeft size={16} /> Back to Hotels
            </Link>
            <h1>{hotel?.name || 'Hotel'} — Rooms</h1>
          </div>
          <Button variant="primary" icon={Plus} onClick={() => { setEditRoom(null); setModalOpen(true); }}>
            Add Room Type
          </Button>
        </div>

        {isLoading ? <Spinner centered /> : rooms.length === 0 ? (
          <EmptyState
            title="No room types yet"
            description="Add your first room type for this hotel."
            action={{ label: 'Add Room', onClick: () => setModalOpen(true) }}
          />
        ) : (
          <>
            <div className={styles.tableWrap}>
              <table className={styles.table}>
                <thead>
                  <tr>
                    <th>Room Name</th>
                    <th>Bed Type</th>
                    <th>Capacity</th>
                    <th>Price/Night</th>
                    <th>Units</th>
                    <th>Waiting</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {rooms.map((room) => (
                    <tr key={room.id} className={styles.row}>
                      <td className={styles.nameCell}>
                        <span className={styles.hotelNameText}>{room.name}</span>
                      </td>
                      <td>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                          <BedDouble size={14} />
                          {room.bedCount}x {BED_LABELS[room.bedType] || room.bedType}
                        </span>
                      </td>
                      <td>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                          <Users size={14} />
                          {room.maxAdults}A{room.maxChildren > 0 ? ` + ${room.maxChildren}C` : ''} (cap: {room.capacity})
                        </span>
                      </td>
                      <td style={{ fontWeight: 600, color: 'var(--color-terracotta)' }}>
                        ${Number(room.basePrice).toFixed(0)}
                      </td>
                      <td>{room.totalUnits}</td>
                      <td>
                        <WaitingCountBadge roomTypeId={room.id} onClick={() => setWaitingRoom(room)} />
                      </td>
                      <td><StatusBadge status={room.status} /></td>
                      <td>
                        <div className={styles.actions}>
                          <button
                            className={styles.actionBtn}
                            onClick={() => { setEditRoom(room); setModalOpen(true); }}
                            title="Edit"
                          >
                            <Edit size={15} />
                          </button>
                          <button
                            className={styles.actionBtn}
                            onClick={() => { setStatusModal(room); setNewStatus(room.status); }}
                            title="Change Status"
                          >
                            <Settings size={15} />
                          </button>
                          <button
                            className={`${styles.actionBtn} ${styles.deleteBtn}`}
                            onClick={() => handleDelete(room)}
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

      {/* Add/Edit Room Modal */}
      <Modal
        isOpen={modalOpen}
        onClose={() => { setModalOpen(false); setEditRoom(null); }}
        title={editRoom ? 'Edit Room Type' : 'Add Room Type'}
        size="lg"
      >
        <RoomTypeForm
          hotelId={hotelId}
          room={editRoom}
          onSubmit={handleSubmit}
          loading={createMutation.isPending || updateMutation.isPending}
        />
      </Modal>

      {/* Status Change Modal */}
      <Modal
        isOpen={!!statusModal}
        onClose={() => setStatusModal(null)}
        title={`Change Status — ${statusModal?.name || ''}`}
        size="sm"
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <select
            value={newStatus}
            onChange={(e) => setNewStatus(e.target.value)}
            style={{ padding: '10px 14px', borderRadius: 8, border: '1.5px solid var(--color-beige)', fontSize: 14, fontFamily: 'var(--font-body)' }}
          >
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
          </select>
          <Button variant="primary" onClick={handleStatusChange} loading={statusMutation.isPending}>
            Update Status
          </Button>
        </div>
      </Modal>

      {waitingRoom && (
        <AdminWaitingListPanel
          roomTypeId={waitingRoom.id}
          roomTypeName={waitingRoom.name}
          onClose={() => setWaitingRoom(null)}
        />
      )}
    </div>
  );
};

export default ManageRooms;
