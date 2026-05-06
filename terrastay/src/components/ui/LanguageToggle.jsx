import useLanguage from '../../hooks/useLanguage';
import styles from './LanguageToggle.module.css';

const LanguageToggle = () => {
  const { language, setLanguage } = useLanguage();

  return (
    <div className={styles.toggle} aria-label="Language">
      <button
        type="button"
        className={language === 'ar' ? styles.active : ''}
        onClick={() => setLanguage('ar')}
      >
        AR
      </button>
      <button
        type="button"
        className={language === 'en' ? styles.active : ''}
        onClick={() => setLanguage('en')}
      >
        EN
      </button>
    </div>
  );
};

export default LanguageToggle;
