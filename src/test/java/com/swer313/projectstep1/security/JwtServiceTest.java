package com.swer313.projectstep1.security;

import com.swer313.projectstep1.user.User;
import com.swer313.projectstep1.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setup() throws Exception {
        jwtService = new JwtService();
        // set secretKey (base64) and expiration
        String rawKey = "01234567890123456789012345678901"; // 32 bytes = 256 bits
        String b64 = Base64.getEncoder().encodeToString(rawKey.getBytes());

        Field f1 = JwtService.class.getDeclaredField("secretKey");
        f1.setAccessible(true);
        f1.set(jwtService, b64);

        Field f2 = JwtService.class.getDeclaredField("expirationMs");
        f2.setAccessible(true);
        f2.setLong(jwtService, 1000L * 60 * 60);
    }

    @Test
    void generate_and_extract_and_validate() {
        User u = new User();
        u.setEmail("test@x.com");
        u.setRole(UserRole.GUEST);
        u.setId(55L);

        String token = jwtService.generateToken(u);
        assertNotNull(token);

        String username = jwtService.extractUsername(token);
        assertEquals("test@x.com", username);

        UserDetails ud = mock(UserDetails.class);
        org.mockito.Mockito.when(ud.getUsername()).thenReturn("test@x.com");

        assertTrue(jwtService.isTokenValid(token, ud));
    }
}

