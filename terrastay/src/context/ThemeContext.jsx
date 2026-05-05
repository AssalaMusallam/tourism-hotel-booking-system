import { createContext, useEffect, useMemo, useState } from 'react';

export const ThemeContext = createContext(null);

const STORAGE_KEY = 'theme';

const getSystemTheme = () =>
  window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';

export const ThemeProvider = ({ children }) => {
  const [theme, setThemeState] = useState(() => localStorage.getItem(STORAGE_KEY) || 'system');
  const [systemTheme, setSystemTheme] = useState(getSystemTheme);
  const resolvedTheme = theme === 'system' ? systemTheme : theme;

  useEffect(() => {
    const media = window.matchMedia?.('(prefers-color-scheme: dark)');
    if (!media) return undefined;

    const handleChange = (event) => setSystemTheme(event.matches ? 'dark' : 'light');
    media.addEventListener?.('change', handleChange);
    return () => media.removeEventListener?.('change', handleChange);
  }, []);

  useEffect(() => {
    document.documentElement.classList.toggle('dark', resolvedTheme === 'dark');
    document.documentElement.dataset.theme = resolvedTheme;
  }, [resolvedTheme]);

  const setTheme = (nextTheme) => {
    const safeTheme = ['light', 'dark', 'system'].includes(nextTheme) ? nextTheme : 'system';
    localStorage.setItem(STORAGE_KEY, safeTheme);
    setThemeState(safeTheme);
  };

  const value = useMemo(() => ({
    theme,
    resolvedTheme,
    setTheme,
  }), [theme, resolvedTheme]);

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
};
