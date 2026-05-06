import { Link } from 'react-router-dom';
import { Bell, CreditCard, Globe2, Moon, Sun, Trash2, UserCog } from 'lucide-react';
import CurrencySelector from '../components/CurrencySelector';
import LanguageToggle from '../components/ui/LanguageToggle';
import useTheme from '../hooks/useTheme';
import useLanguage from '../hooks/useLanguage';
import styles from './SettingsPage.module.css';

const SettingsPage = () => {
  const { resolvedTheme, setTheme } = useTheme();
  const { t } = useLanguage();
  const isDark = resolvedTheme === 'dark';

  return (
    <section className={`container ${styles.page}`}>
      <header className={styles.header}>
        <span>TerraStay</span>
        <h1>{t('settings')}</h1>
      </header>

      <div className={styles.grid}>
        <article className={styles.card}>
          <Moon size={22} />
          <div>
            <h2>{t('appearance')}</h2>
            <p>{isDark ? t('darkMode') : t('lightMode')}</p>
          </div>
          <button
            type="button"
            className={`${styles.switch} ${isDark ? styles.switchActive : ''}`}
            onClick={() => setTheme(isDark ? 'light' : 'dark')}
            aria-label="Toggle dark mode"
          >
            {isDark ? <Moon size={16} /> : <Sun size={16} />}
          </button>
        </article>

        <article className={styles.card}>
          <Globe2 size={22} />
          <div>
            <h2>{t('language')}</h2>
            <p>العربية | English</p>
          </div>
          <LanguageToggle />
        </article>

        <article className={styles.card}>
          <CreditCard size={22} />
          <div>
            <h2>{t('currency')}</h2>
            <p>USD, JOD, EUR, SAR, ILS</p>
          </div>
          <CurrencySelector />
        </article>

        <article className={styles.card}>
          <Bell size={22} />
          <div>
            <h2>{t('notifications')}</h2>
            <p>تنبيهات البريد وتذكيرات الحجز</p>
          </div>
          <label className={styles.checkbox}>
            <input type="checkbox" defaultChecked />
            <span />
          </label>
        </article>

        <article className={styles.card}>
          <UserCog size={22} />
          <div>
            <h2>{t('account')}</h2>
            <p>إدارة بيانات الحساب وكلمة المرور</p>
          </div>
          <Link to="/profile" className={styles.linkButton}>{t('profile')}</Link>
        </article>

        <article className={`${styles.card} ${styles.danger}`}>
          <Trash2 size={22} />
          <div>
            <h2>Danger zone</h2>
            <p>Delete account requires confirmation.</p>
          </div>
          <button type="button">Delete</button>
        </article>
      </div>
    </section>
  );
};

export default SettingsPage;
