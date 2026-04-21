package com.swer313.projectstep1.security;

import com.swer313.projectstep1.user.User;
import com.swer313.projectstep1.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CurrentUserService service;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserEmail_throwsWhenNoAuth() {
        SecurityContextHolder.clearContext();
        assertThrows(IllegalStateException.class, () -> service.getCurrentUserEmail());
    }

    @Test
    void getCurrentUser_returnsFromRepository() {
        Authentication auth = org.mockito.Mockito.mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("u@e.com");

        SecurityContext ctx = org.mockito.Mockito.mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        User u = new User(); u.setEmail("u@e.com");
        when(userRepository.findByEmailIgnoreCase("u@e.com")).thenReturn(Optional.of(u));

        User found = service.getCurrentUser();
        assertNotNull(found);
        assertEquals("u@e.com", found.getEmail());
    }
}

