import { AnimatePresence, motion } from 'framer-motion';
import { Check, X } from 'lucide-react';
import { useAdminRoleRequests } from '../../hooks/useRoleRequest';
import { updateUserRole } from '../../api/usersApi';
import styles from './RoleRequestsPage.module.css';

const RoleRequestsPage = () => {
  const { requests, updateStatus } = useAdminRoleRequests();
  const pending = requests.filter((request) => request.status === 'pending');

  const approve = async (request) => {
    if (request.userId && !String(request.userId).includes('@')) {
      try {
        await updateUserRole(request.userId, 'MANAGER');
      } catch {
        // Local mock requests may not map to backend users; status still updates for demo flow.
      }
    }
    updateStatus(request.id, 'approved');
  };

  return (
    <section className={styles.page}>
      <div className={styles.header}>
        <div>
          <h2>طلبات ترقية الدور</h2>
          <p>راجع طلبات الضيوف الراغبين بإدارة الفنادق على TerraStay.</p>
        </div>
        <span className={styles.count}>{pending.length} قيد الانتظار</span>
      </div>

      <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>المستخدم</th>
              <th>البريد</th>
              <th>الدور الحالي</th>
              <th>تاريخ الطلب</th>
              <th>الإجراء</th>
            </tr>
          </thead>
          <tbody>
            <AnimatePresence initial={false}>
              {pending.map((request) => (
                <motion.tr
                  key={request.id}
                  initial={{ opacity: 0, y: 8 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, x: 24 }}
                >
                  <td>{request.name}</td>
                  <td>{request.email}</td>
                  <td><span className={styles.badge}>{request.currentRole}</span></td>
                  <td>{new Date(request.requestedAt).toLocaleDateString('ar')}</td>
                  <td>
                    <div className={styles.actions}>
                      <button className={styles.approve} onClick={() => approve(request)}>
                        <Check size={15} /> قبول
                      </button>
                      <button className={styles.reject} onClick={() => updateStatus(request.id, 'rejected')}>
                        <X size={15} /> رفض
                      </button>
                    </div>
                  </td>
                </motion.tr>
              ))}
            </AnimatePresence>
            {pending.length === 0 && (
              <tr>
                <td colSpan="5" className={styles.empty}>لا توجد طلبات قيد الانتظار حالياً.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
};

export default RoleRequestsPage;
