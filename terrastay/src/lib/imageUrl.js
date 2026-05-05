const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * Construct a full image URL from a filename returned by the backend.
 *
 * Backend serves uploads at: <BASE_URL>/uploads/<filename>
 *
 * @param {string | null | undefined} filename
 * @returns {string}  absolute URL or local placeholder
 */
export function getImageUrl(filename) {
  if (!filename) return '/placeholder-hotel.jpg';
  if (filename.startsWith('http')) return filename;
  return `${BASE_URL}/uploads/${filename}`;
}
