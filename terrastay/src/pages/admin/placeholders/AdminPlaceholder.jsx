import { useNavigate } from 'react-router-dom';
import styles from './AdminPlaceholder.module.css';

const AdminPlaceholder = ({ title }) => {
  const navigate = useNavigate();

  return (
    <section className={styles.placeholder}>
      <h1>{title}</h1>
      <p>هذه الصفحة جاهزة للربط عند توفر شاشة الإدارة الخاصة بها.</p>
      <button type="button" onClick={() => navigate('/admin')}>
        العودة للوحة التحكم
      </button>
    </section>
  );
};

export default AdminPlaceholder;
