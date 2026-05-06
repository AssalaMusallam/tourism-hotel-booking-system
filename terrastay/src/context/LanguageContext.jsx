import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import ar from '../translations/ar';
import en from '../translations/en';

const STORAGE_KEY = 'language';
const dictionaries = { ar, en };

export const LanguageContext = createContext(null);

export const LanguageProvider = ({ children }) => {
  const [language, setLanguageState] = useState(() => localStorage.getItem(STORAGE_KEY) || 'ar');

  useEffect(() => {
    const safeLanguage = dictionaries[language] ? language : 'ar';
    document.documentElement.lang = safeLanguage;
    document.documentElement.dir = safeLanguage === 'ar' ? 'rtl' : 'ltr';
    localStorage.setItem(STORAGE_KEY, safeLanguage);
  }, [language]);

  const setLanguage = (nextLanguage) => {
    setLanguageState(dictionaries[nextLanguage] ? nextLanguage : 'ar');
  };

  const value = useMemo(() => {
    const messages = dictionaries[language] || ar;
    return {
      language,
      direction: language === 'ar' ? 'rtl' : 'ltr',
      setLanguage,
      t: (key) => messages[key] || en[key] || key,
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
