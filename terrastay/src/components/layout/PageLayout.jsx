import Navbar from './Navbar';
import Footer from './Footer';
import InactiveAccountBanner from './InactiveAccountBanner';
import { WaitingListNotificationStrip } from '../../pages/MyWaitingListPage';
import styles from './PageLayout.module.css';

const PageLayout = ({ children }) => {
  return (
    <>
      <Navbar />
      <InactiveAccountBanner />
      <WaitingListNotificationStrip />
      <main className={styles.main}>{children}</main>
      <Footer />
    </>
  );
};

export default PageLayout;
