import { useState } from 'react';
import { addDays, format } from 'date-fns';
import Button from '../ui/Button';
import Spinner from '../ui/Spinner';
import WaitingListStatusBadge from './WaitingListStatusBadge';
import { useAdminWaitingList, useWaitingListCount } from '../../hooks/useWaitingList';
import styles from './AdminWaitingListPanel.module.css';

const today = format(new Date(), 'yyyy-MM-dd');
const tomorrow = format(addDays(new Date(), 1), 'yyyy-MM-dd');

const formatDateTime = (value) => value
  ? new Date(value).toLocaleString('en-US', { month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit' })
  : '-';

const AdminWaitingListPanel = ({ roomTypeId, roomTypeName, onClose }) => {
  const [page, setPage] = useState(0);
  const [checkIn, setCheckIn] = useState(today);
  const [checkOut, setCheckOut] = useState(tomorrow);

  const listQuery = useAdminWaitingList(roomTypeId, page);
  const countQuery = useWaitingListCount(roomTypeId, checkIn, checkOut);
  const entries = listQuery.data?.content || [];

  return (
    <div className={styles.overlay} onClick={onClose}>
      <aside className={styles.panel} onClick={(event) => event.stopPropagation()}>
        <div className={styles.header}>
          <div>
            <h2>Waiting List - {roomTypeName}</h2>
            <p>Email addresses are masked for guest privacy.</p>
          </div>
          <button type="button" onClick={onClose}>Close</button>
        </div>

        <div className={styles.filters}>
          <label>
            <span>Check-in</span>
            <input type="date" value={checkIn} onChange={(event) => setCheckIn(event.target.value)} />
          </label>
          <label>
            <span>Check-out</span>
            <input type="date" value={checkOut} onChange={(event) => setCheckOut(event.target.value)} />
          </label>
        </div>

        <div className={styles.count}>
          {countQuery.isLoading ? 'Counting...' : `${countQuery.data?.waitingCount || 0} guests waiting for this period`}
        </div>

        {listQuery.isLoading ? <Spinner centered /> : (
          <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Position</th>
                  <th>Guest</th>
                  <th>Check-in</th>
                  <th>Check-out</th>
                  <th>Status</th>
                  <th>Joined</th>
                  <th>Notified At</th>
                </tr>
              </thead>
              <tbody>
                {entries.map((entry, index) => (
                  <tr key={entry.id}>
                    <td>#{page * 20 + index + 1}</td>
                    <td title="Email addresses are masked for guest privacy">
                      <strong>{entry.guestName}</strong>
                      <span>{entry.maskedEmail}</span>
                    </td>
                    <td>{entry.checkIn}</td>
                    <td>{entry.checkOut}</td>
                    <td><WaitingListStatusBadge status={entry.status} /></td>
                    <td>{formatDateTime(entry.createdAt)}</td>
                    <td>{formatDateTime(entry.notifiedAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            {entries.length === 0 && <p className={styles.empty}>No waiting list entries for this room.</p>}
          </div>
        )}

        <div className={styles.pagination}>
          <Button size="sm" variant="secondary" disabled={page === 0} onClick={() => setPage((value) => value - 1)}>Previous</Button>
          <span>Page {page + 1}</span>
          <Button size="sm" variant="secondary" disabled={listQuery.data?.last !== false} onClick={() => setPage((value) => value + 1)}>Next</Button>
        </div>
      </aside>
    </div>
  );
};

export default AdminWaitingListPanel;
