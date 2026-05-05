import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import Button from '../ui/Button';
import Modal from '../ui/Modal';
import useAuth from '../../hooks/useAuth';
import { useJoinWaitingList } from '../../hooks/useWaitingList';
import { waitingListErrorMessage } from '../../api/waitingListApi';
import styles from './JoinWaitingListButton.module.css';

const JoinWaitingListButton = ({ roomTypeId, checkIn, checkOut, roomTypeName, hotelName, onBookAvailable }) => {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [open, setOpen] = useState(false);
  const [joined, setJoined] = useState(false);

  const mutation = useJoinWaitingList();

  const start = () => {
    if (!isAuthenticated) {
      toast.error('Please log in to join the waiting list');
      navigate('/login', { state: { from: `${location.pathname}${location.search}` } });
      return;
    }
    setOpen(true);
  };

  const confirm = () => {
    mutation.mutate({ roomTypeId, checkIn, checkOut }, {
      onSuccess: (entry) => {
        setJoined(true);
        setOpen(false);
        toast.success(`You're #${entry.positionInQueue || 1} in the queue! We'll email you if a spot opens.`);
      },
      onError: (error) => {
        const message = waitingListErrorMessage(error);
        toast.error(message);
        if (error.response?.status === 400) {
          setOpen(false);
          onBookAvailable?.();
        }
      },
    });
  };

  if (joined) {
    return <Button variant="secondary" disabled fullWidth>On Waiting List ✓</Button>;
  }

  return (
    <>
      <Button variant="secondary" fullWidth onClick={start}>Notify Me When Available</Button>
      <Modal isOpen={open} onClose={() => setOpen(false)} title="Join Waiting List" size="md">
        <div className={styles.modal}>
          <div>
            <strong>{roomTypeName}</strong>
            <span>{hotelName}</span>
          </div>
          <p>{checkIn} to {checkOut}</p>
          <p>We'll notify you if a room becomes available. You'll have 24 hours to complete your booking.</p>
          <Button variant="primary" loading={mutation.isPending} onClick={confirm}>
            Confirm - Join Waiting List
          </Button>
        </div>
      </Modal>
    </>
  );
};

export default JoinWaitingListButton;
