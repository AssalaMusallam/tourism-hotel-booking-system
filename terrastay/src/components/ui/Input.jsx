import { forwardRef } from 'react';
import styles from './Input.module.css';
import { cn } from '../../utils/cn';

const Input = forwardRef(({
  label,
  error,
  hint,
  id,
  className,
  containerClassName,
  leftIcon: LeftIcon,
  rightElement,
  ...props
}, ref) => {
  const inputId = id || label?.toLowerCase().replace(/\s+/g, '-');

  return (
    <div className={cn(styles.container, containerClassName)}>
      {label && (
        <label htmlFor={inputId} className={styles.label}>
          {label}
        </label>
      )}
      <div className={styles.inputWrapper}>
        {LeftIcon && (
          <span className={styles.leftIcon}>
            <LeftIcon size={16} />
          </span>
        )}
        <input
          ref={ref}
          id={inputId}
          className={cn(
            styles.input,
            error && styles.error,
            LeftIcon && styles.withLeftIcon,
            rightElement && styles.withRightElement,
            className
          )}
          {...props}
        />
        {rightElement && (
          <span className={styles.rightElement}>{rightElement}</span>
        )}
      </div>
      {error && <span className={styles.errorMsg}>{error}</span>}
      {hint && !error && <span className={styles.hint}>{hint}</span>}
    </div>
  );
});

Input.displayName = 'Input';

export default Input;
