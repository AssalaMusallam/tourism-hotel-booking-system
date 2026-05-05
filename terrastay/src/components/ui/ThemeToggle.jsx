import { Monitor, Moon, Sun } from 'lucide-react';
import useTheme from '../../hooks/useTheme';
import styles from './ThemeToggle.module.css';

const ORDER = ['light', 'dark', 'system'];
const LABELS = {
  light: 'Light mode',
  dark: 'Dark mode',
  system: 'System theme',
};
const ICONS = {
  light: Sun,
  dark: Moon,
  system: Monitor,
};

const ThemeToggle = () => {
  const { theme, setTheme } = useTheme();
  const Icon = ICONS[theme] || Monitor;

  const cycleTheme = () => {
    const index = ORDER.indexOf(theme);
    setTheme(ORDER[(index + 1) % ORDER.length]);
  };

  return (
    <button
      type="button"
      className={styles.toggle}
      onClick={cycleTheme}
      aria-label={`Current theme: ${LABELS[theme]}. Change theme`}
    >
      <Icon size={18} aria-hidden="true" />
      <span className={styles.tooltip}>{LABELS[theme]}</span>
    </button>
  );
};

export default ThemeToggle;
