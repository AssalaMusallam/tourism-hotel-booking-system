import { useSearchParams as useRouterSearchParams } from 'react-router-dom';
import { useCallback } from 'react';

const useSearchParams = () => {
  const [searchParams, setSearchParams] = useRouterSearchParams();

  const getParam = useCallback((key, defaultValue = '') => {
    return searchParams.get(key) || defaultValue;
  }, [searchParams]);

  const setParam = useCallback((key, value) => {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      if (value === '' || value === null || value === undefined) {
        next.delete(key);
      } else {
        next.set(key, value);
      }
      return next;
    });
  }, [setSearchParams]);

  const setParams = useCallback((params) => {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      Object.entries(params).forEach(([key, value]) => {
        if (value === '' || value === null || value === undefined) {
          next.delete(key);
        } else {
          next.set(key, String(value));
        }
      });
      return next;
    });
  }, [setSearchParams]);

  return { searchParams, getParam, setParam, setParams };
};

export default useSearchParams;
