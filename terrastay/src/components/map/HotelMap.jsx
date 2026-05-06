import { useEffect, useMemo, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import useTheme from '../../hooks/useTheme';
import { createHotelMarkerIcon } from './HotelMarker';
import { getHotelPopupHtml } from './HotelMapPopup';
import styles from './HotelMap.module.css';

const LIGHT_TILE = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
const DARK_TILE = 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png';
const LIGHT_ATTRIBUTION = '&copy; OpenStreetMap contributors';
const DARK_ATTRIBUTION = '&copy; OpenStreetMap &copy; CARTO';

const validCoordinate = (hotel) =>
    hotel?.latitude != null &&
    hotel?.longitude != null &&
    !Number.isNaN(Number(hotel.latitude)) &&
    !Number.isNaN(Number(hotel.longitude));

const HotelMap = ({ hotels = [], selectedId, onMarkerClick, height = '500px' }) => {
  const { resolvedTheme } = useTheme();
  const navigate = useNavigate();

  const mapRef = useRef(null);
  const nodeRef = useRef(null);
  const tileRef = useRef(null);
  const markersRef = useRef(new Map());

  const visibleHotels = useMemo(() => hotels.filter(validCoordinate), [hotels]);

  useEffect(() => {
    if (!nodeRef.current || mapRef.current) return;

    mapRef.current = L.map(nodeRef.current, {
      scrollWheelZoom: false,
      zoomControl: true,
    }).setView([31.9, 35.2], 9);

    setTimeout(() => {
      mapRef.current?.invalidateSize();
    }, 0);

    return () => {
      markersRef.current.forEach((marker) => marker.remove());
      markersRef.current.clear();

      tileRef.current?.remove();
      tileRef.current = null;

      mapRef.current?.remove();
      mapRef.current = null;
    };
  }, []);

  useEffect(() => {
    const map = mapRef.current;
    if (!map) return;

    if (tileRef.current) {
      tileRef.current.remove();
    }

    tileRef.current = L.tileLayer(
        resolvedTheme === 'dark' ? DARK_TILE : LIGHT_TILE,
        {
          attribution: resolvedTheme === 'dark' ? DARK_ATTRIBUTION : LIGHT_ATTRIBUTION,
          maxZoom: 19,
        }
    ).addTo(map);
  }, [resolvedTheme]);

  useEffect(() => {
    const map = mapRef.current;
    if (!map) return;

    markersRef.current.forEach((marker) => marker.remove());
    markersRef.current.clear();

    const bounds = [];

    visibleHotels.forEach((hotel) => {
      const selected = Number(selectedId) === Number(hotel.id);
      const latLng = [Number(hotel.latitude), Number(hotel.longitude)];

      const marker = L.marker(latLng, {
        icon: createHotelMarkerIcon(L, hotel, selected),
        riseOnHover: true,
      }).addTo(map);

      marker.bindPopup(getHotelPopupHtml(hotel), {
        closeButton: true,
        className: resolvedTheme === 'dark' ? 'pf-map-popup-dark' : 'pf-map-popup-light',
      });

      marker.on('click', () => onMarkerClick?.(hotel.id));

      markersRef.current.set(Number(hotel.id), marker);
      bounds.push(latLng);
    });

    if (visibleHotels.length === 1 && bounds[0]) {
      map.setView(bounds[0], 14);
    } else if (bounds.length > 0) {
      map.fitBounds(bounds, { padding: [34, 34], maxZoom: 14 });
    }
  }, [visibleHotels, selectedId, onMarkerClick, resolvedTheme]);

  useEffect(() => {
    markersRef.current.forEach((marker, id) => {
      const hotel = visibleHotels.find((item) => Number(item.id) === id);

      if (hotel) {
        marker.setIcon(createHotelMarkerIcon(L, hotel, Number(selectedId) === id));
      }
    });
  }, [selectedId, visibleHotels]);

  useEffect(() => {
    const node = nodeRef.current;
    if (!node) return undefined;

    const handleClick = (event) => {
      const button = event.target.closest('[data-hotel-id]');

      if (button) {
        navigate(`/hotels/${button.dataset.hotelId}`);
      }
    };

    node.addEventListener('click', handleClick);

    return () => {
      node.removeEventListener('click', handleClick);
    };
  }, [navigate]);

  if (visibleHotels.length === 0) {
    return (
        <div className={styles.wrap} style={{ height }}>
          <div className={styles.message}>No mapped hotel locations available.</div>
        </div>
    );
  }

  return (
      <div className={styles.wrap} style={{ height }}>
        <div ref={nodeRef} className={styles.map} />
      </div>
  );
};

export default HotelMap;
