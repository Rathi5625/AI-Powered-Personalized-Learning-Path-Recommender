package com.learningpath.security;

import com.learningpath.entity.User;
import com.learningpath.entity.enums.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Getter
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final String fullName;
    private final String password;
    private final UserRole role;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(UUID id, String email, String fullName, String password, UserRole role, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.role = role != null ? role : UserRole.USER;
        this.authorities = authorities;
    }

    public UserPrincipal(UUID id, String email, String fullName, String password, Collection<? extends GrantedAuthority> authorities) {
        this(id, email, fullName, password, UserRole.USER, authorities);
    }

    public static UserPrincipal create(User user) {
        UserRole userRole = user.getRole() != null ? user.getRole() : UserRole.USER;
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPasswordHash(),
                userRole,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userRole.name()))
        );
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
