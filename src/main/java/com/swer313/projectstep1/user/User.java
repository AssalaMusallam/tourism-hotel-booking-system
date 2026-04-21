package com.swer313.projectstep1.user;

import com.swer313.projectstep1.catalog.hotel.Hotel;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * نفس كلاس User القديم بالضبط — الإضافة الوحيدة هي حقل passwordHash.
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_users_email",  columnList = "email"),
                @Index(name = "idx_users_role",   columnList = "role"),
                @Index(name = "idx_users_active", columnList = "active")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "fullName is required")
    @Size(max = 150, message = "fullName must be at most 150 characters")
    @Column(nullable = false, length = 150)
    private String fullName;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    @Size(max = 200, message = "email must be at most 200 characters")
    @Column(nullable = false, length = 200)
    private String email;

    // ── NEW: كلمة المرور المشفّرة (BCrypt) ───────────────────────────────────
    @Column(nullable = false, length = 100)
    private String passwordHash;
    // ─────────────────────────────────────────────────────────────────────────

    @Size(max = 30, message = "phone must be at most 30 characters")
    @Column(length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.GUEST;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "manager_hotels",
            joinColumns        = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "hotel_id")
    )
    private Set<Hotel> managedHotels = new HashSet<>();

    // Getter فقط — لا setter مباشر، نتحكم عبر methods
    public Set<Hotel> getManagedHotels() { return managedHotels; }

    public void addManagedHotel(Hotel hotel) {
        managedHotels.add(hotel);
    }

    public void removeManagedHotel(Hotel hotel) {
        managedHotels.remove(hotel);
    }

    public boolean managesHotel(Long hotelId) {
        return managedHotels.stream()
                .anyMatch(h -> h.getId().equals(hotelId));
    }

    // ── Constructors ─────────────────────────────────────────────────────────

    public User() {}

    public User(String fullName, String email, String passwordHash,
                String phone, UserRole role) {
        this.fullName     = fullName;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.phone        = phone;
        this.role         = (role == null ? UserRole.GUEST : role);
    }

    // ── JPA callbacks ────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.email    != null) this.email    = this.email.trim().toLowerCase();
        if (this.fullName != null) this.fullName = this.fullName.trim();
        if (this.phone    != null) this.phone    = this.phone.trim();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
        if (this.email    != null) this.email    = this.email.trim().toLowerCase();
        if (this.fullName != null) this.fullName = this.fullName.trim();
        if (this.phone    != null) this.phone    = this.phone.trim();
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }

    public String getFullName()            { return fullName; }
    public void setFullName(String n)      { this.fullName = n; }

    public String getEmail()               { return email; }
    public void setEmail(String e)         { this.email = e; }

    public String getPasswordHash()        { return passwordHash; }
    public void setPasswordHash(String p)  { this.passwordHash = p; }

    public String getPhone()               { return phone; }
    public void setPhone(String p)         { this.phone = p; }

    public UserRole getRole()              { return role; }
    public void setRole(UserRole r)        { this.role = r; }

    public boolean isActive()              { return active; }
    public void setActive(boolean a)       { this.active = a; }

    public Instant getCreatedAt()          { return createdAt; }
    public Instant getUpdatedAt()          { return updatedAt; }
}