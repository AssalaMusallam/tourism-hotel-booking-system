import { forwardRef } from 'react';
import styles from './Input.module.css';

const Input = forwardRef(({
  label, error, hint, icon: Icon, type = 'text',
  id, className, containerClass, rightElement, ...rest
}, ref) => {
  const inputId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined);
  return (
    <div className={[styles.wrap, containerClass].filter(Boolean).join(' ')}>
      {label && <label htmlFor={inputId} className={styles.label}>{label}</label>}
      <div className={styles.inputWrap}>
        {Icon && <span className={styles.iconLeft}><Icon size={15} /></span>}
        <input
          ref={ref}
          id={inputId}
          type={type}
          className={[
            styles.input,
            error ? styles.error : '',
            Icon ? styles.withIcon : '',
            rightElement ? styles.withRight : '',
            className || '',
          ].filter(Boolean).join(' ')}
          {...rest}
        />
        {rightElement && <span className={styles.right}>{rightElement}</span>}
      </div>
      {error && <span className={styles.err}>{error}</span>}
      {hint && !error && <span className={styles.hint}>{hint}</span>}
    </div>
  );
});
Input.displayName = 'Input';
export default Input;
