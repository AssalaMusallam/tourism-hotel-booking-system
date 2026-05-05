import { Mail, Plus } from 'lucide-react';
import { NOTIFICATION_STATUSES, NOTIFICATION_TYPES } from '../../api/notifications';
import Button from '../ui/Button';
import Input from '../ui/Input';
import Select from '../ui/Select';
import styles from './NotificationFilters.module.css';

const asOptions = (items) => items.map((item) => ({
  value: item,
  label: item.replaceAll('_', ' '),
}));

const NotificationFilters = ({
  email,
  status,
  type,
  onEmailChange,
  onStatusChange,
  onTypeChange,
  onSend,
}) => (
  <div className={styles.toolbar}>
    <div className={styles.filters}>
      <Input
        label="Search by email"
        icon={Mail}
        value={email}
        onChange={(event) => onEmailChange(event.target.value)}
        placeholder="guest@example.com"
      />
      <Select
        label="Status"
        value={status}
        onChange={(event) => onStatusChange(event.target.value)}
        placeholder="All statuses"
        options={asOptions(NOTIFICATION_STATUSES)}
      />
      <Select
        label="Type"
        value={type}
        onChange={(event) => onTypeChange(event.target.value)}
        placeholder="All types"
        options={asOptions(NOTIFICATION_TYPES)}
      />
    </div>
    <div className={styles.actions}>
      <Button icon={Plus} variant="primary" onClick={onSend}>
        Send Notification
      </Button>
    </div>
  </div>
);

export default NotificationFilters;
