package com.swer313.projectstep1.auth;

import com.swer313.projectstep1.security.JwtService;
import com.swer313.projectstep1.user.DuplicateUserEmailException;
import com.swer313.projectstep1.user.User;
import com.swer313.projectstep1.user.UserRepository;
import com.swer313.projectstep1.user.UserResponseDTO;
import com.swer313.projectstep1.user.UserRole;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           AuthenticationManager authenticationManager) {
        this.userRepository        = userRepository;
        this.passwordEncoder       = passwordEncoder;
        this.jwtService            = jwtService;
        this.authenticationManager = authenticationManager;
    }

    // ── Register ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserEmailException(
                    "Email already registered: " + email);
        }

        User user = new User(
                request.getFullName(),
                email,
                passwordEncoder.encode(request.getPassword()),
                request.getPhone(),
                UserRole.GUEST          // التسجيل العادي = GUEST دائماً
        );

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, new UserResponseDTO(user));
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Override
    public AuthResponse login(LoginRequest request) {

        // Spring Security يتحقق من الـ credentials تلقائياً
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().trim().toLowerCase(),
                        request.getPassword()
                )
        );

        User user = userRepository
                .findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, new UserResponseDTO(user));
    }
}