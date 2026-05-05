import Navbar from './Navbar';
import Footer from './Footer';
import styles from './PageLayout.module.css';

const PageLayout = ({ children }) => {
  return (
    <>
      <Navbar />
      <main className={styles.main}>{children}</main>
      <Footer />
    </>
  );
};

export default PageLayout;
