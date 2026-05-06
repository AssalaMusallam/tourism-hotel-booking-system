import { useMemo, useState } from 'react';
import { MapContainer, Marker, Popup, TileLayer } from 'react-leaflet';
import { motion, AnimatePresence } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import L from 'leaflet';
import { MapPinned } from 'lucide-react';
import PriceDisplay from '../PriceDisplay';
import styles from './HotelsMap.module.css';

const customIcon = L.divIcon({
  html: '<div class="trs-pin"></div>',
  className: '',
  iconSize: [32, 32],
  iconAnchor: [16, 32],
  popupAnchor: [0, -36],
});

const isMapped = (hotel) =>
  hotel?.latitude != null &&
  hotel?.longitude != null &&
  !Number.isNaN(Number(hotel.latitude)) &&
  !Number.isNaN(Number(hotel.longitude));

const HotelsMap = ({ hotels = [], initiallyOpen = false, single = false, zoom = 8 }) => {
  const navigate = useNavigate();
  const [open, setOpen] = useState(initiallyOpen || single);
  const mappedHotels = useMemo(() => hotels.filter(isMapped), [hotels]);
  const center = mappedHotels.length === 1
    ? [Number(mappedHotels[0].latitude), Number(mappedHotels[0].longitude)]
    : [31.9474, 35.3026];

  return (
    <section className={styles.shell}>
      {!single && (
        <button type="button" className={styles.toggle} onClick={() => setOpen((value) => !value)}>
          <MapPinned size={18} />
          {open ? 'إخفاء الخريطة' : 'عرض على الخريطة'}
        </button>
      )}
      <AnimatePresence initial={false}>
        {open && (
          <motion.div
            className={styles.mapWrap}
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: single ? 360 : 480 }}
            exit={{ opacity: 0, height: 0 }}
            transition={{ duration: 0.24 }}
          >
            <span className={styles.badge}>{mappedHotels.length} فنادق على الخريطة</span>
            <MapContainer center={center} zoom={mappedHotels.length === 1 ? 14 : zoom} className={styles.map} scrollWheelZoom={false}>
              <TileLayer
                attribution="&copy; OpenStreetMap contributors"
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />
              {mappedHotels.map((hotel) => (
                <Marker key={hotel.id} position={[Number(hotel.latitude), Number(hotel.longitude)]} icon={customIcon}>
                  <Popup>
                    <div className={styles.popup}>
                      <strong>{hotel.name}</strong>
                      <span>{hotel.city} · ⭐ {Number(hotel.rating || 0).toFixed(1)}</span>
                      <span><PriceDisplay usdAmount={hotel.minPricePerNight || hotel.pricePerNight || hotel.basePrice || 90} suffix="/ليلة" /></span>
                      <button type="button" onClick={() => navigate(`/hotels/${hotel.id}`)}>عرض التفاصيل</button>
                    </div>
                  </Popup>
                </Marker>
              ))}
            </MapContainer>
          </motion.div>
        )}
      </AnimatePresence>
    </section>
  );
};

export default HotelsMap;
