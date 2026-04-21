package com.swer313.projectstep1.security;

import com.swer313.projectstep1.user.User;
import com.swer313.projectstep1.user.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("No authenticated user found");
        }

        return auth.getName();
    }

    public User getCurrentUser() {
        String email = getCurrentUserEmail();

        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }

    public boolean isAdmin() {
        return getCurrentUser().getRole().name().equals("ADMIN");
    }

    public boolean isManager() {
        return getCurrentUser().getRole().name().equals("MANAGER");
    }

    public boolean isGuest() {
        return getCurrentUser().getRole().name().equals("GUEST");
    }
}