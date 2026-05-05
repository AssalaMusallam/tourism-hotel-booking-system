import { useRef, useState } from 'react';
import { Upload, X } from 'lucide-react';
import styles from './ImageUpload.module.css';

// existingImages: [{ id, imageUrl, fileName }]
// onUpload(files): called with File[]
// onDeleteExisting(imageId): called when removing an existing image
const ImageUpload = ({ existingImages = [], onUpload, onDeleteExisting, disabled }) => {
  const inputRef = useRef(null);
  const [previews, setPreviews] = useState([]); // { url, file }

  const handleFiles = (files) => {
    const valid = Array.from(files).filter((f) => f.type.startsWith('image/')).slice(0, 10 - existingImages.length - previews.length);
    if (!valid.length) return;
    const newPreviews = valid.map((f) => ({ url: URL.createObjectURL(f), file: f }));
    const all = [...previews, ...newPreviews];
    setPreviews(all);
    onUpload?.(all.map((p) => p.file));
  };

  const removePreview = (i) => {
    const next = previews.filter((_, idx) => idx !== i);
    setPreviews(next);
    onUpload?.(next.map((p) => p.file));
  };

  const onDrop = (e) => {
    e.preventDefault();
    handleFiles(e.dataTransfer.files);
  };

  return (
    <div className={styles.wrap}>
      {/* Existing images */}
      {existingImages.map((img) => (
        <div key={img.id} className={styles.thumb}>
          <img src={img.imageUrl} alt={img.fileName} className={styles.img} />
          {onDeleteExisting && (
            <button type="button" className={styles.del} onClick={() => onDeleteExisting(img.id)} disabled={disabled}>
              <X size={12} />
            </button>
          )}
        </div>
      ))}
      {/* New previews */}
      {previews.map((p, i) => (
        <div key={i} className={styles.thumb}>
          <img src={p.url} alt="preview" className={styles.img} />
          <button type="button" className={styles.del} onClick={() => removePreview(i)}>
            <X size={12} />
          </button>
        </div>
      ))}
      {/* Drop zone */}
      {existingImages.length + previews.length < 10 && (
        <div
          className={styles.zone}
          onClick={() => inputRef.current?.click()}
          onDrop={onDrop}
          onDragOver={(e) => e.preventDefault()}
        >
          <Upload size={20} />
          <span>Click or drag</span>
          <input
            ref={inputRef}
            type="file"
            accept="image/*"
            multiple
            style={{ display: 'none' }}
            onChange={(e) => handleFiles(e.target.files)}
            disabled={disabled}
          />
        </div>
      )}
    </div>
  );
};
export default ImageUpload;
