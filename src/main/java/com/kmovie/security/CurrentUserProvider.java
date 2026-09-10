package com.kmovie.security;

import com.kmovie.entity.User;
import com.kmovie.exception.ApiException;
import com.kmovie.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Small helper for controllers/services to resolve the logged in User entity. */
@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPrincipal;
    }

    public UserPrincipal getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw ApiException.unauthorized("Authentication required");
        }
        return principal;
    }

    public User getUser() {
        UserPrincipal principal = getPrincipal();
        return userRepository.findByIdAndIsDeletedFalse(principal.getId())
                .orElseThrow(() -> ApiException.unauthorized("Authentication required"));
    }
}
