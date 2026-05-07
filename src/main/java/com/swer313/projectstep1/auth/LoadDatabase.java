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
import com.swer313.projectstep1.catalog.room.RoomTypeImage;
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
 *  TERRASTAY — PALESTINE HOTEL DATA SEEDER
 *  بيانات فلسطينية حقيقية لمشروع TerraStay
 * ════════════════════════════════════════════════════════════════════════════
 *
 *  المدن الفلسطينية المغطاة:
 *  - القدس (Jerusalem)      31.7767° N, 35.2345° E
 *  - بيت لحم (Bethlehem)   31.7054° N, 35.2024° E
 *  - أريحا (Jericho)       31.8667° N, 35.4500° E
 *  - رام الله (Ramallah)   31.9038° N, 35.2034° E
 *  - نابلس (Nablus)        32.2211° N, 35.2544° E
 *  - الخليل (Hebron)       31.5326° N, 35.0998° E
 *  - طولكرم (Tulkarm)      32.3100° N, 35.0300° E
 *
 *  ╔═══════════════════════════════════════════════════════════════════╗
 *  ║  بيانات الدخول:                                                   ║
 *  ╠═══════════════════════╦══════════════╦════════════════════════════╣
 *  ║ Email                 ║ Password     ║ Role / Info               ║
 *  ╠═══════════════════════╬══════════════╬════════════════════════════╣
 *  ║ admin@terrastay.ps    ║ Admin@1234   ║ ADMIN                     ║
 *  ║ manager1@terrastay.ps ║ Manager@1234 ║ MANAGER — فندق القدس      ║
 *  ║ manager2@terrastay.ps ║ Manager@1234 ║ MANAGER — فندق بيت لحم    ║
 *  ║ manager3@terrastay.ps ║ Manager@1234 ║ MANAGER — فنادق متعددة    ║
 *  ║ layla@example.ps      ║ Guest@1234   ║ GUEST — ليلى أبو عمر      ║
 *  ║ omar@example.ps       ║ Guest@1234   ║ GUEST — عمر الحسين        ║
 *  ║ rania@example.ps      ║ Guest@1234   ║ GUEST — رانيا الخطيب      ║
 *  ║ khalid@example.ps     ║ Guest@1234   ║ GUEST — خالد النجار       ║
 *  ║ sara@example.ps       ║ Guest@1234   ║ GUEST — سارة حداد         ║
 *  ║ james@example.com     ║ Guest@1234   ║ GUEST — James Wilson (سائح)║
 *  ║ amira@example.ps      ║ Guest@1234   ║ GUEST — أميرة سلامة       ║
 *  ╚═══════════════════════╩══════════════╩════════════════════════════╝
 *
 *  الفنادق (10 فنادق):
 *  - فندق الكرمل القدس          (القدس)     — ACTIVE  ★4.8
 *  - فندق دار السلام            (القدس)     — ACTIVE  ★4.5
 *  - فندق قصر فلسطين            (القدس)     — ACTIVE  ★4.6
 *  - فندق جورج                  (بيت لحم)   — ACTIVE  ★4.4
 *  - منتجع سانت جورج            (بيت لحم)   — ACTIVE  ★4.7
 *  - فندق أريحا الريف            (أريحا)     — ACTIVE  ★4.2
 *  - فندق رام المشرق             (رام الله)  — ACTIVE  ★4.3
 *  - فندق نابلس الكبير           (نابلس)     — ACTIVE  ★4.1
 *  - فندق أبراهام الخليل         (الخليل)    — ACTIVE  ★4.0
 *  - فندق الزهور (قيد التجديد)   (طولكرم)   — INACTIVE
 */
@Profile("dev")
@Component
public class LoadDatabase implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

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
        log.info("╔══════════════════════════════════════════════╗");
        log.info("║   TerraStay Palestine — Data Seeder START    ║");
        log.info("╚══════════════════════════════════════════════╝");

        List<User>     users     = seedUsers();
        List<Amenity>  amenities = seedAmenities();
        List<Hotel>    hotels    = seedHotels(users, amenities);
        seedHotelImages(hotels);
        List<RoomType> roomTypes = seedRoomTypes(hotels, amenities);
        seedPricingRules();
        List<Booking>  bookings  = seedBookings(roomTypes);
        List<Payment>  payments  = seedPayments(bookings);
        seedReviews(bookings, hotels);
        seedWaitingList(roomTypes, hotels);
        seedNotifications(bookings, payments);

        log.info("╔══════════════════════════════════════════════╗");
        log.info("║   TerraStay Palestine — Data Seeder DONE ✅  ║");
        log.info("╚══════════════════════════════════════════════╝");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  1. USERS — مستخدمون فلسطينيون وسياح
    // ═════════════════════════════════════════════════════════════════════════

    private List<User> seedUsers() {
        String adminPwd   = passwordEncoder.encode("Admin@1234");
        String managerPwd = passwordEncoder.encode("Manager@1234");
        String guestPwd   = passwordEncoder.encode("Guest@1234");

        // ADMIN
        createUserIfMissing("مدير النظام",        "admin@terrastay.ps",
                adminPwd, "+970-2-240-0001", UserRole.ADMIN, true);

        // MANAGERs — مديرو فنادق فلسطينيون
        createUserIfMissing("سمير عبد الرحمن",    "manager1@terrastay.ps",
                managerPwd, "+970-2-628-1001", UserRole.MANAGER, true);
        createUserIfMissing("ميساء القاسم",       "manager2@terrastay.ps",
                managerPwd, "+970-2-274-2002", UserRole.MANAGER, true);
        createUserIfMissing("أحمد شحادة",         "manager3@terrastay.ps",
                managerPwd, "+970-9-232-3003", UserRole.MANAGER, true);

        // GUESTs — ضيوف فلسطينيون وأجانب
        createUserIfMissing("ليلى أبو عمر",       "layla@example.ps",
                guestPwd, "+970-59-200-1111", UserRole.GUEST, true);
        createUserIfMissing("عمر الحسين",          "omar@example.ps",
                guestPwd, "+970-59-300-2222", UserRole.GUEST, true);
        createUserIfMissing("رانيا الخطيب",        "rania@example.ps",
                guestPwd, "+970-56-400-3333", UserRole.GUEST, true);
        createUserIfMissing("خالد النجار",         "khalid@example.ps",
                guestPwd, "+970-59-500-4444", UserRole.GUEST, true);
        createUserIfMissing("سارة حداد",           "sara@example.ps",
                guestPwd, "+970-56-600-5555", UserRole.GUEST, true);

        // سائح أجنبي
        createUserIfMissing("James Wilson",        "james@example.com",
                guestPwd, "+1-202-555-0199", UserRole.GUEST, true);

        // ضيفة مع حساب غير نشط (لتست حالة inactive)
        createUserIfMissing("أميرة سلامة",         "amira@example.ps",
                guestPwd, "+970-59-700-6666", UserRole.GUEST, false);

        log.info("✅ Users seeded: 1 Admin, 3 Managers, 7 Guests (1 inactive)");
        return userRepository.findAll();
    }

    private void createUserIfMissing(String fullName, String email,
                                     String passwordHash, String phone,
                                     UserRole role, boolean active) {
        userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User(fullName, email, passwordHash, phone, role);
            u.setActive(active);
            return userRepository.save(u);
        });
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  2. AMENITIES — مرافق وخدمات فندقية
    // ═════════════════════════════════════════════════════════════════════════

    private List<Amenity> seedAmenities() {
        if (amenityRepository.count() > 0) {
            log.info("⏭  Amenities already exist — skipping.");
            return amenityRepository.findAll();
        }

        // CONNECTIVITY
        Amenity wifi      = am("إنترنت فايبر عالي السرعة",
                "إنترنت لاسلكي بسرعة 500 ميغابايت في جميع أرجاء الفندق",
                Amenity.AmenityCategory.CONNECTIVITY, false, true);
        Amenity smartTv   = am("تلفاز ذكي 4K مع Netflix",
                "شاشة 65 بوصة 4K مع Netflix وYouTube وتطبيقات البث المباشر",
                Amenity.AmenityCategory.ENTERTAINMENT, false, true);

        // WELLNESS
        Amenity spa       = am("مركز صحي وسبا فاخر",
                "سبا متكامل مع مساج وسونا وحمام بخار وجلسات تدليك",
                Amenity.AmenityCategory.WELLNESS, true, true);
        Amenity gym       = am("صالة رياضية مجهزة",
                "صالة رياضية 24/7 بأحدث الأجهزة ومدربين شخصيين",
                Amenity.AmenityCategory.WELLNESS, false, true);
        Amenity pool      = am("مسبح خارجي مدفأ",
                "مسبح خارجي مفتوح طوال السنة مع خدمة شاملة",
                Amenity.AmenityCategory.OUTDOOR, false, true);
        Amenity jacuzzi   = am("جاكوزي خاص",
                "جاكوزي خاص بغرف السويت مع إطلالة بانورامية",
                Amenity.AmenityCategory.WELLNESS, true, true);

        // DINING
        Amenity breakfast = am("إفطار بوفيه مجاني",
                "بوفيه إفطار يومي بأصناف عربية وعالمية متنوعة",
                Amenity.AmenityCategory.DINING, false, true);
        Amenity roomSvc   = am("خدمة غرف على مدار الساعة",
                "خدمة غرف 24 ساعة من قائمة المطعم الكاملة",
                Amenity.AmenityCategory.DINING, false, true);
        Amenity minibar   = am("ميني بار متميز",
                "ميني بار مكتمل بمشروبات ومقبلات فاخرة",
                Amenity.AmenityCategory.DINING, true, true);
        Amenity restaurant = am("مطعم عربي أصيل",
                "مطعم يقدم أشهى المأكولات الفلسطينية والعربية الأصيلة",
                Amenity.AmenityCategory.DINING, false, true);

        // COMFORT
        Amenity ac        = am("تكييف وتدفئة مركزي",
                "تحكم فردي بدرجة الحرارة عبر ثرموستات ذكي في كل غرفة",
                Amenity.AmenityCategory.COMFORT, false, true);
        Amenity balcony   = am("شرفة خاصة مع إطلالة",
                "شرفة خاصة مؤثثة بإطلالة على المدينة أو الحديقة",
                Amenity.AmenityCategory.OUTDOOR, false, true);
        Amenity cityView  = am("إطلالة بانورامية على المدينة",
                "غرف علوية مع إطلالة 180° على المدينة القديمة والمعالم الأثرية",
                Amenity.AmenityCategory.OUTDOOR, false, true);

        // PARKING & SECURITY
        Amenity parking   = am("موقف سيارات مجاني مع خدمة فاليه",
                "موقف مجاني مع خدمة فاليه على مدار الساعة",
                Amenity.AmenityCategory.PARKING, false, true);
        Amenity safe      = am("خزنة إلكترونية في الغرفة",
                "خزنة إلكترونية بحجم كافٍ لحفظ اللابتوب والمستندات",
                Amenity.AmenityCategory.SECURITY, false, true);

        // CLEANING
        Amenity laundry   = am("خدمة غسيل وكوي",
                "خدمة غسيل وكوي في نفس اليوم متاحة يومياً",
                Amenity.AmenityCategory.CLEANING, false, true);

        // ACCESSIBILITY
        Amenity elevator  = am("مصعد كهربائي",
                "مصاعد حديثة للوصول لجميع الطوابق بسهولة",
                Amenity.AmenityCategory.ACCESSIBILITY, false, true);

        // OUTDOOR
        Amenity garden    = am("حديقة وفضاء خارجي",
                "حديقة جميلة مع أماكن للجلوس في الهواء الطلق وتراس مسقوف",
                Amenity.AmenityCategory.OUTDOOR, false, true);
        Amenity rooftop   = am("مقهى سطح الفندق",
                "مقهى روفتوب مفتوح مع مشروبات ومقبلات وإطلالة ساحرة على المدينة",
                Amenity.AmenityCategory.DINING, false, true);

        // INACTIVE — لتست حالة الخدمات غير النشطة
        Amenity oldPool   = am("مسبح داخلي (تحت التجديد)",
                "مسبح داخلي مغلق مؤقتاً لأعمال التجديد الشاملة",
                Amenity.AmenityCategory.OUTDOOR, false, false);

        List<Amenity> saved = amenityRepository.saveAll(List.of(
                wifi, smartTv, spa, gym, pool, jacuzzi,
                breakfast, roomSvc, minibar, restaurant,
                ac, balcony, cityView, parking, safe,
                laundry, elevator, garden, rooftop, oldPool));

        log.info("✅ Amenities created: {} (19 active + 1 inactive)", saved.size());
        return saved;
    }

    private Amenity am(String name, String desc,
                       Amenity.AmenityCategory cat, boolean premium, boolean active) {
        Amenity a = new Amenity(name, desc, cat);
        a.setNameEn(amenityNameEn(name));
        a.setPremium(premium);
        a.setActive(active);
        return a;
    }

    private String amenityNameEn(String name) {
        return switch (name) {
            case "موقف سيارات مجاني مع خدمة فاليه" -> "Free Valet Parking";
            case "إفطار بوفيه مجاني" -> "Free Breakfast Buffet";
            case "مصعد كهربائي" -> "Elevator";
            case "حديقة وفضاء خارجي" -> "Garden & Outdoor Space";
            case "مسبح خارجي مدفأ" -> "Heated Outdoor Pool";
            case "تكييف وتدفئة مركزي" -> "Central AC & Heating";
            case "مطعم عربي أصيل" -> "Authentic Arabic Restaurant";
            case "إنترنت فايبر عالي السرعة" -> "High-Speed Fiber Internet";
            case "مركز صحي وسبا فاخر" -> "Luxury Spa & Wellness Center";
            case "تلفاز ذكي 4K مع Netflix", "NETFLIX مع 4K تلفاز ذكي" -> "Smart 4K TV with Netflix";
            case "مقهى سطح الفندق" -> "Rooftop Café";
            case "إطلالة بانورامية على المدينة" -> "Panoramic City View";
            case "واي فاي مجاني" -> "Free WiFi";
            case "موقف سيارات مجاني" -> "Free Parking";
            case "مسبح" -> "Swimming Pool";
            case "مركز لياقة", "صالة رياضية مجهزة" -> "Fitness Center";
            case "سبا" -> "Spa";
            case "مطعم" -> "Restaurant";
            case "خدمة الغرف", "خدمة غرف على مدار الساعة" -> "Room Service";
            case "مكيف هواء" -> "Air Conditioning";
            case "غسيل ملابس", "خدمة غسيل وكوي" -> "Laundry Service";
            case "مركز أعمال" -> "Business Center";
            case "قاعة مؤتمرات", "قاعة اجتماعات" -> "Conference Room";
            case "نقل مطار", "نقل من المطار" -> "Airport Shuttle";
            case "إطلالة بحرية" -> "Sea View";
            case "إطلالة على الحديقة", "إطلالة حديقة" -> "Garden View";
            default -> null;
        };
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  3. HOTELS — فنادق فلسطينية حقيقية
    // ═════════════════════════════════════════════════════════════════════════

    private List<Hotel> seedHotels(List<User> users, List<Amenity> amenities) {

        User mgr1 = findUser(users, "manager1@terrastay.ps");
        User mgr2 = findUser(users, "manager2@terrastay.ps");
        User mgr3 = findUser(users, "manager3@terrastay.ps");

        Amenity wifi      = findAmenity(amenities, "إنترنت فايبر عالي السرعة");
        Amenity smartTv   = findAmenity(amenities, "تلفاز ذكي 4K مع Netflix");
        Amenity spa       = findAmenity(amenities, "مركز صحي وسبا فاخر");
        Amenity gym       = findAmenity(amenities, "صالة رياضية مجهزة");
        Amenity pool      = findAmenity(amenities, "مسبح خارجي مدفأ");
        Amenity breakfast = findAmenity(amenities, "إفطار بوفيه مجاني");
        Amenity parking   = findAmenity(amenities, "موقف سيارات مجاني مع خدمة فاليه");
        Amenity restaurant = findAmenity(amenities, "مطعم عربي أصيل");
        Amenity ac        = findAmenity(amenities, "تكييف وتدفئة مركزي");
        Amenity cityView  = findAmenity(amenities, "إطلالة بانورامية على المدينة");
        Amenity garden    = findAmenity(amenities, "حديقة وفضاء خارجي");
        Amenity rooftop   = findAmenity(amenities, "مقهى سطح الفندق");
        Amenity elevator  = findAmenity(amenities, "مصعد كهربائي");

        // ════════════════════════════════════════════════════
        // H1 — فندق الكرمل القدس (Jerusalem / القدس)
        // ════════════════════════════════════════════════════
        Hotel h1 = hotelRepository.findByName("فندق الكرمل القدس")
                .orElseGet(() -> hotelRepository.save(hotel(
                "فندق الكرمل القدس",
                "شارع صلاح الدين، القدس القديمة، فلسطين",
                "يقع فندق الكرمل في قلب البلدة القديمة في القدس، " +
                        "على بعد خطوات من قبة الصخرة وحائط البراق والمسجد الأقصى. " +
                        "يوفر الفندق إقامة فاخرة تجمع بين الأصالة الفلسطينية والرفاهية العصرية، " +
                        "مع إطلالات ساحرة على أسوار المدينة القديمة التي تعود للقرن السادس عشر.",
                "القدس", "فلسطين",
                "+970-2-628-1100",  "reservations@karmeljrusalem.ps",
                "https://www.karmeljerusalem.ps",
                4.8, 31.7765, 35.2344,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "لا تدخين. لا حيوانات أليفة. الأطفال أقل من 12 سنة مجاناً.",
                "إلغاء مجاني حتى 48 ساعة قبل الوصول. بعد ذلك تُفرض رسوم ليلة واحدة.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, spa, gym, breakfast, parking, restaurant, cityView, elevator))));

        // ════════════════════════════════════════════════════
        // H2 — فندق دار السلام (Jerusalem / القدس)
        // ════════════════════════════════════════════════════
        Hotel h2 = hotelRepository.findByName("فندق دار السلام")
                .orElseGet(() -> hotelRepository.save(hotel(
                "فندق دار السلام",
                "شارع المسجد الأقصى، القدس، فلسطين 91000",
                "فندق دار السلام وجهة للمسافرين الباحثين عن الهدوء والراحة " +
                        "في قلب القدس. تصميمه يعكس الهندسة الإسلامية الكلاسيكية " +
                        "مع ساحة داخلية بنوافير رخامية وحدائق مثمرة. " +
                        "يبعد 5 دقائق سيراً عن المسجد الأقصى والكنيسة والقبور التاريخية.",
                "القدس", "فلسطين",
                "+970-2-628-2200", "info@darsalam-hotel.ps",
                "https://www.darsalamhotel.ps",
                4.5, 31.7761, 35.2354,
                LocalTime.of(15, 0), LocalTime.of(11, 0),
                "ساعات هدوء بعد 11 مساءً. ممنوع التدخين في الغرف.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, restaurant, garden, elevator))));

        // ════════════════════════════════════════════════════
        // H3 — فندق قصر فلسطين (Jerusalem / القدس)
        // ════════════════════════════════════════════════════
        Hotel h3 = hotelRepository.findByName("فندق قصر فلسطين")
                .orElseGet(() -> hotelRepository.save(hotel(
                "فندق قصر فلسطين",
                "شارع أم الشرايط، القدس الغربية، فلسطين",
                "فندق قصر فلسطين رمز الفخامة في القدس. " +
                        "أسّس عام 1960 ويحتفظ بطابع معماري فلسطيني أصيل. " +
                        "يضم أكبر قاعة للمؤتمرات في المدينة، ومسبحاً بانورامياً " +
                        "وحديقة ملكية تمتد على مساحة 5 دونمات. " +
                        "اختيار أمراء ورؤساء الدول منذ خمسة عقود.",
                "القدس", "فلسطين",
                "+970-2-628-3300", "reservations@qasrpalestine.ps",
                "https://www.qasrpalestine.ps",
                4.6, 31.7834, 35.2183,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "لا تدخين. نزلاء VIP يحصلون على خدمة كونسيارج شخصية.",
                "إلغاء مجاني حتى 72 ساعة. خصم 50% للإلغاء خلال 24-72 ساعة.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, spa, gym, pool, breakfast, parking, restaurant, cityView, rooftop, elevator))));

        // ════════════════════════════════════════════════════
        // H4 — فندق جورج (Bethlehem / بيت لحم)
        // ════════════════════════════════════════════════════
        Hotel h4 = hotelRepository.findByName("فندق جورج")
                .orElseGet(() -> hotelRepository.save(hotel(
                "فندق جورج",
                "ميدان المهد، بيت لحم، فلسطين 90500",
                "فندق جورج يقع على بعد 100 متر من كنيسة المهد، " +
                        "أقدس كنيسة مسيحية في العالم. " +
                        "يرحب بالحجاج والسياح من جميع أنحاء العالم " +
                        "في أجواء دافئة تعكس روح بيت لحم الأصيلة. " +
                        "المطعم يقدم أشهى الأكلات الفلسطينية الموروثة.",
                "بيت لحم", "فلسطين",
                "+970-2-274-4400", "info@georgehotel-bethlehem.ps",
                "https://www.georgehotel-bethlehem.ps",
                4.4, 31.7043, 35.2073,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "فندق عائلي. يرحب بالحجاج والسياح من جميع الأديان.",
                "إلغاء مجاني حتى 48 ساعة قبل الوصول.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, restaurant, garden, elevator))));

        // ════════════════════════════════════════════════════
        // H5 — منتجع سانت جورج (Bethlehem / بيت لحم)
        // ════════════════════════════════════════════════════
        Hotel h5 = hotelRepository.findByName("منتجع سانت جورج")
                .orElseGet(() -> hotelRepository.save(hotel(
                "منتجع سانت جورج",
                "طريق بيت جالا، بيت لحم، فلسطين 90501",
                "منتجع سانت جورج الفاخر يجمع بين الهدوء والطبيعة الخلابة " +
                        "في قلب جبال بيت لحم. المنتجع محاط بكروم العنب وأشجار الزيتون المعمرة. " +
                        "يضم مسبحاً لانهائياً بإطلالة على التلال، " +
                        "ومركز صحي عالمي المستوى، ومطعماً يقدم خمور العنب الفلسطينية المحلية.",
                "بيت لحم", "فلسطين",
                "+970-2-274-5500", "reservations@saintgeorge-resort.ps",
                "https://www.saintgeorgeresort.ps",
                4.7, 31.7098, 35.1834,
                LocalTime.of(15, 0), LocalTime.of(12, 0),
                "لا حيوانات أليفة. الأطفال أقل من 16 سنة في الأجنحة الكبيرة فقط.",
                "غير قابل للاسترداد لحجوزات أقل من 48 ساعة.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, spa, gym, pool, breakfast, parking, restaurant, garden, rooftop, elevator))));

        // ════════════════════════════════════════════════════
        // H6 — فندق أريحا الريف (Jericho / أريحا)
        // ════════════════════════════════════════════════════
        Hotel h6 = hotelRepository.findByName("فندق أريحا الريف")
                .orElseGet(() -> hotelRepository.save(hotel(
                "فندق أريحا الريف",
                "شارع النخيل، أريحا، فلسطين 18000",
                "أريحا — أقدم مدينة في العالم تستضيف فندق الريف، " +
                        "واحة هادئة وسط نخيل أريحا الشهير. " +
                        "المناخ الدافئ طوال السنة يجعله وجهة مثالية في الشتاء. " +
                        "قريب من قلعة هيروديوم وبحر الملح، " +
                        "ومن مسار دراجات وادي القلط الشهير.",
                "أريحا", "فلسطين",
                "+970-2-232-6600", "info@jerichohotel.ps",
                "https://www.jerichohotel.ps",
                4.2, 31.8579, 35.4588,
                LocalTime.of(14, 0), LocalTime.of(11, 0),
                "تحكم فردي بالتكييف. خدمات المنتجع تشمل رحلات للمواقع الأثرية.",
                "إلغاء مجاني حتى 24 ساعة.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, pool, breakfast, parking, restaurant, garden))));

        // ════════════════════════════════════════════════════
        // H7 — فندق رام المشرق (Ramallah / رام الله)
        // ════════════════════════════════════════════════════
        Hotel h7 = hotelRepository.findByName("فندق رام المشرق")
                .orElseGet(() -> hotelRepository.save(hotel(
                "فندق رام المشرق",
                "شارع الإرسال، رام الله، فلسطين 00972",
                "فندق رام المشرق في قلب رام الله التجارية والثقافية. " +
                        "وجهة المؤتمرات والأعمال في فلسطين. " +
                        "يضم أحدث قاعات الاجتماعات وصالات العرض، " +
                        "ومطعماً على السطح يوفر أجمل إطلالة على تلال رام الله الخضراء. " +
                        "بالقرب من المتحف الفلسطيني ومركز المدينة.",
                "رام الله", "فلسطين",
                "+970-2-295-7700", "info@rammashreq.ps",
                "https://www.rammashreq.ps",
                4.3, 31.9012, 35.2042,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "مناسب لرجال الأعمال. فطور مجاني مع الإقامة.",
                "إلغاء مجاني حتى 24 ساعة.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, ac, gym, breakfast, parking, restaurant, rooftop, elevator))));

        // ════════════════════════════════════════════════════
        // H8 — فندق نابلس الكبير (Nablus / نابلس)
        // ════════════════════════════════════════════════════
        Hotel h8 = hotelRepository.findByName("فندق نابلس الكبير")
                .orElseGet(() -> hotelRepository.save(hotel(
                "فندق نابلس الكبير",
                "شارع فيصل، نابلس، فلسطين 44000",
                "فندق نابلس الكبير يرحب بك في عاصمة فلسطين الاقتصادية. " +
                        "موقعه المتميز قريب من البلدة القديمة وأسواق النحاسين والقيساريات. " +
                        "جرّب أشهى الكنافة النابلسية الأصيلة في مطعمنا، " +
                        "واستمتع بجلسة حمام تركي تقليدي في الحمام العثماني الملحق.",
                "نابلس", "فلسطين",
                "+970-9-232-8800", "reservations@nablushotel.ps",
                "https://www.nablushotel.ps",
                4.1, 32.2181, 35.2534,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قريب من الأسواق الشعبية. أجواء عائلية مرحبة.",
                "إلغاء مجاني حتى 24 ساعة.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, spa, breakfast, parking, restaurant, garden, elevator))));

        // ════════════════════════════════════════════════════
        // H9 — فندق أبراهام الخليل (Hebron / الخليل)
        // ════════════════════════════════════════════════════
        Hotel h9 = hotelRepository.findByName("فندق أبراهام الخليل")
                .orElseGet(() -> hotelRepository.save(hotel(
                "فندق أبراهام الخليل",
                "شارع العين، الخليل، فلسطين 90200",
                "فندق أبراهام في الخليل — مدينة إبراهيم الخليل ومسرح التاريخ. " +
                        "قريب من المسجد الإبراهيمي والبلدة القديمة العريقة، " +
                        "ومصانع الزجاج والفخار الخليلية الشهيرة. " +
                        "يقدم الفندق تجربة إقامة أصيلة مع إطلالة مميزة على جبال الخليل.",
                "الخليل", "فلسطين",
                "+970-2-222-9900", "info@abrahamhotel-hebron.ps",
                "https://www.abrahamhotelhebron.ps",
                4.0, 31.5326, 35.0998,
                LocalTime.of(14, 0), LocalTime.of(11, 0),
                "يرحب بجميع الأديان. طابع عائلي أصيل.",
                "إلغاء مجاني حتى 24 ساعة.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, restaurant, garden))));

        // ════════════════════════════════════════════════════
        // H10 — فندق الزهور (Tulkarm / طولكرم) — INACTIVE
        // ════════════════════════════════════════════════════
        Hotel h10 = hotelRepository.findByName("فندق الزهور طولكرم")
                .orElseGet(() -> hotelRepository.save(hotel(
                "فندق الزهور طولكرم",
                "شارع الاستقلال، طولكرم، فلسطين 26400",
                "فندق الزهور في طولكرم تحت التجديد الشامل. " +
                        "سيعود بأبهى حلة في الربع الثاني من عام 2026 " +
                        "بتصميم عصري يحافظ على الطابع التراثي الفلسطيني.",
                "طولكرم", "فلسطين",
                "+970-9-267-1010", "info@zuhourtulkarm.ps",
                null,
                3.8, 32.3100, 35.0300,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "مغلق مؤقتاً لأعمال التجديد الشاملة.",
                "لا تُقبل الحجوزات حالياً — قيد التجديد.",
                Hotel.Status.INACTIVE,
                Set.of(wifi, ac))));

        Hotel h11 = hotelRepository.findByName("Reggenza Hotel Downtown Ramallah")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Reggenza Hotel Downtown Ramallah",
                "ميدان المنارة، رام الله، فلسطين",
                "فندق فاخر في قلب رام الله قرب ميدان المنارة، مناسب للمسافرين الباحثين عن إقامة راقية وخدمة عالية المستوى. يوفر غرفا أنيقة وتجربة حضرية قريبة من المطاعم والمراكز الثقافية.",
                "رام الله", "فلسطين",
                "+970-2-295-1101", "info@reggenza-ramallah.ps",
                "https://www.reggenza-ramallah.ps",
                4.7, 31.9038, 35.2034,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, spa, gym, pool, breakfast, parking, restaurant, cityView, rooftop, elevator))));

        Hotel h12 = hotelRepository.findByName("Millennium Palestine Ramallah")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Millennium Palestine Ramallah",
                "شارع البيرة، رام الله، فلسطين",
                "فندق عصري بإطلالة بانورامية على المدينة ومسبح خارجي مناسب للإقامات الفاخرة ورحلات الأعمال. يتميز بمرافق ضيافة واسعة وخدمة احترافية.",
                "رام الله", "فلسطين",
                "+970-2-295-1102", "info@millennium-palestine.ps",
                "https://www.millennium-palestine.ps",
                4.5, 31.9065, 35.2058,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, spa, gym, pool, breakfast, parking, restaurant, garden, cityView, elevator))));

        Hotel h13 = hotelRepository.findByName("Carmel Hotel Ramallah")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Carmel Hotel Ramallah",
                "شارع الإرسال، رام الله، فلسطين",
                "فندق خمس نجوم في شارع الإرسال يضم مركز لياقة وحديقة هادئة وغرفا رحبة. يلائم الضيوف الباحثين عن راحة فاخرة في مركز رام الله.",
                "رام الله", "فلسطين",
                "+970-2-295-1103", "info@carmel-ramallah.ps",
                "https://www.carmel-ramallah.ps",
                4.5, 31.902, 35.201,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, spa, gym, pool, breakfast, parking, restaurant, cityView, rooftop, elevator))));

        Hotel h14 = hotelRepository.findByName("St Andrew's Guesthouse Ramallah")
                .orElseGet(() -> hotelRepository.save(hotel(
                "St Andrew's Guesthouse Ramallah",
                "شارع النزهة، رام الله، فلسطين",
                "بيت ضيافة مريح وودود في رام الله يقدم إقامة اقتصادية دافئة وخدمة شخصية. مناسب للزوار والرحالة والوفود الصغيرة.",
                "رام الله", "فلسطين",
                "+970-2-295-1104", "info@standrews-ramallah.ps",
                "https://www.standrews-ramallah.ps",
                4.55, 31.899, 35.207,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, elevator))));

        Hotel h15 = hotelRepository.findByName("Royal Court Suites Ramallah")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Royal Court Suites Ramallah",
                "قلب رام الله، فلسطين",
                "أجنحة مريحة في قلب رام الله، تتميز بشرفات خاصة في جميع الغرف وقربها من المطاعم والأسواق. خيار عملي للإقامات الطويلة والعائلية.",
                "رام الله", "فلسطين",
                "+970-2-295-1105", "info@royalcourt-ramallah.ps",
                "https://www.royalcourt-ramallah.ps",
                4.2, 31.9012, 35.2005,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, ac, gym, breakfast, parking, restaurant, rooftop, elevator))));

        Hotel h16 = hotelRepository.findByName("MERYLAND Hotel Ramallah")
                .orElseGet(() -> hotelRepository.save(hotel(
                "MERYLAND Hotel Ramallah",
                "200م من ميدان المنارة، رام الله",
                "فندق ثلاث نجوم قريب من ميدان المنارة يقدم إقامة اقتصادية نظيفة وموقعا مركزيا. مناسب لرحلات العمل القصيرة والزوار الباحثين عن قيمة جيدة.",
                "رام الله", "فلسطين",
                "+970-2-295-1106", "info@meryland-ramallah.ps",
                "https://www.meryland-ramallah.ps",
                4.1, 31.9035, 35.203,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, elevator))));

        Hotel h17 = hotelRepository.findByName("City Inn Palace Hotel")
                .orElseGet(() -> hotelRepository.save(hotel(
                "City Inn Palace Hotel",
                "1كم من ميدان المنارة، رام الله",
                "فندق هادئ يضم حديقة وتراسا وموقف سيارات مجاني، ويقدم إقامة مريحة على مسافة قصيرة من مركز رام الله.",
                "رام الله", "فلسطين",
                "+970-2-295-1107", "info@cityinnpalace.ps",
                "https://www.cityinnpalace.ps",
                4, 31.9, 35.199,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, ac, gym, breakfast, parking, restaurant, rooftop, elevator))));

        Hotel h18 = hotelRepository.findByName("Ankars Suites & Hotel")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Ankars Suites & Hotel",
                "400م من مركز السكاكيني، رام الله",
                "أجنحة فندقية مع صالة رياضية وحديقة وتراس على السطح بالقرب من مركز السكاكيني الثقافي. مناسبة للإقامات العملية والطويلة.",
                "رام الله", "فلسطين",
                "+970-2-295-1108", "info@ankars-suites.ps",
                "https://www.ankars-suites.ps",
                3.9, 31.8985, 35.2015,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, ac, gym, breakfast, parking, restaurant, rooftop, elevator))));

        Hotel h19 = hotelRepository.findByName("Palestine Plaza Hotel")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Palestine Plaza Hotel",
                "800م من المقاطعة، رام الله",
                "فندق حضري يضم مطعما وتراسا وموقف سيارات مجاني وصالة رياضية. مناسب للمسافرين الباحثين عن موقع عملي في رام الله.",
                "رام الله", "فلسطين",
                "+970-2-295-1109", "info@palestineplaza.ps",
                "https://www.palestineplaza.ps",
                3.25, 31.905, 35.197,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, ac, gym, breakfast, parking, restaurant, rooftop, elevator))));

        Hotel h20 = hotelRepository.findByName("Mirador Hotel")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Mirador Hotel",
                "2كم من ميدان المنارة، رام الله",
                "فندق أربع نجوم يوفر خدمة كونسيرج وحديقة وخدمات عملية مثل الصراف الآلي. يقع في منطقة هادئة نسبيا من رام الله.",
                "رام الله", "فلسطين",
                "+970-2-295-1110", "info@mirador-ramallah.ps",
                "https://www.mirador-ramallah.ps",
                3.65, 31.908, 35.195,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, ac, gym, breakfast, parking, restaurant, rooftop, elevator))));

        Hotel h21 = hotelRepository.findByName("Lotus Boutique Hotel")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Lotus Boutique Hotel",
                "بيت لحم، فلسطين",
                "فندق بوتيكي أنيق في بيت لحم يجمع بين التصميم الهادئ والخدمة الشخصية. يوفر موقف سيارات مجاني وإقامة قريبة من قلب المدينة.",
                "بيت لحم", "فلسطين",
                "+970-2-274-1201", "info@lotus-bethlehem.ps",
                "https://www.lotus-bethlehem.ps",
                4.4, 31.706, 35.201,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, ac, breakfast, parking, restaurant, garden, elevator))));

        Hotel h22 = hotelRepository.findByName("Assaraya Palace Hotel")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Assaraya Palace Hotel",
                "بيت لحم القديمة، فلسطين",
                "فندق بطابع قصر في بيت لحم القديمة يقدم تجربة إقامة راقية وأجواء تراثية. مناسب للضيوف الباحثين عن موقع تاريخي وخدمة مميزة.",
                "بيت لحم", "فلسطين",
                "+970-2-274-1202", "info@assaraya-palace.ps",
                "https://www.assaraya-palace.ps",
                4.6, 31.704, 35.205,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, restaurant, garden, cityView, elevator))));

        Hotel h23 = hotelRepository.findByName("Bright Tower Hotel")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Bright Tower Hotel",
                "بيت لحم، فلسطين",
                "فندق حديث بإطلالات بانورامية على بيت لحم وغرف عصرية مشرقة. يقدم إقامة مريحة وقريبة من معالم المدينة.",
                "بيت لحم", "فلسطين",
                "+970-2-274-1203", "info@brighttower-bethlehem.ps",
                "https://www.brighttower-bethlehem.ps",
                4.55, 31.7025, 35.2035,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, ac, gym, breakfast, parking, restaurant, rooftop, elevator))));

        Hotel h24 = hotelRepository.findByName("Sancta Maria Hotel")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Sancta Maria Hotel",
                "بيت لحم، فلسطين",
                "فندق بطابع ديني وتراثي يرحب بالحجاج والزوار، مع موقف سيارات مجاني وموقع مناسب لاستكشاف بيت لحم.",
                "بيت لحم", "فلسطين",
                "+970-2-274-1204", "info@sanctamaria-bethlehem.ps",
                "https://www.sanctamaria-bethlehem.ps",
                4.45, 31.7055, 35.206,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, restaurant, garden, elevator))));

        Hotel h25 = hotelRepository.findByName("Grand Hotel Bethlehem")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Grand Hotel Bethlehem",
                "بيت لحم، فلسطين",
                "فندق فاخر عالي التقييم في بيت لحم يوفر غرفا واسعة وخدمة راقية. مناسب للإقامات المميزة وقريب من أهم المعالم.",
                "بيت لحم", "فلسطين",
                "+970-2-274-1205", "info@grand-bethlehem.ps",
                "https://www.grand-bethlehem.ps",
                4.65, 31.707, 35.202,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, spa, gym, pool, breakfast, parking, restaurant, cityView, rooftop, elevator))));

        Hotel h26 = hotelRepository.findByName("Christmas Hotel Bethlehem")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Christmas Hotel Bethlehem",
                "شارع المهد، بيت لحم، فلسطين",
                "فندق قريب من كنيسة المهد بطابع احتفالي وخدمة تناسب الحجاج والعائلات. يتميز بموقعه على شارع المهد.",
                "بيت لحم", "فلسطين",
                "+970-2-274-1206", "info@christmas-bethlehem.ps",
                "https://www.christmas-bethlehem.ps",
                4.3, 31.7035, 35.208,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, restaurant, garden, elevator))));

        Hotel h27 = hotelRepository.findByName("Paradise Hotel Bethlehem")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Paradise Hotel Bethlehem",
                "بيت لحم، فلسطين",
                "فندق عائلي محاط بحديقة يقدم إقامة مريحة وهادئة في بيت لحم. مناسب للعائلات والمجموعات الصغيرة.",
                "بيت لحم", "فلسطين",
                "+970-2-274-1207", "info@paradise-bethlehem.ps",
                "https://www.paradise-bethlehem.ps",
                4.1, 31.708, 35.199,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, elevator))));

        Hotel h28 = hotelRepository.findByName("Alexander Hotel Bethlehem")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Alexander Hotel Bethlehem",
                "بيت لحم، فلسطين",
                "فندق اقتصادي قرب البلدة القديمة في بيت لحم يقدم غرفا عملية وخدمة ودودة. خيار مناسب للمسافرين ذوي الميزانية المحدودة.",
                "بيت لحم", "فلسطين",
                "+970-2-274-1208", "info@alexander-bethlehem.ps",
                "https://www.alexander-bethlehem.ps",
                3.9, 31.7015, 35.2045,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, elevator))));

        Hotel h29 = hotelRepository.findByName("American Colony Hotel")
                .orElseGet(() -> hotelRepository.save(hotel(
                "American Colony Hotel",
                "شارع نابلس، القدس الشرقية، فلسطين",
                "فندق تاريخي فاخر من فئة خمس نجوم في القدس الشرقية، يعد من معالم الضيافة الكلاسيكية. يجمع بين الحدائق الهادئة والعمارة التاريخية والخدمة الراقية.",
                "القدس", "فلسطين",
                "+970-2-628-1301", "info@american-colony.ps",
                "https://www.american-colony.ps",
                4.9, 31.7895, 35.231,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, spa, gym, pool, breakfast, parking, restaurant, cityView, rooftop, elevator))));

        Hotel h30 = hotelRepository.findByName("Legacy Hotel Jerusalem")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Legacy Hotel Jerusalem",
                "القدس، فلسطين",
                "فندق حديث في القدس يوفر إطلالات على المدينة ومرافق راقية ومسبحا للضيوف. مناسب للإقامات العائلية ورحلات العمل.",
                "القدس", "فلسطين",
                "+970-2-628-1302", "info@legacy-jerusalem.ps",
                "https://www.legacy-jerusalem.ps",
                4.6, 31.782, 35.22,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, spa, gym, pool, breakfast, parking, restaurant, garden, cityView, elevator))));

        Hotel h31 = hotelRepository.findByName("National Hotel Jerusalem")
                .orElseGet(() -> hotelRepository.save(hotel(
                "National Hotel Jerusalem",
                "شارع الزهراء، القدس، فلسطين",
                "فندق مركزي في شارع الزهراء يقدم غرفا عائلية وموقعا مناسبا لاستكشاف القدس. يتميز بخدمة عملية وأجواء مريحة.",
                "القدس", "فلسطين",
                "+970-2-628-1303", "info@national-jerusalem.ps",
                "https://www.national-jerusalem.ps",
                4.3, 31.785, 35.228,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, ac, gym, breakfast, parking, restaurant, rooftop, elevator))));

        Hotel h32 = hotelRepository.findByName("Jerusalem Hotel")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Jerusalem Hotel",
                "شارع نابلس، القدس، فلسطين",
                "فندق بوتيكي في مبنى تاريخي من القرن التاسع عشر قرب شارع نابلس. يوفر تجربة أصيلة تجمع بين التراث والراحة.",
                "القدس", "فلسطين",
                "+970-2-628-1304", "info@jerusalem-hotel.ps",
                "https://www.jerusalem-hotel.ps",
                4.4, 31.787, 35.2295,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, restaurant, garden, cityView, elevator))));

        Hotel h33 = hotelRepository.findByName("Golden Walls Hotel Jerusalem")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Golden Walls Hotel Jerusalem",
                "القدس القديمة، فلسطين",
                "فندق قريب من باب العامود وخطوات من أسوار البلدة القديمة. مناسب للزوار الراغبين في موقع تاريخي مركزي.",
                "القدس", "فلسطين",
                "+970-2-628-1305", "info@goldenwalls-jerusalem.ps",
                "https://www.goldenwalls-jerusalem.ps",
                4.2, 31.778, 35.235,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, restaurant, garden, elevator))));

        Hotel h34 = hotelRepository.findByName("Christmas Hotel Jerusalem")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Christmas Hotel Jerusalem",
                "القدس، فلسطين",
                "فندق مناسب للحجاج والزوار بميزانية متوسطة، يقدم إقامة ودودة وموقعا مريحا في القدس.",
                "القدس", "فلسطين",
                "+970-2-628-1306", "info@christmas-jerusalem.ps",
                "https://www.christmas-jerusalem.ps",
                4, 31.781, 35.226,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, restaurant, garden, elevator))));

        Hotel h35 = hotelRepository.findByName("Mount of Olives Hotel")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Mount of Olives Hotel",
                "جبل الزيتون، القدس، فلسطين",
                "فندق على جبل الزيتون يتميز بإطلالة رائعة على البلدة القديمة وقبة الصخرة. خيار هادئ للزوار ومحبي المشاهد التاريخية.",
                "القدس", "فلسطين",
                "+970-2-628-1307", "info@mountofolives-hotel.ps",
                "https://www.mountofolives-hotel.ps",
                4.1, 31.778, 35.243,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, restaurant, garden, elevator))));

        Hotel h36 = hotelRepository.findByName("Notre Dame Jerusalem Center")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Notre Dame Jerusalem Center",
                "مقابل باب الجديد، القدس، فلسطين",
                "مركز ضيافة تاريخي مقابل باب الجديد يضم مطعما على السطح بإطلالة أيقونية. مناسب للحجاج والضيوف الباحثين عن موقع استثنائي.",
                "القدس", "فلسطين",
                "+970-2-628-1308", "info@notredame-jerusalem.ps",
                "https://www.notredame-jerusalem.ps",
                4.5, 31.78, 35.227,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, restaurant, garden, elevator))));

        Hotel h37 = hotelRepository.findByName("InterContinental Jericho")
                .orElseGet(() -> hotelRepository.save(hotel(
                "InterContinental Jericho",
                "أريحا، فلسطين",
                "منتجع دولي واسع في أريحا يقدم مرافق متكاملة ومسابح ومساحات استرخاء. مناسب للعائلات والإقامات الفاخرة في المناخ الدافئ.",
                "أريحا", "فلسطين",
                "+970-2-232-1401", "info@intercontinental-jericho.ps",
                "https://www.intercontinental-jericho.ps",
                4.25, 31.85, 35.456,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, spa, gym, pool, breakfast, parking, restaurant, garden, cityView, elevator))));

        Hotel h38 = hotelRepository.findByName("Dead Sea View Hotel Jericho")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Dead Sea View Hotel Jericho",
                "طريق البحر الميت، أريحا، فلسطين",
                "فندق بإطلالة على البحر الميت يوفر سبا ومسابح ووصولا سهلا إلى الطريق الصحراوي. مناسب للاسترخاء والعلاجات الطبيعية.",
                "أريحا", "فلسطين",
                "+970-2-232-1402", "info@deadsea-jericho.ps",
                "https://www.deadsea-jericho.ps",
                4.35, 31.78, 35.5,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, spa, gym, pool, breakfast, parking, restaurant, garden, cityView, elevator))));

        Hotel h39 = hotelRepository.findByName("Oasis Hotel Jericho")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Oasis Hotel Jericho",
                "أريحا، فلسطين",
                "فندق واحة هادئ بين النخيل في أريحا مع مسبح خارجي وأجواء مريحة. مثالي للإجازات القصيرة والهدوء.",
                "أريحا", "فلسطين",
                "+970-2-232-1403", "info@oasis-jericho.ps",
                "https://www.oasis-jericho.ps",
                4, 31.862, 35.45,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, spa, gym, pool, breakfast, parking, restaurant, garden, cityView, elevator))));

        Hotel h40 = hotelRepository.findByName("Seven Trees Hotel Jericho")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Seven Trees Hotel Jericho",
                "أريحا، فلسطين",
                "فندق اقتصادي في أريحا وسط حديقة هادئة، يقدم إقامة بسيطة ومناسبة للرحلات القصيرة.",
                "أريحا", "فلسطين",
                "+970-2-232-1404", "info@seventrees-jericho.ps",
                "https://www.seventrees-jericho.ps",
                3.8, 31.87, 35.445,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, elevator))));

        Hotel h41 = hotelRepository.findByName("Al-Yasmeen Hotel Nablus")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Al-Yasmeen Hotel Nablus",
                "نابلس، فلسطين",
                "فندق مركزي قريب من سوق البلدة القديمة في نابلس، يقدم إقامة مريحة وأجواء محلية أصيلة.",
                "نابلس", "فلسطين",
                "+970-9-232-1501", "info@alyasmeen-nablus.ps",
                "https://www.alyasmeen-nablus.ps",
                4.2, 32.223, 35.258,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, restaurant, garden, cityView, elevator))));

        Hotel h42 = hotelRepository.findByName("Mövenpick Hotel Nablus")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Mövenpick Hotel Nablus",
                "نابلس، فلسطين",
                "فندق خمس نجوم بعلامة دولية في نابلس يقدم غرفا فاخرة ومرافق ضيافة متكاملة. مناسب للأعمال والإقامات الراقية.",
                "نابلس", "فلسطين",
                "+970-9-232-1502", "info@movenpick-nablus.ps",
                "https://www.movenpick-nablus.ps",
                4.5, 32.215, 35.25,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, spa, gym, pool, breakfast, parking, restaurant, cityView, rooftop, elevator))));

        Hotel h43 = hotelRepository.findByName("Al-Balad Hotel Nablus")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Al-Balad Hotel Nablus",
                "البلدة القديمة، نابلس، فلسطين",
                "فندق تراثي في مبنى محلي داخل البلدة القديمة، يتيح للضيوف تجربة نابلس الأصيلة وقرب الأسواق التاريخية.",
                "نابلس", "فلسطين",
                "+970-9-232-1503", "info@albalad-nablus.ps",
                "https://www.albalad-nablus.ps",
                4, 32.22, 35.255,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, restaurant, garden, cityView, elevator))));

        Hotel h44 = hotelRepository.findByName("Al-Zeitoun Hotel Jenin")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Al-Zeitoun Hotel Jenin",
                "جنين، فلسطين",
                "فندق عالي التقييم في جنين يقدم غرفا راقية وميني بار وسبا فاخر. مناسب للضيوف الباحثين عن إقامة مميزة شمال فلسطين.",
                "جنين", "فلسطين",
                "+970-4-250-1504", "info@alzeitoun-jenin.ps",
                "https://www.alzeitoun-jenin.ps",
                5, 32.461, 35.296,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, spa, gym, pool, breakfast, parking, restaurant, cityView, rooftop, elevator))));

        Hotel h45 = hotelRepository.findByName("Hebron Heritage Hotel")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Hebron Heritage Hotel",
                "البلدة القديمة، الخليل، فلسطين",
                "فندق تراثي في مبنى حجري داخل البلدة القديمة في الخليل. يقدم تجربة محلية قريبة من الأسواق والمعالم التاريخية.",
                "الخليل", "فلسطين",
                "+970-2-222-1601", "info@hebronheritage.ps",
                "https://www.hebronheritage.ps",
                4, 31.54, 35.105,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, restaurant, garden, cityView, elevator))));

        Hotel h46 = hotelRepository.findByName("Al-Reef Hotel Hebron")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Al-Reef Hotel Hebron",
                "الخليل، فلسطين",
                "فندق اقتصادي قريب من المسجد الإبراهيمي، يوفر غرفا عملية وخدمة بسيطة للمسافرين.",
                "الخليل", "فلسطين",
                "+970-2-222-1602", "info@alreef-hebron.ps",
                "https://www.alreef-hebron.ps",
                3.8, 31.53, 35.095,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, elevator))));

        Hotel h47 = hotelRepository.findByName("Palestine House Hotel Hebron")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Palestine House Hotel Hebron",
                "الخليل، فلسطين",
                "فندق محلي تديره عائلة في الخليل، يقدم ضيافة ودودة وأجواء فلسطينية أصيلة.",
                "الخليل", "فلسطين",
                "+970-2-222-1603", "info@palestinehouse-hebron.ps",
                "https://www.palestinehouse-hebron.ps",
                3.9, 31.535, 35.1,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, elevator))));

        Hotel h48 = hotelRepository.findByName("Al-Mathaf Hotel Gaza")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Al-Mathaf Hotel Gaza",
                "غزة، فلسطين",
                "فندق بوتيكي قرب المتحف في غزة يجمع بين الأجواء الثقافية والضيافة الهادئة. مناسب للزوار الباحثين عن تجربة محلية مميزة.",
                "غزة", "فلسطين",
                "+970-8-282-1701", "info@almathaf-gaza.ps",
                "https://www.almathaf-gaza.ps",
                4.2, 31.505, 34.456,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, ac, breakfast, parking, restaurant, garden, elevator))));

        Hotel h49 = hotelRepository.findByName("Windmill Hotel Ramallah")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Windmill Hotel Ramallah",
                "البيرة، رام الله، فلسطين",
                "فندق قريب من منطقة ميدان المنارة والبيرة، يقدم إقامة عملية ومريحة للزوار ورجال الأعمال.",
                "رام الله", "فلسطين",
                "+970-2-295-1702", "info@windmill-ramallah.ps",
                "https://www.windmill-ramallah.ps",
                4, 31.91, 35.21,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, smartTv, ac, gym, breakfast, parking, restaurant, rooftop, elevator))));

        Hotel h50 = hotelRepository.findByName("Daraghmeh Hotel Nablus")
                .orElseGet(() -> hotelRepository.save(hotel(
                "Daraghmeh Hotel Nablus",
                "نابلس، فلسطين",
                "فندق محلي ميسور في نابلس يخدم المسافرين والزبائن المحليين بغرف بسيطة وموقع مناسب.",
                "نابلس", "فلسطين",
                "+970-9-232-1703", "info@daraghmeh-nablus.ps",
                "https://www.daraghmeh-nablus.ps",
                3.7, 32.219, 35.253,
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                "قوانين إقامة مرنة مع احترام الهدوء والممتلكات العامة. يمنع التدخين داخل الغرف وتتوفر خدمات الضيوف على مدار اليوم.",
                "إلغاء مجاني حتى 24 ساعة قبل الوصول. بعد ذلك قد تطبق رسوم الليلة الأولى.",
                Hotel.Status.ACTIVE,
                Set.of(wifi, ac, breakfast, parking, elevator))));

        List<Hotel> saved = hotelRepository.findAll();

        // ── ربط المانجرز بالفنادق ─────────────────────────────────────────
        // mgr1 manages Jerusalem hotels
        mgr1.addManagedHotel(h1);
        mgr1.addManagedHotel(h2);
        mgr1.addManagedHotel(h3);
        mgr1.addManagedHotel(h29);
        mgr1.addManagedHotel(h30);
        mgr1.addManagedHotel(h31);
        mgr1.addManagedHotel(h32);
        mgr1.addManagedHotel(h33);
        mgr1.addManagedHotel(h34);
        mgr1.addManagedHotel(h35);
        mgr1.addManagedHotel(h36);
        // mgr2 manages Bethlehem hotels
        mgr2.addManagedHotel(h4);
        mgr2.addManagedHotel(h5);
        mgr2.addManagedHotel(h21);
        mgr2.addManagedHotel(h22);
        mgr2.addManagedHotel(h23);
        mgr2.addManagedHotel(h24);
        mgr2.addManagedHotel(h25);
        mgr2.addManagedHotel(h26);
        mgr2.addManagedHotel(h27);
        mgr2.addManagedHotel(h28);
        // mgr3 manages all other hotels
        mgr3.addManagedHotel(h6);
        mgr3.addManagedHotel(h7);
        mgr3.addManagedHotel(h8);
        mgr3.addManagedHotel(h9);
        mgr3.addManagedHotel(h10);
        mgr3.addManagedHotel(h11);
        mgr3.addManagedHotel(h12);
        mgr3.addManagedHotel(h13);
        mgr3.addManagedHotel(h14);
        mgr3.addManagedHotel(h15);
        mgr3.addManagedHotel(h16);
        mgr3.addManagedHotel(h17);
        mgr3.addManagedHotel(h18);
        mgr3.addManagedHotel(h19);
        mgr3.addManagedHotel(h20);
        mgr3.addManagedHotel(h37);
        mgr3.addManagedHotel(h38);
        mgr3.addManagedHotel(h39);
        mgr3.addManagedHotel(h40);
        mgr3.addManagedHotel(h41);
        mgr3.addManagedHotel(h42);
        mgr3.addManagedHotel(h43);
        mgr3.addManagedHotel(h44);
        mgr3.addManagedHotel(h45);
        mgr3.addManagedHotel(h46);
        mgr3.addManagedHotel(h47);
        mgr3.addManagedHotel(h48);
        mgr3.addManagedHotel(h49);
        mgr3.addManagedHotel(h50);
        userRepository.saveAll(List.of(mgr1, mgr2, mgr3));

        log.info("✅ Hotels created: 49 active + 1 inactive across Palestine");
        return hotelRepository.findAll();
    }

    private Hotel hotel(String name, String address, String description,
                        String city, String country,
                        String phone, String email, String website,
                        double rating, double lat, double lon,
                        LocalTime checkIn, LocalTime checkOut,
                        String policies, String cancelPolicy,
                        Hotel.Status status, Set<Amenity> amenities) {
        Hotel h = new Hotel();
        h.setName(name);
        h.setAddress(address);
        h.setDescription(description);
        h.setCity(city);
        h.setCountry(country);
        h.setPhoneNumber(phone);
        h.setEmail(email);
        h.setWebsiteUrl(website);
        h.setRating(rating);
        h.setLatitude(lat);
        h.setLongitude(lon);
        h.setCheckInTime(checkIn);
        h.setCheckOutTime(checkOut);
        h.setPolicies(policies);
        h.setCancellationPolicySummary(cancelPolicy);
        h.setStatus(status);
        h.setAmenities(amenities);
        return h;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  4. HOTEL IMAGES — صور حقيقية من Unsplash
    // ═════════════════════════════════════════════════════════════════════════

    private void seedHotelImages(List<Hotel> hotels) {
        int added = 0;
        for (Hotel hotel : hotels) {
            if (hotel == null || hotel.getId() == null) continue;
            if (!hotelImageRepository.findByHotelId(hotel.getId()).isEmpty()) continue;

            List<String> urls = getHotelImages(hotel.getName());
            List<HotelImage> images = urls.stream().map(url -> {
                HotelImage img = new HotelImage();
                img.setHotel(hotel);
                img.setImageUrl(url);
                img.setFileName(url.substring(url.lastIndexOf('/') + 1).split("\\?")[0]);
                return img;
            }).toList();

            hotelImageRepository.saveAll(images);
            added += images.size();
        }
        log.info("✅ Hotel images seeded: {} images total", added);
    }

    private List<String> getHotelImages(String hotelName) {
        return switch (hotelName) {
            case "فندق الكرمل القدس" -> List.of(
                    "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80"
            );
            case "فندق دار السلام" -> List.of(
                    "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1496417263034-38ec4f0b665a?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1455587734955-081b22074882?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1200&q=80"
            );
            case "فندق قصر فلسطين" -> List.of(
                    "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1528360983277-13d401cdc186?auto=format&fit=crop&w=1200&q=80"
            );
            case "فندق جورج" -> List.of(
                    "https://images.unsplash.com/photo-1529290130-4ca3753253ae?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1504701954957-2010ec3bcec1?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80"
            );
            case "منتجع سانت جورج" -> List.of(
                    "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1528360983277-13d401cdc186?auto=format&fit=crop&w=1200&q=80"
            );
            case "فندق أريحا الريف" -> List.of(
                    "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1528360983277-13d401cdc186?auto=format&fit=crop&w=1200&q=80"
            );
            case "فندق رام المشرق" -> List.of(
                    "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1504701954957-2010ec3bcec1?auto=format&fit=crop&w=1200&q=80"
            );
            case "فندق نابلس الكبير" -> List.of(
                    "https://images.unsplash.com/photo-1445019980597-93fa8acb246c?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1496417263034-38ec4f0b665a?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1455587734955-081b22074882?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1528360983277-13d401cdc186?auto=format&fit=crop&w=1200&q=80"
            );
            case "فندق أبراهام الخليل" -> List.of(
                    "https://images.unsplash.com/photo-1445019980597-93fa8acb246c?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1455587734955-081b22074882?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1200&q=80"
            );
            case "فندق الزهور طولكرم" -> List.of(
                    "https://images.unsplash.com/photo-1529290130-4ca3753253ae?auto=format&fit=crop&w=1200&q=80"
            );

            case "Reggenza Hotel Downtown Ramallah" -> List.of(
                    "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1200&q=80"
            );
            case "Millennium Palestine Ramallah" -> List.of(
                    "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=1200&q=80"
            );
            case "Carmel Hotel Ramallah" -> List.of(
                    "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1200&q=80"
            );
            case "St Andrew's Guesthouse Ramallah" -> List.of(
                    "https://images.unsplash.com/photo-1445019980597-93fa8acb246c?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1529290130-4ca3753253ae?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1504701954957-2010ec3bcec1?auto=format&fit=crop&w=1200&q=80"
            );
            case "Royal Court Suites Ramallah" -> List.of(
                    "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80"
            );
            case "MERYLAND Hotel Ramallah" -> List.of(
                    "https://images.unsplash.com/photo-1445019980597-93fa8acb246c?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1529290130-4ca3753253ae?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1504701954957-2010ec3bcec1?auto=format&fit=crop&w=1200&q=80"
            );
            case "City Inn Palace Hotel" -> List.of(
                    "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80"
            );
            case "Ankars Suites & Hotel" -> List.of(
                    "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80"
            );
            case "Palestine Plaza Hotel" -> List.of(
                    "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80"
            );
            case "Mirador Hotel" -> List.of(
                    "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80"
            );
            case "Lotus Boutique Hotel" -> List.of(
                    "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1496417263034-38ec4f0b665a?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1455587734955-081b22074882?auto=format&fit=crop&w=1200&q=80"
            );
            case "Assaraya Palace Hotel" -> List.of(
                    "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1496417263034-38ec4f0b665a?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1455587734955-081b22074882?auto=format&fit=crop&w=1200&q=80"
            );
            case "Bright Tower Hotel" -> List.of(
                    "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80"
            );
            case "Sancta Maria Hotel" -> List.of(
                    "https://images.unsplash.com/photo-1528360983277-13d401cdc186?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1200&q=80"
            );
            case "Grand Hotel Bethlehem" -> List.of(
                    "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1200&q=80"
            );
            case "Christmas Hotel Bethlehem" -> List.of(
                    "https://images.unsplash.com/photo-1528360983277-13d401cdc186?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1200&q=80"
            );
            case "Paradise Hotel Bethlehem" -> List.of(
                    "https://images.unsplash.com/photo-1445019980597-93fa8acb246c?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1529290130-4ca3753253ae?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1504701954957-2010ec3bcec1?auto=format&fit=crop&w=1200&q=80"
            );
            case "Alexander Hotel Bethlehem" -> List.of(
                    "https://images.unsplash.com/photo-1445019980597-93fa8acb246c?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1529290130-4ca3753253ae?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1504701954957-2010ec3bcec1?auto=format&fit=crop&w=1200&q=80"
            );
            case "American Colony Hotel" -> List.of(
                    "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1200&q=80"
            );
            case "Legacy Hotel Jerusalem" -> List.of(
                    "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=1200&q=80"
            );
            case "National Hotel Jerusalem" -> List.of(
                    "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80"
            );
            case "Jerusalem Hotel" -> List.of(
                    "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1496417263034-38ec4f0b665a?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1455587734955-081b22074882?auto=format&fit=crop&w=1200&q=80"
            );
            case "Golden Walls Hotel Jerusalem" -> List.of(
                    "https://images.unsplash.com/photo-1528360983277-13d401cdc186?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1200&q=80"
            );
            case "Christmas Hotel Jerusalem" -> List.of(
                    "https://images.unsplash.com/photo-1528360983277-13d401cdc186?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1200&q=80"
            );
            case "Mount of Olives Hotel" -> List.of(
                    "https://images.unsplash.com/photo-1528360983277-13d401cdc186?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1200&q=80"
            );
            case "Notre Dame Jerusalem Center" -> List.of(
                    "https://images.unsplash.com/photo-1528360983277-13d401cdc186?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1200&q=80"
            );
            case "InterContinental Jericho" -> List.of(
                    "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=1200&q=80"
            );
            case "Dead Sea View Hotel Jericho" -> List.of(
                    "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=1200&q=80"
            );
            case "Oasis Hotel Jericho" -> List.of(
                    "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=1200&q=80"
            );
            case "Seven Trees Hotel Jericho" -> List.of(
                    "https://images.unsplash.com/photo-1445019980597-93fa8acb246c?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1529290130-4ca3753253ae?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1504701954957-2010ec3bcec1?auto=format&fit=crop&w=1200&q=80"
            );
            case "Al-Yasmeen Hotel Nablus" -> List.of(
                    "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1496417263034-38ec4f0b665a?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1455587734955-081b22074882?auto=format&fit=crop&w=1200&q=80"
            );
            case "Mövenpick Hotel Nablus" -> List.of(
                    "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1200&q=80"
            );
            case "Al-Balad Hotel Nablus" -> List.of(
                    "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1496417263034-38ec4f0b665a?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1455587734955-081b22074882?auto=format&fit=crop&w=1200&q=80"
            );
            case "Al-Zeitoun Hotel Jenin" -> List.of(
                    "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1200&q=80"
            );
            case "Hebron Heritage Hotel" -> List.of(
                    "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1496417263034-38ec4f0b665a?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1455587734955-081b22074882?auto=format&fit=crop&w=1200&q=80"
            );
            case "Al-Reef Hotel Hebron" -> List.of(
                    "https://images.unsplash.com/photo-1445019980597-93fa8acb246c?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1529290130-4ca3753253ae?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1504701954957-2010ec3bcec1?auto=format&fit=crop&w=1200&q=80"
            );
            case "Palestine House Hotel Hebron" -> List.of(
                    "https://images.unsplash.com/photo-1445019980597-93fa8acb246c?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1529290130-4ca3753253ae?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1504701954957-2010ec3bcec1?auto=format&fit=crop&w=1200&q=80"
            );
            case "Al-Mathaf Hotel Gaza" -> List.of(
                    "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1496417263034-38ec4f0b665a?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1455587734955-081b22074882?auto=format&fit=crop&w=1200&q=80"
            );
            case "Windmill Hotel Ramallah" -> List.of(
                    "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80"
            );
            case "Daraghmeh Hotel Nablus" -> List.of(
                    "https://images.unsplash.com/photo-1445019980597-93fa8acb246c?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1529290130-4ca3753253ae?auto=format&fit=crop&w=1200&q=80",
                    "https://images.unsplash.com/photo-1504701954957-2010ec3bcec1?auto=format&fit=crop&w=1200&q=80"
            );
            default -> List.of();
        };
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  5. ROOM TYPES — أنواع الغرف
    // ═════════════════════════════════════════════════════════════════════════

    private List<RoomType> seedRoomTypes(List<Hotel> hotels, List<Amenity> amenities) {

        Hotel h1 = hotels.get(0); // فندق الكرمل القدس
        Hotel h2 = hotels.get(1); // فندق دار السلام
        Hotel h3 = hotels.get(2); // فندق قصر فلسطين
        Hotel h4 = hotels.get(3); // فندق جورج بيت لحم
        Hotel h5 = hotels.get(4); // منتجع سانت جورج
        Hotel h6 = hotels.get(5); // فندق أريحا الريف
        Hotel h7 = hotels.get(6); // فندق رام المشرق
        Hotel h8 = hotels.get(7); // فندق نابلس الكبير
        Hotel h9 = hotels.get(8); // فندق أبراهام الخليل
        // h10 (Tulkarm) → INACTIVE hotel

        Amenity wifi      = findAmenity(amenities, "إنترنت فايبر عالي السرعة");
        Amenity ac        = findAmenity(amenities, "تكييف وتدفئة مركزي");
        Amenity minibar   = findAmenity(amenities, "ميني بار متميز");
        Amenity balcony   = findAmenity(amenities, "شرفة خاصة مع إطلالة");
        Amenity safe      = findAmenity(amenities, "خزنة إلكترونية في الغرفة");
        Amenity roomSvc   = findAmenity(amenities, "خدمة غرف على مدار الساعة");
        Amenity smartTv   = findAmenity(amenities, "تلفاز ذكي 4K مع Netflix");
        Amenity cityView  = findAmenity(amenities, "إطلالة بانورامية على المدينة");
        Amenity jacuzzi   = findAmenity(amenities, "جاكوزي خاص");
        Amenity laundry   = findAmenity(amenities, "خدمة غسيل وكوي");
        Amenity breakfast = findAmenity(amenities, "إفطار بوفيه مجاني");

        // ── H1 — فندق الكرمل القدس (4 أنواع + 1 inactive) ───────────────

        RoomType h1Classic = rt(h1, "غرفة كلاسيك مع إطلالة على المدينة", 2,
                BedType.QUEEN, 1, 2, 0, bd("120.00"), 12,
                "غرفة أنيقة بسرير كبير مريح مع إطلالة جميلة على أسوار القدس القديمة " +
                        "وقبة الصخرة. مساحة 28 م² مع حمام رخامي.",
                "لا تدخين. تسجيل الخروج حتى الساعة 12 ظهراً.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, smartTv, safe, cityView));

        RoomType h1Deluxe = rt(h1, "جناح ديلوكس مع بالكونة", 3,
                BedType.KING, 1, 2, 1, bd("195.00"), 6,
                "جناح واسع بسرير ملكي وبالكونة خاصة مفروشة مع إطلالة بانورامية ساحرة " +
                        "على المسجد الأقصى والحرم الشريف. مساحة 45 م².",
                "لا تدخين. تسجيل متأخر متاح برسوم إضافية.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, minibar, balcony, safe, roomSvc, smartTv, cityView));

        RoomType h1Family = rt(h1, "غرفة عائلية توأم", 5,
                BedType.TWIN, 2, 2, 3, bd("165.00"), 5,
                "غرفة عائلية فسيحة بسريرين كبيرين وسرير إضافي للأطفال. " +
                        "مثالية للعائلات والمجموعات. مساحة 52 م² مع منطقة جلوس.",
                "الأطفال أقل من 12 سنة مجانيون. سرير إضافي متاح بطلب مسبق.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, smartTv, safe, laundry));

        RoomType h1Royal = rt(h1, "الجناح الملكي — إطلالة الأقصى", 2,
                BedType.KING, 1, 2, 0, bd("520.00"), 2,
                "تحفة معمارية بمساحة 120 م². غرفة معيشة خاصة، جاكوزي، " +
                        "وتراس كبير مع أفضل إطلالة على قبة الصخرة والمسجد الأقصى. " +
                        "خدمة كونسيارج شخصية 24/7، وجبة إفطار خاصة على التراس.",
                "غير قابل للاسترداد. كونسيارج شخصي على مدار الساعة.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, minibar, jacuzzi, balcony, safe, roomSvc, smartTv, cityView, breakfast));

        RoomType h1Old = rt(h1, "غرفة مفردة اقتصادية (متوقفة)", 1,
                BedType.TWIN, 1, 1, 0, bd("55.00"), 0,
                "غرفة مفردة قديمة في طور الإيقاف. لم تعد متاحة للحجز.",
                "هذا النوع من الغرف متوقف نهائياً.",
                RoomTypeStatus.INACTIVE, Set.of(wifi));

        // ── H2 — فندق دار السلام (3 أنواع) ──────────────────────────────

        RoomType h2Standard = rt(h2, "غرفة قياسية مزدوجة", 2,
                BedType.QUEEN, 1, 2, 0, bd("85.00"), 14,
                "غرفة مريحة بتصميم عربي أصيل مع إطلالة على الساحة الداخلية الهادئة. " +
                        "مساحة 24 م² مع كل الوسائل الأساسية.",
                "لا تدخين. ساعات هدوء بعد 11 مساءً.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, smartTv, safe));

        RoomType h2Heritage = rt(h2, "جناح التراث الفلسطيني", 2,
                BedType.KING, 1, 2, 0, bd("140.00"), 4,
                "جناح مميز بأثاث يدوي الصنع من خشب الزيتون الفلسطيني، " +
                        "وأسقف مقببة بالحجر الأبيض الأصيل. يعيد توهج القدس العثمانية.",
                "غير قابل للاسترداد خلال 48 ساعة قبل الوصول.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, minibar, balcony, safe, smartTv, cityView));

        RoomType h2Triple = rt(h2, "غرفة ثلاثية للأسرة", 4,
                BedType.TWIN, 2, 2, 2, bd("115.00"), 6,
                "غرفة واسعة مخصصة للعائلات بثلاثة أسرّة مريحة وحمام كبير. " +
                        "إطلالة على حدائق الفندق الداخلية الهادئة.",
                "مناسبة للعائلات مع الأطفال.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, smartTv, safe));

        // ── H3 — فندق قصر فلسطين (3 أنواع) ─────────────────────────────

        RoomType h3Superior = rt(h3, "غرفة سوبيريور مع مسبح", 2,
                BedType.QUEEN, 1, 2, 0, bd("145.00"), 10,
                "غرفة سوبيريور أنيقة مع وصول مباشر إلى المسبح الخارجي. " +
                        "تصميم كلاسيكي فلسطيني مع لمسات عصرية فاخرة.",
                "لا تدخين. الوصول للمسبح من 7 صباحاً حتى 10 مساءً.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, smartTv, safe, balcony));

        RoomType h3Executive = rt(h3, "غرفة إكزيكيوتف للأعمال", 2,
                BedType.KING, 1, 2, 0, bd("220.00"), 8,
                "غرفة مجهزة لرجال الأعمال: مكتب كبير، شاشة عرض، " +
                        "إنترنت ألياف بصرية، وإطلالة على المدينة. تشمل وصولاً مجانياً للصالة التنفيذية.",
                "الفطور مشمول. وصول الصالة التنفيذية من 6 صباحاً.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, minibar, safe, roomSvc, smartTv, cityView, breakfast));

        RoomType h3Presidential = rt(h3, "الجناح الرئاسي — قصر فلسطين", 4,
                BedType.KING, 1, 2, 2, bd("680.00"), 1,
                "أرقى أجنحة فلسطين. مساحة 200 م² مع غرفتي نوم، صالون ملكي، " +
                        "جاكوزي مع إطلالة، تراس خاص، وخدمة باتلر 24/7. " +
                        "استضافة رؤساء الدول ومشاهير العالم.",
                "غير قابل للاسترداد. حجز مسبق 30 يوماً على الأقل.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, minibar, jacuzzi, balcony, safe, roomSvc, smartTv, cityView, breakfast, laundry));

        // ── H4 — فندق جورج بيت لحم (3 أنواع) ───────────────────────────

        RoomType h4Standard = rt(h4, "غرفة قياسية قريبة من المهد", 2,
                BedType.QUEEN, 1, 2, 0, bd("75.00"), 18,
                "غرفة دافئة وهادئة على بعد 100 متر من كنيسة المهد. " +
                        "تصميم بسيط يعكس روح بيت لحم الأصيلة.",
                "مناسبة للحجاج والسياح. فطور متاح بسعر إضافي.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, smartTv));

        RoomType h4Pilgrim = rt(h4, "جناح الحاج — كنيسة المهد", 3,
                BedType.KING, 1, 2, 1, bd("120.00"), 6,
                "جناح خاص للحجاج يطل مباشرة على ساحة المهد. " +
                        "يشمل مكتبة دينية، مصلى خاص، وهدايا تذكارية عند الوصول.",
                "غير قابل للاسترداد. مناسب لجميع الأديان.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, smartTv, safe, balcony));

        RoomType h4Family = rt(h4, "غرفة عائلية كبيرة", 5,
                BedType.TWIN, 2, 2, 3, bd("105.00"), 5,
                "غرفة عائلية فسيحة بسريرين كبيرين وسرير أطفال إضافي. " +
                        "مناسبة للعائلات الحاجّة والمجموعات السياحية.",
                "أطفال أقل من 5 سنوات مجانيون.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, smartTv, safe));

        // ── H5 — منتجع سانت جورج بيت لحم (3 أنواع) ─────────────────────

        RoomType h5Garden = rt(h5, "غرفة الحديقة مع إطلالة على الكرم", 2,
                BedType.QUEEN, 1, 2, 0, bd("160.00"), 10,
                "غرفة هادئة تطل على كروم العنب والزيتون المعمّر. " +
                        "شرفة خاصة مع كرسيين للاسترخاء وتناول الشاي الفلسطيني.",
                "لا تدخين. قواعد المنتجع تُطبق.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, smartTv, safe, balcony, breakfast));

        RoomType h5Vineyard = rt(h5, "فيلا الكرم الخاصة", 4,
                BedType.KING, 1, 2, 2, bd("350.00"), 3,
                "فيلا مستقلة وسط الكروم. مسبح خاص، حديقة زيتون، " +
                        "مطبخ كامل، وتراس رومانسي مع غروب الشمس على جبال بيت لحم.",
                "حجز لليلتين كحد أدنى. غير قابل للاسترداد.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, minibar, jacuzzi, balcony, safe, roomSvc, smartTv, breakfast));

        RoomType h5Suite = rt(h5, "جناح العروسين — سانت جورج", 2,
                BedType.KING, 1, 2, 0, bd("280.00"), 2,
                "جناح رومانسي مخصص للأزواج الجدد. جاكوزي بالورود، " +
                        "شمبانيا محلية عند الوصول، عشاء رومانسي خاص، " +
                        "وإطلالة على غروب الشمس فوق التلال.",
                "حجز مسبق بأسبوع. مفاجآت إضافية عند الوصول.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, minibar, jacuzzi, balcony, safe, roomSvc, smartTv, cityView, breakfast));

        // ── H6 — فندق أريحا الريف (2 أنواع) ─────────────────────────────

        RoomType h6Standard = rt(h6, "غرفة النخيل المزدوجة", 2,
                BedType.QUEEN, 1, 2, 0, bd("65.00"), 16,
                "غرفة مريحة تطل على نخيل أريحا المشهور. " +
                        "أريحا — مدينة الدفء في الشتاء — تجعل الإقامة ممتعة طوال العام.",
                "تسجيل الخروج 11 صباحاً. فطور متاح.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, smartTv, safe));

        RoomType h6Desert = rt(h6, "جناح وادي القلط", 2,
                BedType.KING, 1, 2, 0, bd("115.00"), 4,
                "جناح إلهامي مستوحى من وادي القلط الخلاب. " +
                        "تراس أمامي مع إطلالة على التلال والوادي، " +
                        "ومسبح خاص صغير بالمياه الحارة.",
                "مناسب للأزواج وسياحة الطبيعة.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, minibar, balcony, smartTv, safe));

        // ── H7 — فندق رام المشرق (2 أنواع) ──────────────────────────────

        RoomType h7Business = rt(h7, "غرفة الأعمال التنفيذية", 1,
                BedType.KING, 1, 1, 0, bd("110.00"), 20,
                "غرفة مجهزة بالكامل لرجال الأعمال. مكتب، شاشة عرض، " +
                        "إنترنت ألياف بصرية، وصول مجاني لصالة الأعمال.",
                "الفطور مشمول. خدمة الفطور المبكر متاحة من 5:30 صباحاً.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, smartTv, safe, breakfast));

        RoomType h7Roof = rt(h7, "غرفة روفتوب مع تراس خاص", 2,
                BedType.QUEEN, 1, 2, 0, bd("175.00"), 4,
                "غرفة على الطابق العلوي مع تراس خاص وإطلالة 360° على رام الله. " +
                        "تجربة فريدة: العشاء على التراس مع نجوم رام الله.",
                "متاح للبالغين فقط. حجز في الوقت المناسب.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, minibar, balcony, smartTv, safe, cityView));

        // ── H8 — فندق نابلس الكبير (2 أنواع) ────────────────────────────

        RoomType h8Classic = rt(h8, "غرفة كلاسيك نابلسية", 2,
                BedType.QUEEN, 1, 2, 0, bd("70.00"), 20,
                "غرفة بتصميم يعكس عراقة نابلس القديمة. " +
                        "قريبة من أسواق النحاسين وقيساريات الكنافة الشهيرة.",
                "الحمام التركي التقليدي متاح بسعر إضافي.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, smartTv));

        Amenity spa = findAmenity(amenities, "مركز صحي وسبا فاخر");

        RoomType h8Ottoman = rt(h8, "جناح الحمام العثماني", 2,
                BedType.KING, 1, 2, 0, bd("130.00"), 3,
                "جناح فاخر مع وصول مجاني للحمام التركي العثماني الملحق بالفندق. " +
                        "أسقف مقببة بالحجر الأبيض، ومجلس عربي تقليدي.",
                "حمام تركي مجاني مرتين يومياً.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, spa, minibar, safe, smartTv));

        // ── H9 — فندق أبراهام الخليل (2 أنواع) ──────────────────────────

        RoomType h9Standard = rt(h9, "غرفة قياسية الخليل", 2,
                BedType.QUEEN, 1, 2, 0, bd("60.00"), 18,
                "غرفة بسيطة ومريحة في قلب الخليل، قريبة من المسجد الإبراهيمي " +
                        "ومحلات الزجاج الخليلي الملوّن.",
                "مناسبة للزيارات الدينية والسياحية.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, smartTv));

        RoomType h9Heritage = rt(h9, "جناح الخليلية التراثية", 2,
                BedType.KING, 1, 2, 0, bd("105.00"), 4,
                "جناح مزيّن بأجمل إبداعات الزجاج الخليلي والخزف الفلسطيني. " +
                        "إطلالة على المسجد الإبراهيمي وبلدة الخليل القديمة.",
                "لا تدخين. إطلالة تاريخية لا مثيل لها.",
                RoomTypeStatus.ACTIVE, Set.of(wifi, ac, smartTv, safe, balcony, cityView));

        List<RoomType> candidates = List.of(
                h1Classic, h1Deluxe, h1Family, h1Royal, h1Old, h2Standard,
                h2Heritage, h2Triple, h3Superior, h3Executive, h3Presidential, h4Standard,
                h4Pilgrim, h4Family, h5Garden, h5Vineyard, h5Suite, h6Standard,
                h6Desert, h7Business, h7Roof, h8Classic, h8Ottoman, h9Standard,
                h9Heritage
        );
        for (RoomType roomType : candidates) {
            if (!roomTypeRepository.existsByNameAndHotel(roomType.getName(), roomType.getHotel())) {
                roomTypeRepository.save(roomType);
            }
        }
        List<RoomType> saved = roomTypeRepository.findAll();
        log.info("✅ RoomTypes created: {} total (25 — 1 inactive)", saved.size());
        return roomTypeRepository.findAll();
    }

    private RoomType rt(Hotel hotel, String name, int capacity,
                        BedType bedType, int bedCount, int maxAdults, int maxChildren,
                        BigDecimal basePrice, int totalUnits,
                        String description, String policies,
                        RoomTypeStatus status, Set<Amenity> amenities) {
        RoomType r = new RoomType();
        r.setHotel(hotel);
        r.setName(name);
        r.setCapacity(capacity);
        r.setBedType(bedType);
        r.setBedCount(bedCount);
        r.setMaxAdults(maxAdults);
        r.setMaxChildren(maxChildren);
        r.setBasePrice(basePrice);
        r.setTotalUnits(totalUnits);
        r.setDescription(description);
        r.setPolicies(policies);
        r.setStatus(status);
        r.setAmenities(amenities);
        RoomTypeImage image = new RoomTypeImage();
        image.setRoomType(r);
        image.setImageUrl(roomImageUrl(name, bedType));
        image.setFileName(image.getImageUrl().substring(image.getImageUrl().lastIndexOf('/') + 1).split("\\?")[0]);
        r.getImages().add(image);
        return r;
    }

    private String roomImageUrl(String roomName, BedType bedType) {
        if (roomName != null && roomName.contains("عائ")) {
            return "https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=600&q=80";
        }
        if (roomName != null && roomName.contains("ملكي")) {
            return "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=600&q=80";
        }
        if (roomName != null && roomName.contains("رئاسي")) {
            return "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600&q=80";
        }
        if (roomName != null && (roomName.contains("جناح") || roomName.contains("فيلا"))) {
            return bedType == BedType.KING
                    ? "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=600&q=80"
                    : "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600&q=80";
        }
        if (bedType == BedType.KING) {
            return "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600&q=80";
        }
        if (bedType == BedType.QUEEN) {
            return "https://images.unsplash.com/photo-1564078516393-cf04bd966897?w=600&q=80";
        }
        return "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=600&q=80";
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  6. PRICING RULES — قواعد التسعير الموسمية
    // ═════════════════════════════════════════════════════════════════════════

    private void seedPricingRules() {
        if (pricingRuleRepository.count() > 0) {
            log.info("⏭  PricingRules already exist — skipping.");
            return;
        }

        // موسم رمضان والأعياد الإسلامية (+25%)
        PricingRule ramadan = pr("موسم رمضان الكريم 2026",
                "أسعار موسم رمضان لشهر مارس 2026",
                LocalDate.of(2026, 2, 28), LocalDate.of(2026, 3, 29),
                bd("1.25"), true);

        // موسم عيد الأضحى (+30%)
        PricingRule adha = pr("موسم عيد الأضحى 2026",
                "أسعار فترة عيد الأضحى المبارك وأيام التشريق",
                LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 22),
                bd("1.30"), true);

        // موسم الأعياد المسيحية (+35%) — للحجاج المسيحيين
        PricingRule christmas = pr("موسم أعياد الميلاد والحج المسيحي",
                "ذروة الحجاج المسيحيين في عيد الميلاد والفصح بيت لحم والقدس",
                LocalDate.of(2026, 12, 20), LocalDate.of(2027, 1, 7),
                bd("1.35"), true);

        // موسم الفصح — أبريل (+40% — ذروة السياحة)
        PricingRule easter = pr("موسم الفصح المجيد 2026",
                "أعلى موسم سياحي: أسبوع الآلام وعيد الفصح في القدس وبيت لحم",
                LocalDate.of(2026, 3, 30), LocalDate.of(2026, 4, 10),
                bd("1.40"), true);

        // موسم الصيف (+20%)
        PricingRule summer = pr("موسم الصيف الفلسطيني 2026",
                "أسعار موسم الصيف يونيو - أغسطس 2026",
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31),
                bd("1.20"), true);

        // خصم الشتاء — الموسم المنخفض (-15%)
        PricingRule winter = pr("خصم الشتاء — الموسم المنخفض 2026",
                "أسعار مخفضة لشهري يناير وفبراير — موسم هادئ",
                LocalDate.of(2026, 1, 10), LocalDate.of(2026, 2, 20),
                bd("0.85"), true);

        // عرض مبكر — حجز مبكر الربيع (+10% — محدود)
        PricingRule spring = pr("مهرجان فلسطين للثقافة والسياحة 2026",
                "تزامناً مع مهرجان فلسطين الثقافي السنوي وأسبوع الزيتون",
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 20),
                bd("1.10"), true);

        // rule منتهي (active=false) — لتست
        PricingRule expired = pr("عروض عيد الميلاد 2025 (منتهية)",
                "خصومات موسم الميلاد لعام 2025 — منتهية الصلاحية",
                LocalDate.of(2025, 12, 15), LocalDate.of(2025, 12, 31),
                bd("0.75"), false);

        pricingRuleRepository.saveAll(List.of(
                ramadan, adha, christmas, easter, summer, winter, spring, expired));

        log.info("✅ PricingRules created: 7 active (رمضان, عيد أضحى, ميلاد, فصح, صيف, شتاء, مهرجان) + 1 منتهي");
    }

    private PricingRule pr(String name, String desc,
                           LocalDate start, LocalDate end,
                           BigDecimal multiplier, boolean active) {
        PricingRule r = new PricingRule();
        r.setName(name);
        r.setDescription(desc);
        r.setStartDate(start);
        r.setEndDate(end);
        r.setPriceMultiplier(multiplier);
        r.setActive(active);
        return r;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  7. BOOKINGS — حجوزات متنوعة بكل الحالات
    // ═════════════════════════════════════════════════════════════════════════

    private List<Booking> seedBookings(List<RoomType> roomTypes) {
        if (bookingRepository.count() > 0) {
            log.info("⏭  Bookings already exist — skipping.");
            return bookingRepository.findAll();
        }

        LocalDate today = LocalDate.now();

        // غرف نشطة للحجز
        RoomType h1Classic = findRT(roomTypes, "غرفة كلاسيك مع إطلالة على المدينة");
        RoomType h1Deluxe  = findRT(roomTypes, "جناح ديلوكس مع بالكونة");
        RoomType h1Family  = findRT(roomTypes, "غرفة عائلية توأم");
        RoomType h1Royal   = findRT(roomTypes, "الجناح الملكي — إطلالة الأقصى");
        RoomType h2Standard = findRT(roomTypes, "غرفة قياسية مزدوجة");
        RoomType h2Heritage = findRT(roomTypes, "جناح التراث الفلسطيني");
        RoomType h3Executive = findRT(roomTypes, "غرفة إكزيكيوتف للأعمال");
        RoomType h3Presidential = findRT(roomTypes, "الجناح الرئاسي — قصر فلسطين");
        RoomType h4Standard = findRT(roomTypes, "غرفة قياسية قريبة من المهد");
        RoomType h5Suite    = findRT(roomTypes, "جناح العروسين — سانت جورج");
        RoomType h6Desert   = findRT(roomTypes, "جناح وادي القلط");
        RoomType h7Business = findRT(roomTypes, "غرفة الأعمال التنفيذية");
        RoomType h8Classic  = findRT(roomTypes, "غرفة كلاسيك نابلسية");
        RoomType h9Standard = findRT(roomTypes, "غرفة قياسية الخليل");

        // ── PENDING — حجوزات جديدة لم تُأكد ────────────────────────────

        Booking p1 = bk(h1Classic, "ليلى أبو عمر", "layla@example.ps",
                "+970-59-200-1111", 2, 0,
                today.plusDays(8), today.plusDays(12),
                h1Classic.getBasePrice(), BookingStatus.PENDING,
                "نفضل إطلالة على قبة الصخرة. وصول مبكر إن أمكن.");

        Booking p2 = bk(h4Standard, "James Wilson", "james@example.com",
                "+1-202-555-0199", 2, 0,
                today.plusDays(15), today.plusDays(19),
                h4Standard.getBasePrice(), BookingStatus.PENDING,
                "Christian pilgrimage. Staying for Christmas celebrations.");

        Booking p3 = bk(h2Standard, "عمر الحسين", "omar@example.ps",
                "+970-59-300-2222", 1, 0,
                today.plusDays(3), today.plusDays(6),
                h2Standard.getBasePrice(), BookingStatus.PENDING,
                null);

        Booking p4 = bk(h7Business, "رانيا الخطيب", "rania@example.ps",
                "+970-56-400-3333", 1, 0,
                today.plusDays(5), today.plusDays(7),
                h7Business.getBasePrice(), BookingStatus.PENDING,
                "رحلة عمل. أحتاج فاتورة رسمية.");

        // ── CONFIRMED — حجوزات مؤكدة ────────────────────────────────────

        Booking c1 = bk(h1Deluxe, "خالد النجار", "khalid@example.ps",
                "+970-59-500-4444", 2, 0,
                today.plusDays(10), today.plusDays(14),
                h1Deluxe.getBasePrice(), BookingStatus.CONFIRMED,
                "شهر عسل. يرجى تزيين الغرفة بالورود.");

        Booking c2 = bk(h5Suite, "خالد النجار", "khalid@example.ps",
                "+970-59-500-4444", 2, 0,
                today.plusDays(20), today.plusDays(23),
                h5Suite.getBasePrice(), BookingStatus.CONFIRMED,
                "مفاجأة لزوجتي. لا تذكروا التفاصيل في الرسالة.");

        Booking c3 = bk(h3Presidential, "ليلى أبو عمر", "layla@example.ps",
                "+970-59-200-1111", 2, 0,
                today.plusDays(30), today.plusDays(33),
                h3Presidential.getBasePrice(), BookingStatus.CONFIRMED,
                "VIP. نحتاج خدمة استقبال في المطار.");

        Booking c4 = bk(h1Family, "سارة حداد", "sara@example.ps",
                "+970-56-600-5555", 2, 3,
                today.plusDays(12), today.plusDays(17),
                h1Family.getBasePrice(), BookingStatus.CONFIRMED,
                "عائلة مع 3 أطفال. نحتاج سرير أطفال إضافي.");

        Booking c5 = bk(h2Heritage, "James Wilson", "james@example.com",
                "+1-202-555-0199", 2, 0,
                today.plusDays(25), today.plusDays(28),
                h2Heritage.getBasePrice(), BookingStatus.CONFIRMED,
                "Historical Jerusalem tour group.");

        // ── CANCELLED — حجوزات ملغاة ──────────────────────────────────

        Booking can1 = bk(h1Classic, "عمر الحسين", "omar@example.ps",
                "+970-59-300-2222", 2, 0,
                today.minusDays(15), today.minusDays(12),
                h1Classic.getBasePrice(), BookingStatus.CANCELLED, null);
        can1.setCancelledAt(LocalDateTime.now().minusDays(17));
        can1.setCancellationReason("تغيير في خطط السفر بسبب ظروف عائلية طارئة.");
        can1.setRefundAmount(h1Classic.getBasePrice().multiply(bd("3")));

        Booking can2 = bk(h3Executive, "رانيا الخطيب", "rania@example.ps",
                "+970-56-400-3333", 1, 0,
                today.minusDays(8), today.minusDays(6),
                h3Executive.getBasePrice(), BookingStatus.CANCELLED, null);
        can2.setCancelledAt(LocalDateTime.now().minusDays(10));
        can2.setCancellationReason("تعديل جدول الأعمال — سيتم إعادة الحجز لاحقاً.");
        can2.setRefundAmount(h3Executive.getBasePrice().multiply(bd("2")));

        Booking can3 = bk(h8Classic, "سارة حداد", "sara@example.ps",
                "+970-56-600-5555", 2, 1,
                today.plusDays(45), today.plusDays(48),
                h8Classic.getBasePrice(), BookingStatus.CANCELLED, null);
        can3.setCancelledAt(LocalDateTime.now());
        can3.setCancellationReason("إلغاء مبكر — استرداد كامل.");
        can3.setRefundAmount(h8Classic.getBasePrice().multiply(bd("3")));

        // ── COMPLETED — حجوزات منتهية (للريفيو) ────────────────────────

        Booking done1 = bk(h1Classic, "ليلى أبو عمر", "layla@example.ps",
                "+970-59-200-1111", 2, 0,
                today.minusDays(25), today.minusDays(22),
                h1Classic.getBasePrice(), BookingStatus.COMPLETED, null);

        Booking done2 = bk(h2Heritage, "عمر الحسين", "omar@example.ps",
                "+970-59-300-2222", 2, 0,
                today.minusDays(40), today.minusDays(37),
                h2Heritage.getBasePrice(), BookingStatus.COMPLETED, null);

        Booking done3 = bk(h4Standard, "James Wilson", "james@example.com",
                "+1-202-555-0199", 1, 0,
                today.minusDays(55), today.minusDays(52),
                h4Standard.getBasePrice(), BookingStatus.COMPLETED,
                "Amazing pilgrimage experience!");

        Booking done4 = bk(h6Desert, "خالد النجار", "khalid@example.ps",
                "+970-59-500-4444", 2, 0,
                today.minusDays(20), today.minusDays(17),
                h6Desert.getBasePrice(), BookingStatus.COMPLETED, null);

        Booking done5 = bk(h9Standard, "رانيا الخطيب", "rania@example.ps",
                "+970-56-400-3333", 2, 0,
                today.minusDays(10), today.minusDays(8),
                h9Standard.getBasePrice(), BookingStatus.COMPLETED, null);

        // done6 — بدون ريفيو (لتست تذكير الريفيو)
        Booking done6 = bk(h1Royal, "سارة حداد", "sara@example.ps",
                "+970-56-600-5555", 2, 0,
                today.minusDays(12), today.minusDays(9),
                h1Royal.getBasePrice(), BookingStatus.COMPLETED,
                "ليلة الذكرى. كانت تجربة لا تُنسى!");

        List<Booking> saved = bookingRepository.saveAll(List.of(
                p1, p2, p3, p4,
                c1, c2, c3, c4, c5,
                can1, can2, can3,
                done1, done2, done3, done4, done5, done6));

        log.info("✅ Bookings created: 4 PENDING, 5 CONFIRMED, 3 CANCELLED, 6 COMPLETED");
        return saved;
    }

    private Booking bk(RoomType rt, String name, String email, String phone,
                       int adults, int children, LocalDate in, LocalDate out,
                       BigDecimal ppn, BookingStatus status, String notes) {
        Booking b = new Booking();
        b.setRoomType(rt);
        b.setGuestName(name);
        b.setGuestEmail(email);
        b.setGuestPhone(phone);
        b.setAdults(adults);
        b.setChildren(children);
        b.setTotalGuests(adults + children);
        b.setCheckIn(in);
        b.setCheckOut(out);
        b.setPricePerNight(ppn);
        long nights = java.time.temporal.ChronoUnit.DAYS.between(in, out);
        b.setTotalPrice(ppn.multiply(BigDecimal.valueOf(nights)).multiply(bd("1.16")));
        b.setStatus(status);
        b.setGuestNotes(notes);
        return b;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  8. PAYMENTS — مدفوعات بكل الحالات
    // ═════════════════════════════════════════════════════════════════════════

    private List<Payment> seedPayments(List<Booking> bookings) {
        if (paymentRepository.count() > 0) {
            log.info("⏭  Payments already exist — skipping.");
            return paymentRepository.findAll();
        }

        Booking p1 = findBk(bookings, "layla@example.ps", BookingStatus.PENDING, 0);
        Booking p2 = findBk(bookings, "james@example.com", BookingStatus.PENDING, 0);
        Booking p3 = findBk(bookings, "omar@example.ps", BookingStatus.PENDING, 0);
        Booking p4 = findBk(bookings, "rania@example.ps", BookingStatus.PENDING, 0);
        Booking c1 = findBk(bookings, "khalid@example.ps", BookingStatus.CONFIRMED, 0);
        Booking c2 = findBk(bookings, "khalid@example.ps", BookingStatus.CONFIRMED, 1);
        Booking c3 = findBk(bookings, "layla@example.ps", BookingStatus.CONFIRMED, 0);
        Booking c4 = findBk(bookings, "sara@example.ps", BookingStatus.CONFIRMED, 0);
        Booking c5 = findBk(bookings, "james@example.com", BookingStatus.CONFIRMED, 0);
        Booking can1 = findBk(bookings, "omar@example.ps", BookingStatus.CANCELLED, 0);
        Booking can2 = findBk(bookings, "rania@example.ps", BookingStatus.CANCELLED, 0);
        Booking done1 = findBk(bookings, "layla@example.ps", BookingStatus.COMPLETED, 0);
        Booking done2 = findBk(bookings, "omar@example.ps", BookingStatus.COMPLETED, 0);
        Booking done3 = findBk(bookings, "james@example.com", BookingStatus.COMPLETED, 0);
        Booking done4 = findBk(bookings, "khalid@example.ps", BookingStatus.COMPLETED, 0);
        Booking done5 = findBk(bookings, "rania@example.ps", BookingStatus.COMPLETED, 0);
        Booking done6 = findBk(bookings, "sara@example.ps", BookingStatus.COMPLETED, 0);

        // PENDING payments
        Payment pp1 = pay(p1.getId(), p1.getTotalPrice(), PaymentStatus.PENDING, null, null);
        Payment pp2 = pay(p2.getId(), p2.getTotalPrice(), PaymentStatus.PENDING, null, null);
        Payment pp3 = pay(p3.getId(), p3.getTotalPrice(), PaymentStatus.PENDING, null, null);
        Payment pp4 = pay(p4.getId(), p4.getTotalPrice(), PaymentStatus.PENDING, null, null);

        // FAILED attempt (محاولة فاشلة على p1 قبل البندنق الحالي)
        Payment pf1 = pay(p1.getId(), p1.getTotalPrice(),
                PaymentStatus.FAILED, "البطاقة مرفوضة — رصيد غير كافٍ", null);

        // SUCCESS payments — للحجوزات المؤكدة
        Payment sp1 = pay(c1.getId(), c1.getTotalPrice(), PaymentStatus.SUCCESS,
                null, LocalDateTime.now().minusDays(11));
        Payment sp2 = pay(c2.getId(), c2.getTotalPrice(), PaymentStatus.SUCCESS,
                null, LocalDateTime.now().minusDays(5));
        Payment sp3 = pay(c3.getId(), c3.getTotalPrice(), PaymentStatus.SUCCESS,
                null, LocalDateTime.now().minusDays(2));
        Payment sp4 = pay(c4.getId(), c4.getTotalPrice(), PaymentStatus.SUCCESS,
                null, LocalDateTime.now().minusDays(13));
        Payment sp5 = pay(c5.getId(), c5.getTotalPrice(), PaymentStatus.SUCCESS,
                null, LocalDateTime.now().minusDays(7));

        // SUCCESS — للحجوزات المكتملة
        Payment cp1 = pay(done1.getId(), done1.getTotalPrice(), PaymentStatus.SUCCESS,
                null, LocalDateTime.now().minusDays(26));
        Payment cp2 = pay(done2.getId(), done2.getTotalPrice(), PaymentStatus.SUCCESS,
                null, LocalDateTime.now().minusDays(41));
        Payment cp3 = pay(done3.getId(), done3.getTotalPrice(), PaymentStatus.SUCCESS,
                null, LocalDateTime.now().minusDays(56));
        Payment cp4 = pay(done4.getId(), done4.getTotalPrice(), PaymentStatus.SUCCESS,
                null, LocalDateTime.now().minusDays(21));
        Payment cp5 = pay(done5.getId(), done5.getTotalPrice(), PaymentStatus.SUCCESS,
                null, LocalDateTime.now().minusDays(11));
        Payment cp6 = pay(done6.getId(), done6.getTotalPrice(), PaymentStatus.SUCCESS,
                null, LocalDateTime.now().minusDays(13));

        // REFUNDED — للحجوزات الملغاة
        Payment rp1 = pay(can1.getId(), can1.getTotalPrice(), PaymentStatus.REFUNDED, null,
                LocalDateTime.now().minusDays(18));
        rp1.setRefundReason("إلغاء مبكر — استرداد كامل حسب السياسة.");
        rp1.setRefundedAt(LocalDateTime.now().minusDays(17));

        Payment rp2 = pay(can2.getId(), can2.getTotalPrice(), PaymentStatus.REFUNDED, null,
                LocalDateTime.now().minusDays(11));
        rp2.setRefundReason("إلغاء رحلة عمل — استرداد كامل.");
        rp2.setRefundedAt(LocalDateTime.now().minusDays(10));

        List<Payment> saved = paymentRepository.saveAll(List.of(
                pp1, pp2, pp3, pp4, pf1,
                sp1, sp2, sp3, sp4, sp5,
                cp1, cp2, cp3, cp4, cp5, cp6,
                rp1, rp2));

        log.info("✅ Payments: 4 PENDING, 1 FAILED, 11 SUCCESS, 2 REFUNDED = {} total", saved.size());
        return saved;
    }

    private Payment pay(Long bookingId, BigDecimal amount,
                        PaymentStatus status, String failReason, LocalDateTime paidAt) {
        Payment p = new Payment();
        p.setBookingId(bookingId);
        p.setAmount(amount);
        p.setCurrency("USD");
        p.setMethod(PaymentMethod.MOCK_CARD);
        p.setStatus(status);
        p.setProviderName("MOCK_GATEWAY");
        p.setTransactionReference("TRS-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12).toUpperCase());
        p.setFailureReason(failReason);
        p.setPaidAt(paidAt);
        return p;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  9. REVIEWS — تقييمات حقيقية بالعربية والإنجليزية
    // ═════════════════════════════════════════════════════════════════════════

    private void seedReviews(List<Booking> bookings, List<Hotel> hotels) {
        if (reviewRepository.count() > 0) {
            log.info("⏭  Reviews already exist — skipping.");
            return;
        }

        Hotel h1 = hotels.get(0); // فندق الكرمل القدس
        Hotel h2 = hotels.get(1); // فندق دار السلام
        Hotel h4 = hotels.get(3); // فندق جورج بيت لحم
        Hotel h6 = hotels.get(5); // فندق أريحا الريف
        Hotel h9 = hotels.get(8); // فندق أبراهام الخليل

        Booking done1 = findBk(bookings, "layla@example.ps", BookingStatus.COMPLETED, 0);
        Booking done2 = findBk(bookings, "omar@example.ps", BookingStatus.COMPLETED, 0);
        Booking done3 = findBk(bookings, "james@example.com", BookingStatus.COMPLETED, 0);
        Booking done4 = findBk(bookings, "khalid@example.ps", BookingStatus.COMPLETED, 0);
        Booking done5 = findBk(bookings, "rania@example.ps", BookingStatus.COMPLETED, 0);
        // done6 (sara) — بدون ريفيو عمداً

        // ── تقييم ممتاز 5★ — ليلى في الكرمل القدس ────────────────────────
        Review r1 = new Review();
        r1.setBooking(done1);
        r1.setHotelId(h1.getId());
        r1.setGuestEmail("layla@example.ps");
        r1.setRating(5);
        r1.setComment("تجربة لا تُنسى في قلب القدس! الفندق يقع بالقرب من قبة الصخرة " +
                "والمسجد الأقصى خطوات. الموظفون محترفون للغاية، والفطور بوفيه رائع " +
                "بالأكلات الفلسطينية الأصيلة. الإطلالة من الشرفة على أسوار المدينة القديمة " +
                "سحرية في الفجر والغروب. سأعود حتماً!");

        // ── تقييم جيد 4★ — عمر في دار السلام ────────────────────────────
        Review r2 = new Review();
        r2.setBooking(done2);
        r2.setHotelId(h2.getId());
        r2.setGuestEmail("omar@example.ps");
        r2.setRating(4);
        r2.setComment("فندق رائع بتصميم عربي أصيل وساحة داخلية هادئة ومريحة جداً. " +
                "الموقع ممتاز وقريب من كل المعالم. الطعام لذيذ والغرف نظيفة. " +
                "نقطة الضعف الوحيدة: الإنترنت كان بطيئاً أحياناً في المساء. " +
                "بشكل عام تجربة ممتعة وسأنصح بها الأصدقاء.");

        // ── تقييم إيجابي 5★ — جيمس في فندق جورج بيت لحم (إنجليزي) ───────
        Review r3 = new Review();
        r3.setBooking(done3);
        r3.setHotelId(h4.getId());
        r3.setGuestEmail("james@example.com");
        r3.setRating(5);
        r3.setComment("Absolutely magnificent! Staying at Hotel George was the highlight " +
                "of our Holy Land pilgrimage. The location is literally steps from the " +
                "Church of the Nativity. The Palestinian staff were incredibly warm and " +
                "welcoming. The food was authentic and delicious. Highly recommended " +
                "for Christian pilgrims and tourists alike!");

        // ── تقييم متوسط 3★ — خالد في أريحا الريف ─────────────────────────
        Review r4 = new Review();
        r4.setBooking(done4);
        r4.setHotelId(h6.getId());
        r4.setGuestEmail("khalid@example.ps");
        r4.setRating(3);
        r4.setComment("الفندق في موقع جميل وسط نخيل أريحا، والجو رائع في الشتاء. " +
                "لكن المسبح كان مغلقاً للصيانة خلال إقامتي وهذا مخيّب للآمال. " +
                "الخدمة مقبولة وليست مميزة. الغرفة نظيفة لكن الأثاث قديم نوعاً ما. " +
                "السعر معقول نسبياً مقارنة بالمستوى المقدم.");

        // ── تقييم ضعيف 2★ — رانيا في أبراهام الخليل ──────────────────────
        Review r5 = new Review();
        r5.setBooking(done5);
        r5.setHotelId(h9.getId());
        r5.setGuestEmail("rania@example.ps");
        r5.setRating(2);
        r5.setComment("الموقع ممتاز قريب من المسجد الإبراهيمي لكن الخدمة كانت مخيبة للآمال. " +
                "الغرفة لم تكن نظيفة تماماً عند الوصول، " +
                "والمكيف لم يعمل بشكل صحيح طوال الليل. " +
                "فريق الاستقبال غير متجاوب مع الطلبات. " +
                "لم تصل خدمة الغرف رغم طلبها مرتين. لن أعود.");

        reviewRepository.saveAll(List.of(r1, r2, r3, r4, r5));
        log.info("✅ Reviews: 2×5★, 1×4★, 1×3★, 1×2★ (1 COMPLETED booking without review)");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  10. WAITING LIST — قوائم انتظار
    // ═════════════════════════════════════════════════════════════════════════

    private void seedWaitingList(List<RoomType> roomTypes, List<Hotel> hotels) {
        if (waitingListRepository.count() > 0) {
            log.info("⏭  WaitingList already exist — skipping.");
            return;
        }

        LocalDate today = LocalDate.now();
        Hotel h1 = hotels.get(0);
        Hotel h3 = hotels.get(2);
        Hotel h5 = hotels.get(4);

        RoomType h1Royal = findRT(roomTypes, "الجناح الملكي — إطلالة الأقصى");
        RoomType h1Deluxe = findRT(roomTypes, "جناح ديلوكس مع بالكونة");
        RoomType h3Presidential = findRT(roomTypes, "الجناح الرئاسي — قصر فلسطين");
        RoomType h5Suite = findRT(roomTypes, "جناح العروسين — سانت جورج");

        // WAITING — عمر ينتظر الجناح الملكي
        WaitingListEntry wl1 = wl(h1Royal, h1,
                "omar@example.ps", "عمر الحسين",
                today.plusDays(8), today.plusDays(12),
                WaitingListStatus.WAITING, null,
                "الجناح الملكي — إطلالة الأقصى", h1.getName());

        // WAITING — جيمس ينتظر الجناح الرئاسي للمؤتمر
        WaitingListEntry wl2 = wl(h3Presidential, h3,
                "james@example.com", "James Wilson",
                today.plusDays(20), today.plusDays(25),
                WaitingListStatus.WAITING, null,
                "الجناح الرئاسي — قصر فلسطين", h3.getName());

        // WAITING — رانيا تنتظر جناح العروسين
        WaitingListEntry wl3 = wl(h5Suite, h5,
                "rania@example.ps", "رانيا الخطيب",
                today.plusDays(15), today.plusDays(18),
                WaitingListStatus.WAITING, null,
                "جناح العروسين — سانت جورج", h5.getName());

        // NOTIFIED — ليلى أُشعرت قبل 3 ساعات (21 ساعة باقية)
        WaitingListEntry wl4 = wl(h1Deluxe, h1,
                "layla@example.ps", "ليلى أبو عمر",
                today.plusDays(10), today.plusDays(14),
                WaitingListStatus.NOTIFIED,
                LocalDateTime.now().minusHours(3),
                "جناح ديلوكس مع بالكونة", h1.getName());

        // EXPIRED — سارة انتهت مدتها (أُشعرت قبل 26 ساعة)
        WaitingListEntry wl5 = wl(h1Royal, h1,
                "sara@example.ps", "سارة حداد",
                today.plusDays(5), today.plusDays(8),
                WaitingListStatus.EXPIRED,
                LocalDateTime.now().minusHours(26),
                "الجناح الملكي — إطلالة الأقصى", h1.getName());

        // EXPIRED — تاريخ الحجز فات
        WaitingListEntry wl6 = wl(h1Deluxe, h1,
                "khalid@example.ps", "خالد النجار",
                today.minusDays(8), today.minusDays(5),
                WaitingListStatus.EXPIRED, null,
                "جناح ديلوكس مع بالكونة", h1.getName());

        // CANCELLED — خالد ألغى انتظاره
        WaitingListEntry wl7 = wl(h3Presidential, h3,
                "khalid@example.ps", "خالد النجار",
                today.plusDays(25), today.plusDays(28),
                WaitingListStatus.CANCELLED, null,
                "الجناح الرئاسي — قصر فلسطين", h3.getName());

        waitingListRepository.saveAll(List.of(wl1, wl2, wl3, wl4, wl5, wl6, wl7));
        log.info("✅ WaitingList: 3 WAITING, 1 NOTIFIED, 2 EXPIRED, 1 CANCELLED");
    }

    private WaitingListEntry wl(RoomType rt, Hotel hotel,
                                String email, String name,
                                LocalDate in, LocalDate out,
                                WaitingListStatus status, LocalDateTime notifiedAt,
                                String rtName, String hotelName) {
        WaitingListEntry e = new WaitingListEntry();
        e.setRoomTypeId(rt.getId());
        e.setHotelId(hotel.getId());
        e.setGuestEmail(email);
        e.setGuestName(name);
        e.setCheckIn(in);
        e.setCheckOut(out);
        e.setStatus(status);
        e.setNotifiedAt(notifiedAt);
        e.setRoomTypeName(rtName);
        e.setHotelName(hotelName);
        return e;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  11. NOTIFICATIONS — إشعارات بكل الأنواع والحالات
    // ═════════════════════════════════════════════════════════════════════════

    private void seedNotifications(List<Booking> bookings, List<Payment> payments) {
        if (notificationRepository.count() > 0) {
            log.info("⏭  Notifications already exist — skipping.");
            return;
        }

        Booking confirmed1 = findBk(bookings, "khalid@example.ps", BookingStatus.CONFIRMED, 0);
        Booking confirmed3 = findBk(bookings, "layla@example.ps", BookingStatus.CONFIRMED, 0);
        Booking cancelled1 = findBk(bookings, "omar@example.ps", BookingStatus.CANCELLED, 0);
        Booking pending1   = findBk(bookings, "layla@example.ps", BookingStatus.PENDING, 0);
        Booking done1      = findBk(bookings, "layla@example.ps", BookingStatus.COMPLETED, 0);

        // BOOKING_CONFIRMED — مُرسل
        Notification n1 = notif("khalid@example.ps", "خالد النجار",
                NotificationType.BOOKING_CONFIRMED, NotificationStatus.SENT,
                "تم تأكيد حجزك — فندق الكرمل القدس 🎉",
                "عزيزي خالد النجار، يسعدنا إبلاغك بأن حجزك رقم BK-" + confirmed1.getId() +
                        " قد تم تأكيده بنجاح.\n\nالفندق: فندق الكرمل القدس\nتاريخ الوصول: " +
                        confirmed1.getCheckIn() + " الساعة 2:00 ظهراً\nتاريخ المغادرة: " +
                        confirmed1.getCheckOut() + " الساعة 12:00 ظهراً\nالمبلغ الإجمالي: $" +
                        confirmed1.getTotalPrice() + "\n\nنتطلع لاستقبالك بحفاوة.\nفريق TerraStay",
                confirmed1.getId(), ReferenceType.BOOKING,
                LocalDateTime.now().minusDays(11), LocalDateTime.now().minusDays(11));

        // BOOKING_PENDING — مُرسل
        Notification n2 = notif("layla@example.ps", "ليلى أبو عمر",
                NotificationType.BOOKING_PENDING, NotificationStatus.SENT,
                "استلمنا طلب حجزك — بانتظار التأكيد",
                "عزيزتنا ليلى أبو عمر، استلمنا طلب حجزك رقم BK-" + pending1.getId() +
                        " وهو الآن قيد المراجعة. سنُعلمك فور تأكيده.\n\nشكراً لثقتك بـ TerraStay.",
                pending1.getId(), ReferenceType.BOOKING,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1));

        // BOOKING_CANCELLED — مُرسل
        Notification n3 = notif("omar@example.ps", "عمر الحسين",
                NotificationType.BOOKING_CANCELLED, NotificationStatus.SENT,
                "تم إلغاء حجزك — استرداد كامل",
                "عزيزنا عمر الحسين، تم إلغاء حجزك رقم BK-" + cancelled1.getId() +
                        " بنجاح.\nسبب الإلغاء: " + cancelled1.getCancellationReason() +
                        "\nمبلغ الاسترداد: $" + cancelled1.getRefundAmount() +
                        "\nسيظهر في حسابك خلال 5-7 أيام عمل.\n\nTerraStay",
                cancelled1.getId(), ReferenceType.BOOKING,
                LocalDateTime.now().minusDays(17), LocalDateTime.now().minusDays(17));

        // PAYMENT_SUCCESS — مُرسل
        Notification n4 = notif("khalid@example.ps", "خالد النجار",
                NotificationType.PAYMENT_SUCCESS, NotificationStatus.SENT,
                "✅ تم استلام دفعتك بنجاح — $" + confirmed1.getTotalPrice(),
                "عزيزنا خالد، تمت معالجة دفعتك بمبلغ $" + confirmed1.getTotalPrice() +
                        " بنجاح.\nرقم المعاملة: TRS-XXXX\nالطريقة: بطاقة ائتمانية\n\nTerraStay",
                confirmed1.getId(), ReferenceType.PAYMENT,
                LocalDateTime.now().minusDays(11), LocalDateTime.now().minusDays(11));

        // PAYMENT_FAILED — مُرسل
        Notification n5 = notif("layla@example.ps", "ليلى أبو عمر",
                NotificationType.PAYMENT_FAILED, NotificationStatus.SENT,
                "❌ فشلت عملية الدفع — يرجى المحاولة مجدداً",
                "عزيزتنا ليلى، للأسف لم تتم معالجة دفعتك.\nالسبب: البطاقة مرفوضة — رصيد غير كافٍ.\n" +
                        "يرجى تحديث بيانات الدفع والمحاولة مجدداً.\n\nTerraStay",
                pending1.getId(), ReferenceType.PAYMENT,
                LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(3));

        // PAYMENT_REFUNDED — مُرسل
        Notification n6 = notif("omar@example.ps", "عمر الحسين",
                NotificationType.PAYMENT_REFUNDED, NotificationStatus.SENT,
                "💰 تم استرداد مبلغك — $" + cancelled1.getTotalPrice(),
                "عزيزنا عمر، تم استرداد مبلغ $" + cancelled1.getTotalPrice() +
                        " إلى حسابك البنكي.\nسيظهر خلال 5-7 أيام عمل.\n\nTerraStay",
                cancelled1.getId(), ReferenceType.PAYMENT,
                LocalDateTime.now().minusDays(17), LocalDateTime.now().minusDays(16));

        // BOOKING_REMINDER — مُرسل
        Notification n7 = notif("khalid@example.ps", "خالد النجار",
                NotificationType.BOOKING_REMINDER, NotificationStatus.SENT,
                "⏰ تذكير: تسجيل وصولك غداً — فندق الكرمل القدس",
                "عزيزنا خالد، نذكّرك بأن تسجيل وصولك بفندق الكرمل القدس غداً " +
                        "الساعة 2:00 ظهراً.\n\nنتطلع لاستقبالك.\nفريق TerraStay",
                confirmed1.getId(), ReferenceType.BOOKING,
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(2));

        // REVIEW_REMINDER — معلق (لم يُرسل بعد)
        Notification n8 = notif("sara@example.ps", "سارة حداد",
                NotificationType.REVIEW_REMINDER, NotificationStatus.PENDING,
                "⭐ شاركينا تجربتك — الجناح الملكي، القدس",
                "عزيزتنا سارة، نأمل أن إقامتك في الجناح الملكي بفندق الكرمل كانت استثنائية. " +
                        "هل يمكنك مشاركتنا رأيك؟ رأيك يساعدنا على التحسين المستمر.\n\nTerraStay",
                null, ReferenceType.SYSTEM,
                null, null);

        // ROOM_AVAILABLE — مُرسل (waiting list notification)
        Notification n9 = notif("layla@example.ps", "ليلى أبو عمر",
                NotificationType.ROOM_AVAILABLE, NotificationStatus.SENT,
                "🎉 أصبح الجناح الديلوكس متاحاً — أسرعي بالحجز!",
                "عزيزتنا ليلى، أخبار رائعة! جناح ديلوكس مع بالكونة " +
                        "في فندق الكرمل القدس أصبح متاحاً للتواريخ التي طلبتِها. " +
                        "لديكِ 24 ساعة فقط لإتمام الحجز قبل أن يُعرض للآخرين.\n\nTerraStay",
                null, ReferenceType.SYSTEM,
                LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(3));

        // WELCOME_EMAIL — مُرسل
        Notification n10 = notif("layla@example.ps", "ليلى أبو عمر",
                NotificationType.WELCOME_EMAIL, NotificationStatus.SENT,
                "مرحباً بك في TerraStay — إقامتك الفاخرة تبدأ من هنا",
                "عزيزتنا ليلى أبو عمر، أهلاً وسهلاً بك في TerraStay!\n" +
                        "منصة الحجوزات الفندقية الفلسطينية الأولى.\n" +
                        "ابدئي اكتشاف أجمل فنادق القدس وبيت لحم وأريحا ورام الله.\n\nTerraStay",
                null, ReferenceType.SYSTEM,
                LocalDateTime.now().minusDays(60), LocalDateTime.now().minusDays(60));

        // FAILED — فشل نهائي (بريد غير صالح)
        Notification n11 = notif("amira@example.ps", "أميرة سلامة",
                NotificationType.BOOKING_CONFIRMED, NotificationStatus.PERMANENTLY_FAILED,
                "تأكيد الحجز",
                "تم تأكيد حجزك.",
                null, ReferenceType.BOOKING,
                null, null);
        n11.setRetryCount(3);
        n11.setErrorMessage("SMTP error: recipient address blocked");

        // RETRY_SCHEDULED — في قائمة إعادة المحاولة
        Notification n12 = notif("james@example.com", "James Wilson",
                NotificationType.PAYMENT_SUCCESS, NotificationStatus.RETRY_SCHEDULED,
                "Payment Confirmed",
                "Your payment has been successfully processed.",
                null, ReferenceType.PAYMENT,
                null, null);
        n12.setRetryCount(1);
        n12.setErrorMessage("Temporary SMTP timeout — scheduled for retry");
        n12.setNextRetryAt(LocalDateTime.now().plusMinutes(25));

        // CUSTOM — عرض خاص
        Notification n13 = notif("khalid@example.ps", "خالد النجار",
                NotificationType.CUSTOM, NotificationStatus.SENT,
                "🎁 عرض حصري لنزلائنا المميزين — خصم 20%",
                "عزيزنا خالد النجار، بصفتك ضيفاً مميزاً في TerraStay، " +
                        "نقدم لك خصماً حصرياً 20% على حجزك القادم.\n" +
                        "استخدم الكود: VIPPAL20\nصالح حتى نهاية الشهر.\n\nTerraStay",
                null, ReferenceType.SYSTEM,
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(3));

        notificationRepository.saveAll(List.of(
                n1, n2, n3, n4, n5, n6, n7, n8, n9, n10, n11, n12, n13));

        log.info("✅ Notifications: 13 (كل الأنواع والحالات مغطاة)");
    }

    private Notification notif(String email, String name,
                               NotificationType type, NotificationStatus status,
                               String subject, String body,
                               Long refId, ReferenceType refType,
                               LocalDateTime createdAt, LocalDateTime sentAt) {
        Notification n = Notification.builder()
                .recipientEmail(email)
                .recipientName(name)
                .type(type)
                .status(status)
                .subject(subject)
                .body(body)
                .referenceId(refId)
                .referenceType(refType)
                .retryCount(0)
                .build();
        if (createdAt != null) n.setCreatedAt(createdAt);
        if (sentAt != null) n.setSentAt(sentAt);
        return n;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  LOOKUP HELPERS
    // ═════════════════════════════════════════════════════════════════════════

    private User findUser(List<User> list, String email) {
        return list.stream()
                .filter(u -> email.equals(u.getEmail()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));
    }

    private Amenity findAmenity(List<Amenity> list, String name) {
        return list.stream()
                .filter(a -> name.equals(a.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Amenity not found: " + name));
    }

    private RoomType findRT(List<RoomType> list, String name) {
        return list.stream()
                .filter(r -> name.equals(r.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("RoomType not found: " + name));
    }

    private Booking findBk(List<Booking> list, String email,
                           BookingStatus status, int index) {
        List<Booking> matches = list.stream()
                .filter(b -> email.equals(b.getGuestEmail()) && b.getStatus() == status)
                .toList();
        if (matches.isEmpty())
            throw new IllegalStateException("Booking not found: " + email + "/" + status);
        return matches.get(Math.min(index, matches.size() - 1));
    }

    private BigDecimal bd(String val) {
        return new BigDecimal(val);
    }
}


