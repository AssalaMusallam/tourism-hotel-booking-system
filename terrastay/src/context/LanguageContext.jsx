import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import ar from '../translations/ar';
import en from '../translations/en';

const STORAGE_KEY = 'terrastay_language';
const LEGACY_STORAGE_KEY = 'language';
const dictionaries = { ar, en };

export const LanguageContext = createContext(null);

export const LanguageProvider = ({ children }) => {
  const [language, setLanguageState] = useState(() => localStorage.getItem(STORAGE_KEY) || localStorage.getItem(LEGACY_STORAGE_KEY) || 'ar');

  const applyLanguage = (nextLanguage) => {
    const safeLanguage = dictionaries[nextLanguage] ? nextLanguage : 'ar';
    if (safeLanguage === 'en') {
      document.documentElement.setAttribute('dir', 'ltr');
      document.documentElement.setAttribute('lang', 'en');
      document.documentElement.style.setProperty('--font-primary', "'Inter', sans-serif");
      document.documentElement.style.fontFamily = "'Inter', sans-serif";
      document.body.style.fontFamily = "'Inter', sans-serif";
    } else {
      document.documentElement.setAttribute('dir', 'rtl');
      document.documentElement.setAttribute('lang', 'ar');
      document.documentElement.style.setProperty('--font-primary', "'Cairo', sans-serif");
      document.documentElement.style.fontFamily = "'Cairo', sans-serif";
      document.body.style.fontFamily = "'Cairo', sans-serif";
    }
    localStorage.setItem(STORAGE_KEY, safeLanguage);
    localStorage.setItem(LEGACY_STORAGE_KEY, safeLanguage);
  };

  useEffect(() => {
    const saved = localStorage.getItem(STORAGE_KEY) || localStorage.getItem(LEGACY_STORAGE_KEY) || 'ar';
    applyLanguage(saved);
    if (saved !== language) setLanguageState(dictionaries[saved] ? saved : 'ar');
  }, []);

  useEffect(() => {
    applyLanguage(language);
  }, [language]);

  const setLanguage = (nextLanguage) => {
    const safeLanguage = dictionaries[nextLanguage] ? nextLanguage : 'ar';
    applyLanguage(safeLanguage);
    setLanguageState(safeLanguage);
  };

  const value = useMemo(() => {
    const messages = dictionaries[language] || ar;
    return {
      language,
      direction: language === 'ar' ? 'rtl' : 'ltr',
      setLanguage,
      t: (key) => messages[key] || en[key] || key,
      tField: (obj, field) => (
        language === 'en'
          ? obj?.[`${field}En`] || obj?.[field]
          : obj?.[field]
      ),
    };
  }, [language]);

  return <LanguageContext.Provider value={value}>{children}</LanguageContext.Provider>;
};

export const useLanguageContext = () => {
  const value = useContext(LanguageContext);
  if (!value) throw new Error('useLanguageContext must be used within LanguageProvider');
  return value;
};

export default LanguageContext;
