import { forwardRef } from 'react';
import styles from './Select.module.css';

const Select = forwardRef(({
  label, error, options = [], placeholder, id, className, containerClass, ...rest
}, ref) => {
  const selectId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined);
  return (
    <div className={[styles.wrap, containerClass].filter(Boolean).join(' ')}>
      {label && <label htmlFor={selectId} className={styles.label}>{label}</label>}
      <select
        ref={ref}
        id={selectId}
        className={[styles.select, error ? styles.error : '', className || ''].filter(Boolean).join(' ')}
        {...rest}
      >
        {placeholder && <option value="">{placeholder}</option>}
        {options.map((o) => (
          <option key={o.value} value={o.value}>{o.label}</option>
        ))}
      </select>
      {error && <span className={styles.err}>{error}</span>}
    </div>
  );
});
Select.displayName = 'Select';
export default Select;
