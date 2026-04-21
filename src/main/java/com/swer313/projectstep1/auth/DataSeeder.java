package com.swer313.projectstep1.auth;
import com.swer313.projectstep1.catalog.hotel.Hotel;
import com.swer313.projectstep1.catalog.hotel.HotelRepository;
import com.swer313.projectstep1.user.User;
import com.swer313.projectstep1.user.UserRepository;
import com.swer313.projectstep1.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
/**
 * يشتغل مرة واحدة عند بدء التطبيق.
 * لو ما في ADMIN في الداتابيس → ينشئ واحد تلقائياً.
 *
 * بيانات الـ Admin الافتراضية:
 *   Email    : admin@hotel.com
 *   Password : Admin@1234
 *
 * ⚠️ غيّر الباسورد فوراً بعد أول تشغيل في production

 */

@Profile("dev")
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final HotelRepository hotelRepository;
    public DataSeeder(UserRepository userRepository,
                      PasswordEncoder passwordEncoder, HotelRepository hotelRepository) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.hotelRepository = hotelRepository;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedManager();
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    private void seedAdmin() {
        String email = "admin@hotel.com";

        if (userRepository.existsByEmail(email)) {
            log.info("Admin user already exists — skipping seed.");
            return;
        }

        User admin = new User(
                "System Admin",
                email,
                passwordEncoder.encode("Admin@1234"),
                null,
                UserRole.ADMIN
        );

        userRepository.save(admin);
        log.info("✅ Admin user created → email: {}", email);
    }

    // ── Manager ───────────────────────────────────────────────────────────────

    private void seedManager() {
        String email = "manager@hotel.com";

        if (userRepository.existsByEmail(email)) {
            log.info("Manager user already exists — skipping seed.");
            return;
        }

        // اجلب أول فندق موجود في الداتابيس
        Hotel hotel = hotelRepository.findAll().stream()
                .findFirst()
                .orElse(null);

        User manager = new User(
                "Hotel Manager",
                email,
                passwordEncoder.encode("Manager@1234"),
                null,
                UserRole.MANAGER
        );

        if (hotel != null) {
            manager.addManagedHotel(hotel);
            log.info("✅ Manager linked to hotel: {}", hotel.getName());
        } else {
            log.warn("⚠️ No hotel found — manager created without hotel assignment");
        }

        userRepository.save(manager);
        log.info("✅ Manager user created → email: {}", email);
    }
}
