package com.swer313.projectstep1.auth;

import com.swer313.projectstep1.booking.Booking;
import com.swer313.projectstep1.booking.BookingRepository;
import com.swer313.projectstep1.booking.BookingStatus;
import com.swer313.projectstep1.catalog.amenities.Amenity;
import com.swer313.projectstep1.catalog.amenities.AmenityRepository;
import com.swer313.projectstep1.catalog.hotel.Hotel;
import com.swer313.projectstep1.catalog.hotel.HotelImage;
import com.swer313.projectstep1.catalog.hotel.HotelImageRepository;
import com.swer313.projectstep1.catalog.hotel.HotelRepository;
import com.swer313.projectstep1.catalog.room.BedType;
import com.swer313.projectstep1.catalog.room.RoomType;
import com.swer313.projectstep1.catalog.room.RoomTypeRepository;
import com.swer313.projectstep1.catalog.room.RoomTypeStatus;
import com.swer313.projectstep1.notification.Notification;
import com.swer313.projectstep1.notification.NotificationRepository;
import com.swer313.projectstep1.notification.NotificationStatus;
import com.swer313.projectstep1.notification.NotificationType;
import com.swer313.projectstep1.notification.ReferenceType;
import com.swer313.projectstep1.payment.Payment;
import com.swer313.projectstep1.payment.PaymentMethod;
import com.swer313.projectstep1.payment.PaymentRepository;
import com.swer313.projectstep1.payment.PaymentStatus;
import com.swer313.projectstep1.availabilitypricing.pricing.PricingRule;
import com.swer313.projectstep1.availabilitypricing.pricing.PricingRuleRepository;
import com.swer313.projectstep1.review.Review;
import com.swer313.projectstep1.review.ReviewRepository;
import com.swer313.projectstep1.user.User;
import com.swer313.projectstep1.user.UserRepository;
import com.swer313.projectstep1.user.UserRole;
import com.swer313.projectstep1.waitinglist.WaitingListEntry;
import com.swer313.projectstep1.waitinglist.WaitingListRepository;
import com.swer313.projectstep1.waitinglist.WaitingListStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * ════════════════════════════════════════════════════════════════════════════
 *  COMPREHENSIVE DATA SEEDER — PinkFlow Hotel System
 * ════════════════════════════════════════════════════════════════════════════
 *
 *  يشتغل فقط بروفايل "dev". ينشئ داتا شاملة تغطي كل حالات التست:
 *
 *  USERS
 *  ├── 1 Admin
 *  ├── 3 Managers  (كل واحد مسؤول عن فندق/فنادق مختلفة)
 *  └── 5 Guests    (بيانات متنوعة)
 *
 *  CATALOG
 *  ├── Amenities  (مختلفة الأنواع: premium + regular, active + inactive)
 *  ├── Hotels     (3 فنادق: active, active, inactive)
 *  └── RoomTypes  (لكل فندق عدة أنواع: KING/QUEEN/TWIN, ACTIVE/INACTIVE)
 *
 *  BOOKINGS  — كل الـ statuses
 *  ├── PENDING    (حجز جديد لم يُأكد)
 *  ├── CONFIRMED  (مؤكد ومدفوع)
 *  ├── CANCELLED  (ملغى بسبب معروف)
 *  └── COMPLETED  (انتهى وأمكن تقييمه)
 *
 *  PAYMENTS  — كل الـ statuses
 *  ├── PENDING  → حجز PENDING
 *  ├── SUCCESS  → حجز CONFIRMED
 *  ├── FAILED   → حجز لا يزال PENDING (محاولة فاشلة)
 *  └── REFUNDED → حجز CANCELLED
 *
 *  REVIEWS   — مرتبطة بحجوزات COMPLETED فقط
 *  ├── تقييم ممتاز (5 نجوم)
 *  ├── تقييم متوسط (3 نجوم)
 *  └── تقييم سيئ  (1 نجمة)
 *
 *  WAITING LIST  — كل الـ statuses
 *  ├── WAITING   (ينتظر)
 *  ├── NOTIFIED  (أُشعر وعنده 24 ساعة)
 *  ├── EXPIRED   (انتهت المدة)
 *  └── CANCELLED (ألغى بنفسه)
 *
 *  NOTIFICATIONS — كل الأنواع
 *  ├── BOOKING_CONFIRMED, BOOKING_CANCELLED, BOOKING_PENDING
 *  ├── PAYMENT_SUCCESS, PAYMENT_FAILED, PAYMENT_REFUNDED
 *  ├── BOOKING_REMINDER, REVIEW_REMINDER
 *  ├── ROOM_AVAILABLE, WELCOME_EMAIL
 *  └── CUSTOM
 *
 *  PRICING RULES
 *  ├── موسم الذروة  (multiplier 1.5)
 *  ├── موسم الصيف   (multiplier 1.3)
 *  ├── خصم الشتاء   (multiplier 0.8)
 *  └── rule منتهي   (active=false)
 *
 *  بيانات الدخول:
 *  ┌────────────────────────────────┬─────────────────┬──────────────┐
 *  │ Email                          │ Password        │ Role         │
 *  ├────────────────────────────────┼─────────────────┼──────────────┤
 *  │ admin@hotel.com                │ Admin@1234      │ ADMIN        │
 *  │ manager1@hotel.com             │ Manager@1234    │ MANAGER      │
 *  │ manager2@hotel.com             │ Manager@1234    │ MANAGER      │
 *  │ manager3@hotel.com             │ Manager@1234    │ MANAGER      │
 *  │ guest1@example.com             │ Guest@1234      │ GUEST        │
 *  │ guest2@example.com             │ Guest@1234      │ GUEST        │
 *  │ guest3@example.com             │ Guest@1234      │ GUEST        │
 *  │ guest4@example.com             │ Guest@1234      │ GUEST        │
 *  │ guest5@example.com             │ Guest@1234      │ GUEST        │
 *  └────────────────────────────────┴─────────────────┴──────────────┘
 */
@Profile("dev")
@Component
public class LoadDatabase implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final UserRepository         userRepository;
    private final HotelRepository        hotelRepository;
    private final HotelImageRepository   hotelImageRepository;
    private final AmenityRepository      amenityRepository;
    private final RoomTypeRepository     roomTypeRepository;
    private final BookingRepository      bookingRepository;
    private final PaymentRepository      paymentRepository;
    private final ReviewRepository       reviewRepository;
    private final WaitingListRepository  waitingListRepository;
    private final NotificationRepository notificationRepository;
    private final PricingRuleRepository  pricingRuleRepository;
    private final PasswordEncoder        passwordEncoder;

    public LoadDatabase(UserRepository userRepository,
                      HotelRepository hotelRepository,
                      HotelImageRepository hotelImageRepository,
                      AmenityRepository amenityRepository,
                      RoomTypeRepository roomTypeRepository,
                      BookingRepository bookingRepository,
                      PaymentRepository paymentRepository,
                      ReviewRepository reviewRepository,
                      WaitingListRepository waitingListRepository,
                      NotificationRepository notificationRepository,
                      PricingRuleRepository pricingRuleRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository         = userRepository;
        this.hotelRepository        = hotelRepository;
        this.hotelImageRepository   = hotelImageRepository;
        this.amenityRepository      = amenityRepository;
        this.roomTypeRepository     = roomTypeRepository;
        this.bookingRepository      = bookingRepository;
        this.paymentRepository      = paymentRepository;
        this.reviewRepository       = reviewRepository;
        this.waitingListRepository  = waitingListRepository;
        this.notificationRepository = notificationRepository;
        this.pricingRuleRepository  = pricingRuleRepository;
        this.passwordEncoder        = passwordEncoder;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ENTRY POINT
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public void run(String... args) {
        log.info("╔══════════════════════════════════════════╗");
        log.info("║        PinkFlow Data Seeder START        ║");
        log.info("╚══════════════════════════════════════════╝");

        // ── الترتيب مهم (Foreign Keys) ────────────────────────────────────
        List<User>      users       = seedUsers();
        List<Amenity>   amenities   = seedAmenities();
        List<Hotel>     hotels      = seedHotels(users, amenities);
        seedHotelImages(hotels);
        List<RoomType>  roomTypes   = seedRoomTypes(hotels, amenities);
        seedPricingRules();
        List<Booking>   bookings    = seedBookings(roomTypes);
        List<Payment>   payments    = seedPayments(bookings);
        seedReviews(bookings, hotels);
        seedWaitingList(roomTypes, hotels);
        seedNotifications(bookings, payments);

        log.info("╔══════════════════════════════════════════╗");
        log.info("║        PinkFlow Data Seeder DONE ✅       ║");
        log.info("╚══════════════════════════════════════════╝");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  1. USERS
    // ═════════════════════════════════════════════════════════════════════════
    private User createUserIfMissing(String fullName,
                                     String email,
                                     String passwordHash,
                                     String phone,
                                     UserRole role,
                                     boolean active) {

        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User(fullName, email, passwordHash, phone, role);
            user.setActive(active);
            return userRepository.save(user);
        });
    }
    private List<User> seedUsers() {
        String adminPwd   = passwordEncoder.encode("Admin@1234");
        String managerPwd = passwordEncoder.encode("Manager@1234");
        String guestPwd   = passwordEncoder.encode("Guest@1234");

        createUserIfMissing("System Admin", "admin@hotel.com",
                adminPwd, "+1-800-555-0001", UserRole.ADMIN, true);

        createUserIfMissing("Sarah Johnson", "manager1@hotel.com",
                managerPwd, "+1-800-555-0101", UserRole.MANAGER, true);

        createUserIfMissing("Ali Hassan", "manager2@hotel.com",
                managerPwd, "+962-79-555-0202", UserRole.MANAGER, true);

        createUserIfMissing("Maria Gonzalez", "manager3@hotel.com",
                managerPwd, "+34-91-555-0303", UserRole.MANAGER, true);

        createUserIfMissing("Ahmed Al-Rashid", "guest1@example.com",
                guestPwd, "+962-79-100-0001", UserRole.GUEST, true);

        createUserIfMissing("Emily Clarke", "guest2@example.com",
                guestPwd, "+44-20-7946-0002", UserRole.GUEST, true);

        createUserIfMissing("Yuki Tanaka", "guest3@example.com",
                guestPwd, "+81-3-5550-0003", UserRole.GUEST, true);

        createUserIfMissing("Carlos Mendez", "guest4@example.com",
                guestPwd, "+52-55-5550-0004", UserRole.GUEST, true);

        createUserIfMissing("Blocked User", "guest5@example.com",
                guestPwd, "+1-555-000-0005", UserRole.GUEST, false);

        log.info("✅ Users seeded/verified individually.");
        return userRepository.findAll();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  2. AMENITIES
    // ═════════════════════════════════════════════════════════════════════════

    private List<Amenity> seedAmenities() {
        if (amenityRepository.count() > 0) {
            log.info("⏭  Amenities already exist — skipping.");
            return amenityRepository.findAll();
        }

        // CONNECTIVITY
        Amenity wifi = amenity("High-Speed WiFi",
                "Fiber optic internet up to 500 Mbps throughout the property",
                Amenity.AmenityCategory.CONNECTIVITY, false, true);
        Amenity smartTv = amenity("Smart TV with Netflix",
                "65-inch 4K Smart TV with Netflix, YouTube and streaming apps",
                Amenity.AmenityCategory.ENTERTAINMENT, false, true);

        // WELLNESS
        Amenity spa = amenity("Luxury Spa & Wellness Center",
                "Full-service spa with massages, sauna, steam room and treatments",
                Amenity.AmenityCategory.WELLNESS, true, true);
        Amenity gym = amenity("Fully-Equipped Gym",
                "24/7 fitness center with modern equipment and personal trainers",
                Amenity.AmenityCategory.WELLNESS, false, true);
        Amenity pool = amenity("Outdoor Swimming Pool",
                "Heated outdoor pool open year-round with pool service",
                Amenity.AmenityCategory.OUTDOOR, false, true);

        // DINING
        Amenity breakfast = amenity("Complimentary Breakfast",
                "Daily buffet breakfast with international and local options",
                Amenity.AmenityCategory.DINING, false, true);
        Amenity roomService = amenity("24/7 Room Service",
                "Round-the-clock in-room dining from our full restaurant menu",
                Amenity.AmenityCategory.DINING, false, true);
        Amenity minibar = amenity("Premium Minibar",
                "Fully stocked minibar with premium drinks and snacks",
                Amenity.AmenityCategory.DINING, true, true);

        // COMFORT
        Amenity ac = amenity("Air Conditioning & Heating",
                "Individual climate control with smart thermostat in each room",
                Amenity.AmenityCategory.COMFORT, false, true);
        Amenity balcony = amenity("Private Balcony",
                "Furnished private balcony with panoramic views",
                Amenity.AmenityCategory.OUTDOOR, false, true);

        // PARKING
        Amenity parking = amenity("Free Valet Parking",
                "Complimentary valet parking service available 24/7",
                Amenity.AmenityCategory.PARKING, false, true);

        // SECURITY
        Amenity safe = amenity("In-Room Safe",
                "Electronic in-room safe large enough for a laptop",
                Amenity.AmenityCategory.SECURITY, false, true);

        // INACTIVE amenity — لتست الفلترة والـ inactive state
        Amenity oldGym = amenity("Old Gym (Closed for Renovation)",
                "Temporarily closed gym area under major renovation",
                Amenity.AmenityCategory.WELLNESS, false, false);

        List<Amenity> saved = amenityRepository.saveAll(List.of(
                wifi, smartTv, spa, gym, pool,
                breakfast, roomService, minibar, ac, balcony,
                parking, safe, oldGym));
        log.info("✅ Amenities created: {} (12 active, 1 inactive)", saved.size());
        return saved;
    }

    private void seedHotelImages(List<Hotel> hotels) {
        int added = 0;

        for (Hotel hotel : hotels) {
            if (hotel == null || hotel.getId() == null) {
                continue;
            }

            if (!hotelImageRepository.findByHotelId(hotel.getId()).isEmpty()) {
                continue;
            }

            List<HotelImage> images = demoHotelImages(hotel.getName()).stream()
                    .map(url -> hotelImage(hotel, url))
                    .toList();

            hotelImageRepository.saveAll(images);
            added += images.size();
        }

        log.info("✅ Hotel demo images seeded/verified. Added {} image(s).", added);
    }

    private HotelImage hotelImage(Hotel hotel, String imageUrl) {
        HotelImage image = new HotelImage();
        image.setHotel(hotel);
        image.setImageUrl(imageUrl);
        image.setFileName(imageUrl.substring(imageUrl.lastIndexOf('/') + 1));
        return image;
    }

    private List<String> demoHotelImages(String hotelName) {
        if ("The Grand Amman Palace".equals(hotelName)) {
            return List.of(
                    "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1200&q=80"
            );
        }

        if ("Petra Desert Lodge".equals(hotelName)) {
            return List.of(
                    "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=1200&q=80"
            );
        }

        if ("Dead Sea Horizon Resort".equals(hotelName)) {
            return List.of(
                    "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=1200&q=80"
            );
        }

        return List.of();
    }

    private Amenity amenity(String name, String desc,
                            Amenity.AmenityCategory cat, boolean premium, boolean active) {
        Amenity a = new Amenity(name, desc, cat);
        a.setPremium(premium);
        a.setActive(active);
        return a;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  3. HOTELS
    // ═════════════════════════════════════════════════════════════════════════

    private List<Hotel> seedHotels(List<User> users, List<Amenity> amenities) {
        if (hotelRepository.count() > 0) {
            log.info("⏭  Hotels already exist — skipping.");
            return hotelRepository.findAll();
        }

        // نحضّر الـ managers والـ amenities
        User mgr1 = users.stream().filter(u -> "manager1@hotel.com".equals(u.getEmail())).findFirst().orElseThrow();
        User mgr2 = users.stream().filter(u -> "manager2@hotel.com".equals(u.getEmail())).findFirst().orElseThrow();
        User mgr3 = users.stream().filter(u -> "manager3@hotel.com".equals(u.getEmail())).findFirst().orElseThrow();

        Amenity wifi       = findAmenity(amenities, "High-Speed WiFi");
        Amenity spa        = findAmenity(amenities, "Luxury Spa & Wellness Center");
        Amenity pool       = findAmenity(amenities, "Outdoor Swimming Pool");
        Amenity gym        = findAmenity(amenities, "Fully-Equipped Gym");
        Amenity breakfast  = findAmenity(amenities, "Complimentary Breakfast");
        Amenity parking    = findAmenity(amenities, "Free Valet Parking");
        Amenity smartTv    = findAmenity(amenities, "Smart TV with Netflix");

        // ── Hotel 1: فاخر ونشيط ───────────────────────────────────────────
        Hotel h1 = new Hotel();
        h1.setName("The Grand Amman Palace");
        h1.setAddress("King Abdullah II St, Amman, Jordan 11118");
        h1.setDescription("A 5-star luxury hotel in the heart of Amman offering " +
                "breathtaking views of the city skyline and world-class amenities.");
        h1.setCity("Amman");
        h1.setCountry("Jordan");
        h1.setPhoneNumber("+962-6-550-8888");
        h1.setEmail("reservations@grandamman.com");
        h1.setWebsiteUrl("https://www.grandamman.com");
        h1.setRating(4.8);
        h1.setLatitude(31.9539);
        h1.setLongitude(35.9106);
        h1.setCheckInTime(LocalTime.of(14, 0));
        h1.setCheckOutTime(LocalTime.of(12, 0));
        h1.setPolicies("No smoking. No pets. Children under 12 stay free.");
        h1.setCancellationPolicySummary("Free cancellation up to 48 hours before check-in.");
        h1.setStatus(Hotel.Status.ACTIVE);
        h1.setAmenities(Set.of(wifi, spa, pool, gym, breakfast, parking));

        // ── Hotel 2: متوسط ونشيط ─────────────────────────────────────────
        Hotel h2 = new Hotel();
        h2.setName("Petra Desert Lodge");
        h2.setAddress("Tourism Street, Wadi Musa, Jordan 71810");
        h2.setDescription("Boutique lodge near the ancient city of Petra, " +
                "offering authentic Jordanian hospitality and desert views.");
        h2.setCity("Wadi Musa");
        h2.setCountry("Jordan");
        h2.setPhoneNumber("+962-3-215-7111");
        h2.setEmail("info@petralodge.com");
        h2.setWebsiteUrl("https://www.petralodge.com");
        h2.setRating(4.2);
        h2.setLatitude(30.3285);
        h2.setLongitude(35.4444);
        h2.setCheckInTime(LocalTime.of(15, 0));
        h2.setCheckOutTime(LocalTime.of(11, 0));
        h2.setPolicies("Quiet hours after 11 PM. No smoking in rooms.");
        h2.setCancellationPolicySummary("Free cancellation up to 24 hours before check-in.");
        h2.setStatus(Hotel.Status.ACTIVE);
        h2.setAmenities(Set.of(wifi, breakfast, parking, smartTv));

        // ── Hotel 3: مغلق/غير نشيط — لتست الـ inactive hotels ────────────
        Hotel h3 = new Hotel();
        h3.setName("Dead Sea Horizon Resort");
        h3.setAddress("Dead Sea Road, Sweimeh, Jordan 19711");
        h3.setDescription("Luxury resort on the shores of the Dead Sea, " +
                "currently under renovation. Will reopen soon.");
        h3.setCity("Sweimeh");
        h3.setCountry("Jordan");
        h3.setPhoneNumber("+962-5-356-1234");
        h3.setEmail("info@deadsearesort.com");
        h3.setRating(4.5);
        h3.setLatitude(31.7220);
        h3.setLongitude(35.5731);
        h3.setCheckInTime(LocalTime.of(14, 0));
        h3.setCheckOutTime(LocalTime.of(12, 0));
        h3.setPolicies("Currently closed for renovation.");
        h3.setCancellationPolicySummary("All existing bookings will be fully refunded.");
        h3.setStatus(Hotel.Status.INACTIVE);
        h3.setAmenities(Set.of(wifi, spa, pool));

        List<Hotel> saved = hotelRepository.saveAll(List.of(h1, h2, h3));

        // ── ربط المانجرز بالفنادق ─────────────────────────────────────────
        // mgr1 يدير الفندق الأول
        mgr1.addManagedHotel(saved.get(0));
        // mgr2 يدير الفندق الثاني
        mgr2.addManagedHotel(saved.get(1));
        // mgr3 يدير فندقين (الأول والثالث) — لتست الـ multi-hotel manager
        mgr3.addManagedHotel(saved.get(0));
        mgr3.addManagedHotel(saved.get(2));
        userRepository.saveAll(List.of(mgr1, mgr2, mgr3));

        log.info("✅ Hotels created: 2 active, 1 inactive. Managers assigned.");
        return saved;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  4. ROOM TYPES
    // ═════════════════════════════════════════════════════════════════════════

    private List<RoomType> seedRoomTypes(List<Hotel> hotels, List<Amenity> amenities) {
        if (roomTypeRepository.count() > 0) {
            log.info("⏭  RoomTypes already exist — skipping.");
            return roomTypeRepository.findAll();
        }

        Hotel h1 = hotels.get(0); // Grand Amman Palace
        Hotel h2 = hotels.get(1); // Petra Desert Lodge
        Hotel h3 = hotels.get(2); // Dead Sea (inactive hotel)

        Amenity wifi      = findAmenity(amenities, "High-Speed WiFi");
        Amenity ac        = findAmenity(amenities, "Air Conditioning & Heating");
        Amenity minibar   = findAmenity(amenities, "Premium Minibar");
        Amenity balcony   = findAmenity(amenities, "Private Balcony");
        Amenity safe      = findAmenity(amenities, "In-Room Safe");
        Amenity roomSvc   = findAmenity(amenities, "24/7 Room Service");
        Amenity smartTv   = findAmenity(amenities, "Smart TV with Netflix");

        // ── H1 Room Types ─────────────────────────────────────────────────

        // Standard Double — متاح، سعر معقول
        RoomType h1Standard = roomType(h1, "Standard Double", 2,
                BedType.QUEEN, 1, 2, 0,
                new BigDecimal("89.00"), 10,
                "Comfortable room with queen bed and city view.",
                "No smoking. Check-out by 12:00 PM.",
                RoomTypeStatus.ACTIVE,
                Set.of(wifi, ac, smartTv, safe));

        // Deluxe King — متاح، سعر أعلى
        RoomType h1Deluxe = roomType(h1, "Deluxe King Suite", 3,
                BedType.KING, 1, 2, 1,
                new BigDecimal("159.00"), 6,
                "Spacious suite with king bed, balcony and panoramic city views.",
                "No smoking. Late check-out available for an extra fee.",
                RoomTypeStatus.ACTIVE,
                Set.of(wifi, ac, minibar, balcony, safe, roomSvc, smartTv));

        // Twin Family — متاح للعائلات
        RoomType h1Twin = roomType(h1, "Family Twin Room", 4,
                BedType.TWIN, 2, 2, 2,
                new BigDecimal("130.00"), 4,
                "Spacious family room with two queen beds, ideal for families.",
                "Children stay free. Extra bed available.",
                RoomTypeStatus.ACTIVE,
                Set.of(wifi, ac, smartTv, safe));

        // Presidential Suite — مخصص للتست مع أعلى سعر
        RoomType h1Presidential = roomType(h1, "Presidential Suite", 2,
                BedType.KING, 1, 2, 0,
                new BigDecimal("450.00"), 2,
                "The pinnacle of luxury. Private butler, jacuzzi and terrace.",
                "Non-refundable. Special requests must be submitted 72h in advance.",
                RoomTypeStatus.ACTIVE,
                Set.of(wifi, ac, minibar, balcony, safe, roomSvc, smartTv));

        // INACTIVE room type — لتست الـ INACTIVE rooms
        RoomType h1OldRoom = roomType(h1, "Economy Single (Discontinued)", 1,
                BedType.TWIN, 1, 1, 0,
                new BigDecimal("45.00"), 0,
                "Small room with single bed. No longer available.",
                "This room type has been discontinued.",
                RoomTypeStatus.INACTIVE,
                Set.of(wifi));

        // ── H2 Room Types ─────────────────────────────────────────────────

        // Desert View Room
        RoomType h2Desert = roomType(h2, "Desert View Room", 2,
                BedType.QUEEN, 1, 2, 0,
                new BigDecimal("75.00"), 8,
                "Cozy room with stunning desert and Petra mountain views.",
                "No smoking. Quiet hours 11PM-7AM.",
                RoomTypeStatus.ACTIVE,
                Set.of(wifi, ac, smartTv));

        // Bedouin Suite — أكبر وأغلى في H2
        RoomType h2Bedouin = roomType(h2, "Bedouin Heritage Suite", 2,
                BedType.KING, 1, 2, 0,
                new BigDecimal("145.00"), 3,
                "Authentic Bedouin-style suite with handcrafted furniture and private terrace.",
                "Non-refundable within 48h of check-in.",
                RoomTypeStatus.ACTIVE,
                Set.of(wifi, ac, minibar, balcony, roomSvc));

        // ── H3 Room Types (inactive hotel) ───────────────────────────────

        RoomType h3Spa = roomType(h3, "Dead Sea Spa Room", 2,
                BedType.KING, 1, 2, 0,
                new BigDecimal("200.00"), 5,
                "Luxury spa room with direct Dead Sea access.",
                "Currently unavailable — hotel under renovation.",
                RoomTypeStatus.INACTIVE,
                Set.of(wifi, ac, minibar));

        List<RoomType> saved = roomTypeRepository.saveAll(List.of(
                h1Standard, h1Deluxe, h1Twin, h1Presidential, h1OldRoom,
                h2Desert, h2Bedouin,
                h3Spa));
        log.info("✅ RoomTypes created: {} total (2 inactive)", saved.size());
        return saved;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  5. PRICING RULES
    // ═════════════════════════════════════════════════════════════════════════

    private void seedPricingRules() {
        if (pricingRuleRepository.count() > 0) {
            log.info("⏭  PricingRules already exist — skipping.");
            return;
        }

        LocalDate today = LocalDate.now();

        // ── موسم الصيف الحالي (+30%) ─────────────────────────────────────
        PricingRule summer = new PricingRule();
        summer.setName("Summer Season 2026");
        summer.setDescription("Peak summer pricing for June-August 2026");
        summer.setStartDate(LocalDate.of(2026, 6, 1));
        summer.setEndDate(LocalDate.of(2026, 8, 31));
        summer.setPriceMultiplier(new BigDecimal("1.30"));
        summer.setActive(true);

        // ── موسم عيد الميلاد (+50%) ──────────────────────────────────────
        PricingRule xmas = new PricingRule();
        xmas.setName("Holiday Season - Christmas & New Year");
        xmas.setDescription("Peak pricing for Christmas and New Year holiday period");
        xmas.setStartDate(LocalDate.of(2026, 12, 20));
        xmas.setEndDate(LocalDate.of(2027, 1, 5));
        xmas.setPriceMultiplier(new BigDecimal("1.50"));
        xmas.setActive(true);

        // ── خصم الشتاء (-20%) ────────────────────────────────────────────
        PricingRule winter = new PricingRule();
        winter.setName("Winter Discount 2026");
        winter.setDescription("Off-season discount for January-February 2026");
        winter.setStartDate(LocalDate.of(2026, 1, 15));
        winter.setEndDate(LocalDate.of(2026, 2, 28));
        winter.setPriceMultiplier(new BigDecimal("0.80"));
        winter.setActive(true);

        // ── عروض الربيع (active=true, future) ────────────────────────────
        PricingRule spring = new PricingRule();
        spring.setName("Spring Break 2026");
        spring.setDescription("Special pricing for spring break travelers");
        spring.setStartDate(LocalDate.of(2026, 3, 20));
        spring.setEndDate(LocalDate.of(2026, 4, 10));
        spring.setPriceMultiplier(new BigDecimal("1.20"));
        spring.setActive(true);

        // ── Rule منتهي الصلاحية (active=false) — لتست الـ inactive rules
        PricingRule expired = new PricingRule();
        expired.setName("Black Friday 2025 (Expired)");
        expired.setDescription("Black Friday special discount — no longer active");
        expired.setStartDate(LocalDate.of(2025, 11, 28));
        expired.setEndDate(LocalDate.of(2025, 11, 30));
        expired.setPriceMultiplier(new BigDecimal("0.70"));
        expired.setActive(false);

        pricingRuleRepository.saveAll(List.of(summer, xmas, winter, spring, expired));
        log.info("✅ PricingRules created: 4 active, 1 inactive/expired");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  6. BOOKINGS
    // ═════════════════════════════════════════════════════════════════════════

    private List<Booking> seedBookings(List<RoomType> roomTypes) {
        if (bookingRepository.count() > 0) {
            log.info("⏭  Bookings already exist — skipping.");
            return bookingRepository.findAll();
        }

        LocalDate today = LocalDate.now();

        // الـ room types الـ active
        RoomType standard     = findRoomType(roomTypes, "Standard Double");
        RoomType deluxe       = findRoomType(roomTypes, "Deluxe King Suite");
        RoomType twin         = findRoomType(roomTypes, "Family Twin Room");
        RoomType presidential = findRoomType(roomTypes, "Presidential Suite");
        RoomType desert       = findRoomType(roomTypes, "Desert View Room");
        RoomType bedouin      = findRoomType(roomTypes, "Bedouin Heritage Suite");

        // ──────────────────────────────────────────────────────────────────
        // PENDING bookings — حجوزات جديدة، في الوقت الحالي أو المستقبل
        // ──────────────────────────────────────────────────────────────────

        // PENDING #1 — حجز عادي في المستقبل
        Booking pending1 = booking(standard,
                "Ahmed Al-Rashid", "guest1@example.com", "+962-79-100-0001",
                2, 0, today.plusDays(10), today.plusDays(14),
                standard.getBasePrice(), BookingStatus.PENDING,
                "Early check-in requested if possible.");

        // PENDING #2 — حجز مع أطفال
        Booking pending2 = booking(twin,
                "Emily Clarke", "guest2@example.com", "+44-20-7946-0002",
                2, 2, today.plusDays(20), today.plusDays(25),
                twin.getBasePrice(), BookingStatus.PENDING,
                "Need a crib for the baby please.");

        // PENDING #3 — حجز قريب (بكرة)
        Booking pending3 = booking(desert,
                "Carlos Mendez", "guest4@example.com", "+52-55-5550-0004",
                1, 0, today.plusDays(2), today.plusDays(5),
                desert.getBasePrice(), BookingStatus.PENDING,
                null);

        // ──────────────────────────────────────────────────────────────────
        // CONFIRMED bookings — مؤكدة ومدفوعة
        // ──────────────────────────────────────────────────────────────────

        // CONFIRMED #1 — في المستقبل (upcoming)
        Booking confirmed1 = booking(deluxe,
                "Yuki Tanaka", "guest3@example.com", "+81-3-5550-0003",
                2, 0, today.plusDays(7), today.plusDays(10),
                deluxe.getBasePrice(), BookingStatus.CONFIRMED,
                "Honeymoon couple — please decorate room with flowers.");

        // CONFIRMED #2 — حجز طويل في المستقبل
        Booking confirmed2 = booking(twin,
                "Ahmed Al-Rashid", "guest1@example.com", "+962-79-100-0001",
                2, 1, today.plusDays(30), today.plusDays(37),
                twin.getBasePrice(), BookingStatus.CONFIRMED,
                "Business trip — need invoices for all charges.");

        // CONFIRMED #3 — حجز مؤكد في فندق H2 (Petra)
        Booking confirmed3 = booking(bedouin,
                "Emily Clarke", "guest2@example.com", "+44-20-7946-0002",
                2, 0, today.plusDays(15), today.plusDays(18),
                bedouin.getBasePrice(), BookingStatus.CONFIRMED,
                null);

        // CONFIRMED #4 — Presidential Suite (أعلى سعر)
        Booking confirmed4 = booking(presidential,
                "Carlos Mendez", "guest4@example.com", "+52-55-5550-0004",
                2, 0, today.plusDays(45), today.plusDays(48),
                presidential.getBasePrice(), BookingStatus.CONFIRMED,
                "VIP guest. Requires airport transfer and butler service.");

        // ──────────────────────────────────────────────────────────────────
        // CANCELLED bookings — ملغاة بأسباب مختلفة
        // ──────────────────────────────────────────────────────────────────

        // CANCELLED #1 — ألغاها الضيف (حجز سابق)
        Booking cancelled1 = booking(standard,
                "Guest User", "guest2@example.com", "+44-20-7946-0002",
                1, 0, today.minusDays(20), today.minusDays(17),
                standard.getBasePrice(), BookingStatus.CANCELLED,
                null);
        cancelled1.setCancelledAt(LocalDateTime.now().minusDays(22));
        cancelled1.setCancellationReason("Change of travel plans due to work obligations.");
        cancelled1.setRefundAmount(standard.getBasePrice().multiply(new BigDecimal("3")));

        // CANCELLED #2 — ألغاها المانجر (overbooking)
        Booking cancelled2 = booking(deluxe,
                "Yuki Tanaka", "guest3@example.com", "+81-3-5550-0003",
                2, 0, today.minusDays(10), today.minusDays(8),
                deluxe.getBasePrice(), BookingStatus.CANCELLED,
                null);
        cancelled2.setCancelledAt(LocalDateTime.now().minusDays(12));
        cancelled2.setCancellationReason("Cancelled by manager due to unexpected room maintenance.");
        cancelled2.setRefundAmount(deluxe.getBasePrice().multiply(new BigDecimal("2")));

        // CANCELLED #3 — ملغى بدون رسوم (إلغاء مبكر)
        Booking cancelled3 = booking(desert,
                "Ahmed Al-Rashid", "guest1@example.com", "+962-79-100-0001",
                1, 0, today.plusDays(60), today.plusDays(63),
                desert.getBasePrice(), BookingStatus.CANCELLED,
                null);
        cancelled3.setCancelledAt(LocalDateTime.now());
        cancelled3.setCancellationReason("Guest cancelled early — full refund applied.");
        cancelled3.setRefundAmount(desert.getBasePrice().multiply(new BigDecimal("3")));

        // ──────────────────────────────────────────────────────────────────
        // COMPLETED bookings — انتهت وممكن تكتب ريفيو
        // ──────────────────────────────────────────────────────────────────

        // COMPLETED #1 — ضيف 1 في الفندق الأول (سيكتب review ممتاز)
        Booking completed1 = booking(standard,
                "Ahmed Al-Rashid", "guest1@example.com", "+962-79-100-0001",
                2, 0, today.minusDays(30), today.minusDays(27),
                standard.getBasePrice(), BookingStatus.COMPLETED,
                null);

        // COMPLETED #2 — ضيف 2 في الفندق الأول (سيكتب review متوسط)
        Booking completed2 = booking(deluxe,
                "Emily Clarke", "guest2@example.com", "+44-20-7946-0002",
                2, 0, today.minusDays(45), today.minusDays(42),
                deluxe.getBasePrice(), BookingStatus.COMPLETED,
                null);

        // COMPLETED #3 — ضيف 3 في فندق Petra (سيكتب review سيئ)
        Booking completed3 = booking(desert,
                "Yuki Tanaka", "guest3@example.com", "+81-3-5550-0003",
                1, 0, today.minusDays(60), today.minusDays(57),
                desert.getBasePrice(), BookingStatus.COMPLETED,
                null);

        // COMPLETED #4 — ضيف 4 في الفندق الأول (بدون review — لتست missing review)
        Booking completed4 = booking(twin,
                "Carlos Mendez", "guest4@example.com", "+52-55-5550-0004",
                2, 1, today.minusDays(15), today.minusDays(12),
                twin.getBasePrice(), BookingStatus.COMPLETED,
                null);

        List<Booking> saved = bookingRepository.saveAll(List.of(
                pending1, pending2, pending3,
                confirmed1, confirmed2, confirmed3, confirmed4,
                cancelled1, cancelled2, cancelled3,
                completed1, completed2, completed3, completed4));

        log.info("✅ Bookings created: 3 PENDING, 4 CONFIRMED, 3 CANCELLED, 4 COMPLETED");
        return saved;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  7. PAYMENTS
    // ═════════════════════════════════════════════════════════════════════════

    private List<Payment> seedPayments(List<Booking> bookings) {
        if (paymentRepository.count() > 0) {
            log.info("⏭  Payments already exist — skipping.");
            return paymentRepository.findAll();
        }

        // نجيب الحجوزات بالاسم من الـ list
        Booking pending1   = findBooking(bookings, "guest1@example.com", BookingStatus.PENDING, 0);
        Booking pending2   = findBooking(bookings, "guest2@example.com", BookingStatus.PENDING, 0);
        Booking pending3   = findBooking(bookings, "guest4@example.com", BookingStatus.PENDING, 0);
        Booking confirmed1 = findBooking(bookings, "guest3@example.com", BookingStatus.CONFIRMED, 0);
        Booking confirmed2 = findBooking(bookings, "guest1@example.com", BookingStatus.CONFIRMED, 0);
        Booking confirmed3 = findBooking(bookings, "guest2@example.com", BookingStatus.CONFIRMED, 0);
        Booking confirmed4 = findBooking(bookings, "guest4@example.com", BookingStatus.CONFIRMED, 0);
        Booking cancelled1 = findBooking(bookings, "guest2@example.com", BookingStatus.CANCELLED, 0);
        Booking cancelled2 = findBooking(bookings, "guest3@example.com", BookingStatus.CANCELLED, 0);
        Booking completed1 = findBooking(bookings, "guest1@example.com", BookingStatus.COMPLETED, 0);
        Booking completed2 = findBooking(bookings, "guest2@example.com", BookingStatus.COMPLETED, 1);
        Booking completed3 = findBooking(bookings, "guest3@example.com", BookingStatus.COMPLETED, 0);
        Booking completed4 = findBooking(bookings, "guest4@example.com", BookingStatus.COMPLETED, 0);

        // ── PENDING payments ──────────────────────────────────────────────

        // Payment PENDING للـ pending1 (لم يدفع بعد)
        Payment pp1 = payment(pending1.getId(), pending1.getTotalPrice(),
                PaymentStatus.PENDING, null, null, null);

        // Payment PENDING للـ pending2
        Payment pp2 = payment(pending2.getId(), pending2.getTotalPrice(),
                PaymentStatus.PENDING, null, null, null);

        // Payment PENDING للـ pending3
        Payment pp3 = payment(pending3.getId(), pending3.getTotalPrice(),
                PaymentStatus.PENDING, null, null, null);

        // ── FAILED then PENDING (محاولة فاشلة ثم محاولة جديدة pending) ──

        // محاولة فاشلة لحجز pending1
        Payment failedAttempt = payment(pending1.getId(), pending1.getTotalPrice(),
                PaymentStatus.FAILED,
                "Card declined: insufficient funds",
                null,
                LocalDateTime.now().minusHours(2));

        // ── SUCCESS payments ──────────────────────────────────────────────

        Payment sp1 = payment(confirmed1.getId(), confirmed1.getTotalPrice(),
                PaymentStatus.SUCCESS, null,
                LocalDateTime.now().minusDays(8), null);

        Payment sp2 = payment(confirmed2.getId(), confirmed2.getTotalPrice(),
                PaymentStatus.SUCCESS, null,
                LocalDateTime.now().minusDays(5), null);

        Payment sp3 = payment(confirmed3.getId(), confirmed3.getTotalPrice(),
                PaymentStatus.SUCCESS, null,
                LocalDateTime.now().minusDays(16), null);

        Payment sp4 = payment(confirmed4.getId(), confirmed4.getTotalPrice(),
                PaymentStatus.SUCCESS, null,
                LocalDateTime.now().minusDays(3), null);

        // COMPLETED booking payments
        Payment cp1 = payment(completed1.getId(), completed1.getTotalPrice(),
                PaymentStatus.SUCCESS, null,
                LocalDateTime.now().minusDays(32), null);

        Payment cp2 = payment(completed2.getId(), completed2.getTotalPrice(),
                PaymentStatus.SUCCESS, null,
                LocalDateTime.now().minusDays(47), null);

        Payment cp3 = payment(completed3.getId(), completed3.getTotalPrice(),
                PaymentStatus.SUCCESS, null,
                LocalDateTime.now().minusDays(62), null);

        Payment cp4 = payment(completed4.getId(), completed4.getTotalPrice(),
                PaymentStatus.SUCCESS, null,
                LocalDateTime.now().minusDays(17), null);

        // ── REFUNDED payments ─────────────────────────────────────────────

        // Refund للـ cancelled1
        Payment rp1 = payment(cancelled1.getId(), cancelled1.getTotalPrice(),
                PaymentStatus.REFUNDED, null,
                LocalDateTime.now().minusDays(23), null);
        rp1.setRefundReason("Guest cancelled — full refund per policy.");
        rp1.setRefundedAt(LocalDateTime.now().minusDays(22));

        // Refund للـ cancelled2
        Payment rp2 = payment(cancelled2.getId(), cancelled2.getTotalPrice(),
                PaymentStatus.REFUNDED, null,
                LocalDateTime.now().minusDays(13), null);
        rp2.setRefundReason("Manager cancelled due to room maintenance issue — full refund.");
        rp2.setRefundedAt(LocalDateTime.now().minusDays(11));

        List<Payment> saved = paymentRepository.saveAll(List.of(
                pp1, pp2, pp3,
                failedAttempt,
                sp1, sp2, sp3, sp4,
                cp1, cp2, cp3, cp4,
                rp1, rp2));

        log.info("✅ Payments created: 3 PENDING, 1 FAILED, 8 SUCCESS, 2 REFUNDED");
        return saved;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  8. REVIEWS
    // ═════════════════════════════════════════════════════════════════════════

    private void seedReviews(List<Booking> bookings, List<Hotel> hotels) {
        if (reviewRepository.count() > 0) {
            log.info("⏭  Reviews already exist — skipping.");
            return;
        }

        Hotel h1 = hotels.get(0); // Grand Amman Palace
        Hotel h2 = hotels.get(1); // Petra Desert Lodge

        Booking completed1 = findBooking(bookings, "guest1@example.com", BookingStatus.COMPLETED, 0);
        Booking completed2 = findBooking(bookings, "guest2@example.com", BookingStatus.COMPLETED, 1);
        Booking completed3 = findBooking(bookings, "guest3@example.com", BookingStatus.COMPLETED, 0);
        // completed4 (guest4) — intentionally left without review

        // ── Review 1: ممتاز 5/5 ──────────────────────────────────────────
        Review r1 = new Review();
        r1.setBooking(completed1);
        r1.setHotelId(h1.getId());
        r1.setGuestEmail("guest1@example.com");
        r1.setRating(5);
        r1.setComment("Absolutely outstanding! The staff were incredibly attentive, " +
                "the room was immaculate, and the breakfast was divine. " +
                "Will definitely return. Best hotel stay in years!");

        // ── Review 2: متوسط 3/5 ──────────────────────────────────────────
        Review r2 = new Review();
        r2.setBooking(completed2);
        r2.setHotelId(h1.getId());
        r2.setGuestEmail("guest2@example.com");
        r2.setRating(3);
        r2.setComment("Good hotel overall but the room was smaller than advertised. " +
                "The view was nice but AC was noisy at night. " +
                "Room service was slow (45 min wait). Decent for the price.");

        // ── Review 3: سيئ 1/5 ────────────────────────────────────────────
        Review r3 = new Review();
        r3.setBooking(completed3);
        r3.setHotelId(h2.getId());
        r3.setGuestEmail("guest3@example.com");
        r3.setRating(1);
        r3.setComment("Very disappointing experience. Hot water was not working for 2 days, " +
                "WiFi was extremely slow, and the staff was unhelpful. " +
                "Would not recommend at this price point.");

        reviewRepository.saveAll(List.of(r1, r2, r3));
        log.info("✅ Reviews created: 3 (5★, 3★, 1★). 1 completed booking without review.");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  9. WAITING LIST
    // ═════════════════════════════════════════════════════════════════════════

    private void seedWaitingList(List<RoomType> roomTypes, List<Hotel> hotels) {
        if (waitingListRepository.count() > 0) {
            log.info("⏭  WaitingList already exist — skipping.");
            return;
        }

        LocalDate today = LocalDate.now();

        Hotel h1 = hotels.get(0);
        Hotel h2 = hotels.get(1);
        RoomType standard     = findRoomType(roomTypes, "Standard Double");
        RoomType deluxe       = findRoomType(roomTypes, "Deluxe King Suite");
        RoomType presidential = findRoomType(roomTypes, "Presidential Suite");
        RoomType desert       = findRoomType(roomTypes, "Desert View Room");

        // ── WAITING — ينتظر ──────────────────────────────────────────────
        WaitingListEntry wl1 = waitingEntry(
                standard, h1,
                "guest3@example.com", "Yuki Tanaka",
                today.plusDays(10), today.plusDays(14),
                WaitingListStatus.WAITING, null,
                "Standard Double", h1.getName());

        WaitingListEntry wl2 = waitingEntry(
                presidential, h1,
                "guest4@example.com", "Carlos Mendez",
                today.plusDays(5), today.plusDays(8),
                WaitingListStatus.WAITING, null,
                "Presidential Suite", h1.getName());

        WaitingListEntry wl3 = waitingEntry(
                desert, h2,
                "guest1@example.com", "Ahmed Al-Rashid",
                today.plusDays(20), today.plusDays(24),
                WaitingListStatus.WAITING, null,
                "Desert View Room", h2.getName());

        // ── NOTIFIED — أُشعر وعنده 24 ساعة (أُشعر قبل ساعتين) ───────────
        WaitingListEntry wl4 = waitingEntry(
                deluxe, h1,
                "guest2@example.com", "Emily Clarke",
                today.plusDays(7), today.plusDays(10),
                WaitingListStatus.NOTIFIED,
                LocalDateTime.now().minusHours(2), // notifiedAt — 22 ساعة باقية
                "Deluxe King Suite", h1.getName());

        // ── EXPIRED — انتهت المدة (أُشعر قبل 25 ساعة = منتهي) ───────────
        WaitingListEntry wl5 = waitingEntry(
                standard, h1,
                "guest4@example.com", "Carlos Mendez",
                today.plusDays(3), today.plusDays(6),
                WaitingListStatus.EXPIRED,
                LocalDateTime.now().minusHours(25), // notifiedAt — فات الـ 24 ساعة
                "Standard Double", h1.getName());

        // ── EXPIRED — تاريخ الحجز فات ─────────────────────────────────
        WaitingListEntry wl6 = waitingEntry(
                desert, h2,
                "guest3@example.com", "Yuki Tanaka",
                today.minusDays(10), today.minusDays(7),
                WaitingListStatus.EXPIRED, null,
                "Desert View Room", h2.getName());

        // ── CANCELLED — ألغى بنفسه ─────────────────────────────────────
        WaitingListEntry wl7 = waitingEntry(
                standard, h1,
                "guest1@example.com", "Ahmed Al-Rashid",
                today.plusDays(15), today.plusDays(18),
                WaitingListStatus.CANCELLED, null,
                "Standard Double", h1.getName());

        waitingListRepository.saveAll(List.of(wl1, wl2, wl3, wl4, wl5, wl6, wl7));
        log.info("✅ WaitingList created: 3 WAITING, 1 NOTIFIED, 2 EXPIRED, 1 CANCELLED");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  10. NOTIFICATIONS
    // ═════════════════════════════════════════════════════════════════════════

    private void seedNotifications(List<Booking> bookings, List<Payment> payments) {
        if (notificationRepository.count() > 0) {
            log.info("⏭  Notifications already exist — skipping.");
            return;
        }

        Booking confirmed1 = findBooking(bookings, "guest3@example.com", BookingStatus.CONFIRMED, 0);
        Booking cancelled1 = findBooking(bookings, "guest2@example.com", BookingStatus.CANCELLED, 0);
        Booking completed1 = findBooking(bookings, "guest1@example.com", BookingStatus.COMPLETED, 0);
        Booking pending1   = findBooking(bookings, "guest1@example.com", BookingStatus.PENDING, 0);

        Payment sp1 = findPayment(payments, confirmed1.getId(), PaymentStatus.SUCCESS);
        Payment rp1 = findPayment(payments, cancelled1.getId(), PaymentStatus.REFUNDED);

        // ── BOOKING_CONFIRMED — SENT ───────────────────────────────────────
        Notification n1 = notif(
                "guest3@example.com", "Yuki Tanaka",
                NotificationType.BOOKING_CONFIRMED, NotificationStatus.SENT,
                "Booking Confirmed — Grand Amman Palace",
                buildBookingConfirmedBody("Yuki Tanaka", confirmed1),
                confirmed1.getId(), ReferenceType.BOOKING,
                LocalDateTime.now().minusDays(8), LocalDateTime.now().minusDays(8));

        // ── BOOKING_PENDING — SENT ─────────────────────────────────────────
        Notification n2 = notif(
                "guest1@example.com", "Ahmed Al-Rashid",
                NotificationType.BOOKING_PENDING, NotificationStatus.SENT,
                "Booking Request Received",
                "Dear Ahmed Al-Rashid, your booking request #" + pending1.getId() +
                        " has been received and is pending confirmation. " +
                        "You will be notified once it is confirmed.",
                pending1.getId(), ReferenceType.BOOKING,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1));

        // ── BOOKING_CANCELLED — SENT ───────────────────────────────────────
        Notification n3 = notif(
                "guest2@example.com", "Emily Clarke",
                NotificationType.BOOKING_CANCELLED, NotificationStatus.SENT,
                "Booking Cancelled — Refund Initiated",
                "Dear Emily Clarke, your booking has been cancelled. " +
                        "A refund of $" + cancelled1.getRefundAmount() + " has been initiated " +
                        "and will appear in your account within 5-7 business days.",
                cancelled1.getId(), ReferenceType.BOOKING,
                LocalDateTime.now().minusDays(22), LocalDateTime.now().minusDays(22));

        // ── PAYMENT_SUCCESS — SENT ─────────────────────────────────────────
        Notification n4 = notif(
                "guest3@example.com", "Yuki Tanaka",
                NotificationType.PAYMENT_SUCCESS, NotificationStatus.SENT,
                "Payment Confirmed — $" + confirmed1.getTotalPrice(),
                "Your payment of $" + confirmed1.getTotalPrice() + " has been successfully processed. " +
                        "Transaction reference: " + sp1.getTransactionReference(),
                sp1.getId(), ReferenceType.PAYMENT,
                LocalDateTime.now().minusDays(8), LocalDateTime.now().minusDays(8));

        // ── PAYMENT_FAILED — SENT ─────────────────────────────────────────
        Payment failedPay = findPayment(payments, pending1.getId(), PaymentStatus.FAILED);
        Notification n5 = notif(
                "guest1@example.com", "Ahmed Al-Rashid",
                NotificationType.PAYMENT_FAILED, NotificationStatus.SENT,
                "Payment Failed — Please Retry",
                "Unfortunately your payment could not be processed. " +
                        "Reason: Card declined — insufficient funds. " +
                        "Please update your payment method and try again.",
                failedPay.getId(), ReferenceType.PAYMENT,
                LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(2));

        // ── PAYMENT_REFUNDED — SENT ────────────────────────────────────────
        Notification n6 = notif(
                "guest2@example.com", "Emily Clarke",
                NotificationType.PAYMENT_REFUNDED, NotificationStatus.SENT,
                "Refund Processed — $" + cancelled1.getTotalPrice(),
                "Your refund of $" + cancelled1.getTotalPrice() + " has been processed. " +
                        "Please allow 5-7 business days for the amount to appear in your account.",
                rp1.getId(), ReferenceType.PAYMENT,
                LocalDateTime.now().minusDays(22), LocalDateTime.now().minusDays(21));

        // ── BOOKING_REMINDER — SENT (اليوم قبل الـ check-in) ─────────────
        Notification n7 = notif(
                "guest3@example.com", "Yuki Tanaka",
                NotificationType.BOOKING_REMINDER, NotificationStatus.SENT,
                "Reminder: Check-in Tomorrow at Grand Amman Palace",
                "Dear Yuki Tanaka, this is a reminder that your check-in " +
                        "at Grand Amman Palace is tomorrow. Check-in time: 2:00 PM. " +
                        "We look forward to welcoming you!",
                confirmed1.getId(), ReferenceType.BOOKING,
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(2));

        // ── REVIEW_REMINDER — PENDING (لم يُرسل بعد) ─────────────────────
        Notification n8 = notif(
                "guest4@example.com", "Carlos Mendez",
                NotificationType.REVIEW_REMINDER, NotificationStatus.PENDING,
                "Share Your Experience — Review Your Stay",
                "Dear Carlos Mendez, we hope you enjoyed your recent stay. " +
                        "Would you take a moment to share your experience? " +
                        "Your feedback helps us improve.",
                null, ReferenceType.SYSTEM,
                null, null);

        // ── ROOM_AVAILABLE — SENT (waiting list notification) ────────────
        Notification n9 = notif(
                "guest2@example.com", "Emily Clarke",
                NotificationType.ROOM_AVAILABLE, NotificationStatus.SENT,
                "Room Available! Deluxe King Suite — Act Fast",
                "Great news! A Deluxe King Suite at Grand Amman Palace " +
                        "has become available for your requested dates. " +
                        "You have 24 hours to complete your booking.",
                null, ReferenceType.SYSTEM,
                LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(2));

        // ── WELCOME_EMAIL — SENT ──────────────────────────────────────────
        Notification n10 = notif(
                "guest1@example.com", "Ahmed Al-Rashid",
                NotificationType.WELCOME_EMAIL, NotificationStatus.SENT,
                "Welcome to PinkFlow Hotels!",
                "Dear Ahmed Al-Rashid, welcome to PinkFlow Hotels! " +
                        "Your account has been created successfully. " +
                        "Start exploring our beautiful properties today.",
                null, ReferenceType.SYSTEM,
                LocalDateTime.now().minusDays(60), LocalDateTime.now().minusDays(60));

        // ── FAILED notification — لتست الـ retry logic ────────────────────
        Notification n11 = notif(
                "guest5@example.com", "Blocked User",
                NotificationType.BOOKING_CONFIRMED, NotificationStatus.FAILED,
                "Booking Confirmation",
                "Your booking has been confirmed.",
                null, ReferenceType.BOOKING,
                null, null);
        n11.setRetryCount(3);
        n11.setErrorMessage("SMTP connection refused: recipient address rejected");

        // ── RETRY_SCHEDULED — في قائمة إعادة المحاولة ────────────────────
        Notification n12 = notif(
                "guest4@example.com", "Carlos Mendez",
                NotificationType.PAYMENT_SUCCESS, NotificationStatus.RETRY_SCHEDULED,
                "Payment Confirmed",
                "Your payment has been successfully processed.",
                null, ReferenceType.PAYMENT,
                null, null);
        n12.setRetryCount(1);
        n12.setErrorMessage("Temporary SMTP error — retrying");
        n12.setNextRetryAt(LocalDateTime.now().plusMinutes(15));

        // ── PERMANENTLY_FAILED — فشل نهائي ───────────────────────────────
        Notification n13 = notif(
                "invalid@nonexistent.xyz", "Unknown User",
                NotificationType.CUSTOM, NotificationStatus.PERMANENTLY_FAILED,
                "Test Notification",
                "This notification permanently failed after max retries.",
                null, ReferenceType.SYSTEM,
                null, null);
        n13.setRetryCount(5);
        n13.setErrorMessage("Max retries exceeded: invalid email domain");

        // ── CUSTOM notification ───────────────────────────────────────────
        Notification n14 = notif(
                "guest1@example.com", "Ahmed Al-Rashid",
                NotificationType.CUSTOM, NotificationStatus.SENT,
                "Special Offer — Exclusive for Loyal Guests",
                "Dear Ahmed Al-Rashid, as a valued guest, we're offering you " +
                        "an exclusive 20% discount on your next booking. " +
                        "Use code: LOYAL20 before the end of this month.",
                null, ReferenceType.SYSTEM,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(5));

        notificationRepository.saveAll(List.of(
                n1, n2, n3, n4, n5, n6, n7, n8, n9, n10,
                n11, n12, n13, n14));

        log.info("✅ Notifications created: 14 (all types & statuses covered)");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HELPER BUILDERS
    // ═════════════════════════════════════════════════════════════════════════

    private RoomType roomType(Hotel hotel, String name, int capacity,
                              BedType bedType, int bedCount, int maxAdults, int maxChildren,
                              BigDecimal basePrice, int totalUnits,
                              String description, String policies,
                              RoomTypeStatus status, Set<Amenity> amenities) {
        RoomType rt = new RoomType();
        rt.setHotel(hotel);
        rt.setName(name);
        rt.setCapacity(capacity);
        rt.setBedType(bedType);
        rt.setBedCount(bedCount);
        rt.setMaxAdults(maxAdults);
        rt.setMaxChildren(maxChildren);
        rt.setBasePrice(basePrice);
        rt.setTotalUnits(totalUnits);
        rt.setDescription(description);
        rt.setPolicies(policies);
        rt.setStatus(status);
        rt.setAmenities(amenities);
        return rt;
    }

    private Booking booking(RoomType roomType,
                            String guestName, String guestEmail, String guestPhone,
                            int adults, int children,
                            LocalDate checkIn, LocalDate checkOut,
                            BigDecimal pricePerNight, BookingStatus status,
                            String notes) {
        Booking b = new Booking();
        b.setRoomType(roomType);
        b.setGuestName(guestName);
        b.setGuestEmail(guestEmail);
        b.setGuestPhone(guestPhone);
        b.setAdults(adults);
        b.setChildren(children);
        b.setTotalGuests(adults + children);
        b.setCheckIn(checkIn);
        b.setCheckOut(checkOut);
        b.setPricePerNight(pricePerNight);
        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        b.setTotalPrice(pricePerNight.multiply(BigDecimal.valueOf(nights)));
        b.setStatus(status);
        b.setGuestNotes(notes);
        return b;
    }

    private Payment payment(Long bookingId, BigDecimal amount,
                            PaymentStatus status, String failureReason,
                            LocalDateTime paidAt, LocalDateTime createdOverride) {
        Payment p = new Payment();
        p.setBookingId(bookingId);
        p.setAmount(amount);
        p.setCurrency("USD");
        p.setMethod(PaymentMethod.MOCK_CARD);
        p.setStatus(status);
        p.setProviderName("MOCK_GATEWAY");
        p.setTransactionReference("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        p.setFailureReason(failureReason);
        p.setPaidAt(paidAt);
        return p;
    }

    private WaitingListEntry waitingEntry(RoomType roomType, Hotel hotel,
                                          String email, String name,
                                          LocalDate checkIn, LocalDate checkOut,
                                          WaitingListStatus status,
                                          LocalDateTime notifiedAt,
                                          String roomTypeName, String hotelName) {
        WaitingListEntry e = new WaitingListEntry();
        e.setRoomTypeId(roomType.getId());
        e.setHotelId(hotel.getId());
        e.setGuestEmail(email);
        e.setGuestName(name);
        e.setCheckIn(checkIn);
        e.setCheckOut(checkOut);
        e.setStatus(status);
        e.setNotifiedAt(notifiedAt);
        e.setRoomTypeName(roomTypeName);
        e.setHotelName(hotelName);
        return e;
    }

    private Notification notif(String email, String name,
                               NotificationType type, NotificationStatus status,
                               String subject, String body,
                               Long refId, ReferenceType refType,
                               LocalDateTime createdAt, LocalDateTime sentAt) {
        return Notification.builder()
                .recipientEmail(email)
                .recipientName(name)
                .type(type)
                .status(status)
                .subject(subject)
                .body(body)
                .referenceId(refId)
                .referenceType(refType)
                .createdAt(createdAt != null ? createdAt : LocalDateTime.now())
                .sentAt(sentAt)
                .retryCount(0)
                .build();
    }

    private String buildBookingConfirmedBody(String guestName, Booking booking) {
        return "Dear " + guestName + ",\n\n" +
                "Your booking has been confirmed!\n\n" +
                "Booking ID: #" + booking.getId() + "\n" +
                "Check-in: " + booking.getCheckIn() + " at 2:00 PM\n" +
                "Check-out: " + booking.getCheckOut() + " at 12:00 PM\n" +
                "Total Guests: " + booking.getTotalGuests() + "\n" +
                "Total Amount: $" + booking.getTotalPrice() + "\n\n" +
                "We look forward to welcoming you!\n\n" +
                "Warm regards,\nPinkFlow Hotels Team";
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  LOOKUP HELPERS
    // ═════════════════════════════════════════════════════════════════════════

    private Amenity findAmenity(List<Amenity> list, String name) {
        return list.stream().filter(a -> a.getName().equals(name))
                .findFirst().orElseThrow(() ->
                        new IllegalStateException("Amenity not found: " + name));
    }

    private RoomType findRoomType(List<RoomType> list, String name) {
        return list.stream().filter(r -> r.getName().equals(name))
                .findFirst().orElseThrow(() ->
                        new IllegalStateException("RoomType not found: " + name));
    }

    /**
     * يجيب الحجز الـ N (0-based) لضيف معين بحالة معينة.
     * مفيد لما يكون في أكثر من حجز لنفس الضيف بنفس الحالة.
     */
    private Booking findBooking(List<Booking> list, String email,
                                BookingStatus status, int index) {
        List<Booking> matches = list.stream()
                .filter(b -> b.getGuestEmail().equals(email) && b.getStatus() == status)
                .toList();
        if (matches.isEmpty()) throw new IllegalStateException(
                "Booking not found: " + email + "/" + status);
        return matches.get(Math.min(index, matches.size() - 1));
    }

    private Payment findPayment(List<Payment> list, Long bookingId, PaymentStatus status) {
        return list.stream()
                .filter(p -> p.getBookingId().equals(bookingId) && p.getStatus() == status)
                .findFirst().orElseThrow(() ->
                        new IllegalStateException("Payment not found for bookingId=" + bookingId + "/" + status));
    }
}
