package com.bank.banking_platform.auth.repository;

import com.bank.banking_platform.auth.entity.Role;
import com.bank.banking_platform.auth.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Kullanıcı veritabanı işlemleri.
 * E-posta küçük harfe normalize edilerek saklandığı için aramalar da küçük harf üzerinden yapılır.
 */
@Repository
public interface UserRepository extends JpaRepository<User, java.util.UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);
}
