import { useLanguageContext } from '../context/LanguageContext';

const useLanguage = () => useLanguageContext();

export const useTranslation = () => {
  const { t, language, direction, setLanguage } = useLanguageContext();
  return { t, language, direction, setLanguage };
};

export default useLanguage;
