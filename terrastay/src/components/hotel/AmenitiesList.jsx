import { Wifi, Car, UtensilsCrossed, Waves, Dumbbell, Sparkles, BellRing, Star, ChefHat, Building2, Landmark, Trees } from 'lucide-react';
import styles from './AmenitiesList.module.css';

const iconMap = {
  WiFi: Wifi, Parking: Car, Restaurant: UtensilsCrossed, Pool: Waves,
  Gym: Dumbbell, Spa: Sparkles, 'Room Service': BellRing, 'Prayer Room': Star,
  'Halal Kitchen': ChefHat, 'Rooftop Terrace': Building2, 'City View': Landmark, Garden: Trees,
};

const AmenitiesList = ({ amenities = [] }) => {
  return (
    <div className={styles.grid}>
      {amenities.map((amenity) => {
        const Icon = iconMap[amenity] || Star;
        return (
          <div key={amenity} className={styles.item}>
            <div className={styles.iconWrap}><Icon size={18} /></div>
            <span>{amenity}</span>
          </div>
        );
      })}
    </div>
  );
};

export default AmenitiesList;
