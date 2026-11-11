package com.bank.banking_platform.auth.security;

import com.bank.banking_platform.auth.entity.User;
import com.bank.banking_platform.auth.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security'nin {@link UserDetailsService} implementasyonu.
 * Kullanıcıyı username'e göre veritabanından yükler ve {@link CustomUserDetails} olarak döner.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı."));
        return new CustomUserDetails(user);
    }
}
