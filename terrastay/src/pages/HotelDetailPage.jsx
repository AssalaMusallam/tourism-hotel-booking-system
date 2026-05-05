import { useState } from 'react';
import { useParams, Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { MapPin, Star, Phone, Mail } from 'lucide-react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { getHotelById } from '../api/hotels';
import useAuth from '../hooks/useAuth';
import ImageGallery from '../components/hotel/ImageGallery';
import AmenitiesList from '../components/hotel/AmenitiesList';
import PoliciesSection from '../components/hotel/PoliciesSection';
import RoomTypeCard from '../components/hotel/RoomTypeCard';
import Spinner from '../components/ui/Spinner';
import styles from './HotelDetailPage.module.css';

const TABS = ['Overview', 'Rooms', 'Amenities', 'Policies'];

const HotelDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { isAuthenticated } = useAuth();
  const [activeTab, setActiveTab] = useState('Overview');
  const [selectedRoom, setSelectedRoom] = useState(null);

  const checkIn = searchParams.get('checkIn') || '';
  const checkOut = searchParams.get('checkOut') || '';
  const guests = searchParams.get('guests') || '2';

  const { data: hotel, isLoading, error } = useQuery({
    queryKey: ['hotel', id],
    queryFn: () => getHotelById(id),
  });

  if (isLoading) return <Spinner centered />;
  if (error || !hotel) return (
    <div className="container" style={{ padding: '80px 0', textAlign: 'center' }}>
      <h2>Hotel not found</h2>
      <Link to="/search">← Back to Search</Link>
    </div>
  );

  const handleBookRoom = (room) => {
    if (!isAuthenticated) {
      sessionStorage.setItem('pendingBooking', JSON.stringify({ hotelId: id, roomId: room.id }));
      toast('Please log in to complete your booking', { icon: '🔒' });
      navigate('/login', { state: { from: `/booking/${id}` } });
      return;
    }
    const params = new URLSearchParams();
    if (checkIn) params.set('checkIn', checkIn);
    if (checkOut) params.set('checkOut', checkOut);
    if (guests) params.set('guests', guests);
    navigate(`/booking/${id}?${params.toString()}`, { state: { room, hotel } });
  };

  const starsArr = Array.from({ length: hotel.stars }, (_, i) => i);

  return (
    <div>
      {/* Breadcrumb */}
      <div className={styles.breadcrumbWrap}>
        <div className={`container ${styles.breadcrumb}`}>
          <Link to="/">Home</Link>
          <span>/</span>
          <Link to="/search">Search</Link>
          <span>/</span>
          <span>{hotel.name}</span>
        </div>
      </div>

      <div className="container" style={{ paddingBottom: 80 }}>
        {/* Gallery */}
        <div className={styles.galleryWrap}>
          <ImageGallery images={hotel.images} name={hotel.name} />
        </div>

        {/* Hotel Header */}
        <motion.div
          className={styles.hotelHeader}
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.35 }}
        >
          <div className={styles.hotelMeta}>
            <div className={styles.location}>
              <MapPin size={16} />
              <span>{hotel.address}</span>
            </div>
            <h1 className={styles.hotelName}>{hotel.name}</h1>
            <div className={styles.metaRow}>
              <div className={styles.stars}>
                {starsArr.map((i) => <Star key={i} size={16} fill="currentColor" />)}
              </div>
              <div className={styles.rating}>
                <strong>{hotel.rating}</strong>
                <span>({hotel.reviewCount} reviews)</span>
              </div>
            </div>
            <div className={styles.contact}>
              {hotel.phone && (
                <a href={`tel:${hotel.phone}`} className={styles.contactLink}>
                  <Phone size={14} /> {hotel.phone}
                </a>
              )}
              {hotel.email && (
                <a href={`mailto:${hotel.email}`} className={styles.contactLink}>
                  <Mail size={14} /> {hotel.email}
                </a>
              )}
            </div>
          </div>
          <div className={styles.priceTag}>
            <span className={styles.priceFrom}>From</span>
            <span className={styles.price}>${hotel.pricePerNight}</span>
            <span className={styles.priceNight}>/night</span>
          </div>
        </motion.div>

        {/* Tab Navigation */}
        <div className={styles.tabs}>
          {TABS.map((tab) => (
            <button
              key={tab}
              className={`${styles.tab} ${activeTab === tab ? styles.activeTab : ''}`}
              onClick={() => setActiveTab(tab)}
            >
              {tab}
            </button>
          ))}
        </div>

        {/* Tab Content */}
        <motion.div
          key={activeTab}
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.25 }}
          className={styles.tabContent}
        >
          {activeTab === 'Overview' && (
            <div className={styles.overview}>
              <p className={styles.description}>{hotel.description}</p>
            </div>
          )}

          {activeTab === 'Rooms' && (
            <div className={styles.rooms}>
              <p className={styles.roomsHint}>
                Select a room to book your stay
              </p>
              {(hotel.rooms || []).map((room) => (
                <RoomTypeCard
                  key={room.id}
                  room={room}
                  selected={selectedRoom?.id === room.id}
                  onSelect={(r) => {
                    setSelectedRoom(r);
                    handleBookRoom(r);
                  }}
                />
              ))}
            </div>
          )}

          {activeTab === 'Amenities' && (
            <AmenitiesList amenities={hotel.amenities} />
          )}

          {activeTab === 'Policies' && (
            <PoliciesSection policies={hotel.policies} />
          )}
        </motion.div>
      </div>
    </div>
  );
};

export default HotelDetailPage;
