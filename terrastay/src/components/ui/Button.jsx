import styles from './Button.module.css';

const Button = ({
  children, variant = 'primary', size = 'md',
  disabled, loading, icon: Icon, fullWidth,
  type = 'button', onClick, className, ...rest
}) => (
  <button
    type={type}
    disabled={disabled || loading}
    onClick={onClick}
    className={[
      styles.btn,
      styles[variant],
      styles[size],
      fullWidth ? styles.fullWidth : '',
      className || '',
    ].filter(Boolean).join(' ')}
    {...rest}
  >
    {loading && <span className={styles.spinner} />}
    {Icon && !loading && <Icon size={16} />}
    {children}
  </button>
);

export default Button;
