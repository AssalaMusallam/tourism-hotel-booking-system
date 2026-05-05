import { forwardRef } from 'react';
import styles from './Select.module.css';
import { cn } from '../../utils/cn';

const Select = forwardRef(({
  label,
  error,
  id,
  options = [],
  placeholder,
  className,
  containerClassName,
  ...props
}, ref) => {
  const selectId = id || label?.toLowerCase().replace(/\s+/g, '-');

  return (
    <div className={cn(styles.container, containerClassName)}>
      {label && (
        <label htmlFor={selectId} className={styles.label}>
          {label}
        </label>
      )}
      <select
        ref={ref}
        id={selectId}
        className={cn(styles.select, error && styles.error, className)}
        {...props}
      >
        {placeholder && <option value="">{placeholder}</option>}
        {options.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
      {error && <span className={styles.errorMsg}>{error}</span>}
    </div>
  );
});

Select.displayName = 'Select';

export default Select;
