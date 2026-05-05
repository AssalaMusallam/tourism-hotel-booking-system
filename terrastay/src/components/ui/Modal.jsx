import { useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { X } from 'lucide-react';
import styles from './Modal.module.css';

const Modal = ({ isOpen, onClose, title, children, size = 'md' }) => {
  const overlayRef = useRef(null);

  useEffect(() => {
    if (!isOpen) return;
    document.body.style.overflow = 'hidden';
    const onKey = (e) => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', onKey);
    return () => {
      document.body.style.overflow = '';
      window.removeEventListener('keydown', onKey);
    };
  }, [isOpen, onClose]);

  return createPortal(
    <AnimatePresence>
      {isOpen && (
        <motion.div
          ref={overlayRef}
          className={styles.overlay}
          initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
          transition={{ duration: 0.18 }}
          onClick={(e) => { if (e.target === overlayRef.current) onClose(); }}
        >
          <motion.div
            className={[styles.modal, styles[size]].join(' ')}
            initial={{ scale: 0.95, opacity: 0, y: 8 }}
            animate={{ scale: 1, opacity: 1, y: 0 }}
            exit={{ scale: 0.95, opacity: 0, y: 8 }}
            transition={{ duration: 0.18 }}
          >
            <div className={styles.header}>
              {title && <h3 className={styles.title}>{title}</h3>}
              <button className={styles.close} onClick={onClose} aria-label="Close">
                <X size={18} />
              </button>
            </div>
            <div className={styles.body}>{children}</div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>,
    document.body
  );
};

export default Modal;
