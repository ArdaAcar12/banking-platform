package com.bank.banking_platform.auth.security;

import com.bank.banking_platform.auth.entity.Role;
import com.bank.banking_platform.auth.entity.User;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security'nin kimlik doğrulama bağlamında kullanılan UserDetails implementasyonu.
 * Şifre hash'i burada saklanır ancak dışarıya asla sızdırılmaz.
 */
public class CustomUserDetails implements UserDetails {

    private final java.util.UUID id;
    private final String email;
    private final String username;
    private final String password;
    private final Role role;
    private final boolean enabled;
    private final boolean accountNonLocked;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.password = user.getPasswordHash();
        this.role = user.getRole();
        this.enabled = user.isEnabled();
        this.accountNonLocked = user.isAccountNonLocked();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public java.util.UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
