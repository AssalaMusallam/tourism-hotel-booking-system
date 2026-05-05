import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { BellRing, Search } from 'lucide-react';
import toast from 'react-hot-toast';
import Button from '../components/ui/Button';
import EmptyState from '../components/ui/EmptyState';
import Spinner from '../components/ui/Spinner';
import WaitingListCard from '../components/waitingList/WaitingListCard';
import { useCancelWaitingListEntry, useMyWaitingList } from '../hooks/useWaitingList';
import styles from './MyWaitingListPage.module.css';

const MyWaitingListPage = () => {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const query = useMyWaitingList(page);
  const cancelMutation = useCancelWaitingListEntry();
  const entries = query.data?.content || [];

  const cancel = (entry) => {
    if (!window.confirm('Are you sure you want to leave the waiting list?')) return;
    cancelMutation.mutate(entry.id, {
      onSuccess: () => toast.success('Waiting list entry cancelled'),
      onError: (error) => toast.error(error.response?.data?.message || 'Could not cancel waiting list entry'),
    });
  };

  const book = (entry) => {
    if (!entry.hotelId) {
      toast.error('Hotel link is missing for this waiting list entry.');
      return;
    }
    const params = new URLSearchParams({
      checkIn: entry.checkIn,
      checkOut: entry.checkOut,
      guests: '1',
    });
    navigate(`/hotels/${entry.hotelId}/availability?${params.toString()}`);
  };

  return (
    <section className={styles.page}>
      <div className={styles.header}>
        <div>
          <p className={styles.eyebrow}>Guest</p>
          <h1>My Waiting List</h1>
        </div>
      </div>

      {query.isLoading ? <Spinner centered /> : entries.length === 0 ? (
        <EmptyState
          icon={Search}
          title="You haven't joined any waiting lists yet."
          description="Browse hotels and join the list when a room is fully booked."
          action={{ label: 'Browse Hotels', onClick: () => navigate('/search') }}
        />
      ) : (
        <>
          <div className={styles.list}>
            {entries.map((entry) => (
              <WaitingListCard key={entry.id} entry={entry} onCancel={cancel} onBook={book} />
            ))}
          </div>
          <div className={styles.pagination}>
            <Button variant="secondary" disabled={page === 0} onClick={() => setPage((value) => value - 1)}>Previous</Button>
            <span>Page {page + 1}</span>
            <Button variant="secondary" disabled={query.data?.last !== false} onClick={() => setPage((value) => value + 1)}>Load More</Button>
          </div>
        </>
      )}
    </section>
  );
};

export const WaitingListNotificationStrip = () => {
  const navigate = useNavigate();
  const query = useMyWaitingList(0);
  const notified = (query.data?.content || []).find((entry) => entry.status === 'NOTIFIED');

  if (!notified) return null;

  return (
    <div className={styles.strip}>
      <BellRing size={18} />
      <strong>A room is available for you!</strong>
      <span>{notified.roomTypeName} at {notified.hotelName}</span>
      <Button size="sm" variant="primary" onClick={() => navigate('/my-waiting-list')}>View</Button>
    </div>
  );
};

export default MyWaitingListPage;
