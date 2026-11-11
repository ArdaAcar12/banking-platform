package com.bank.banking_platform.auth.repository;

import com.bank.banking_platform.auth.entity.RefreshToken;
import com.bank.banking_platform.auth.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Refresh Token veritabanı işlemleri.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, java.util.UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUser(User user);

    @Query("SELECT r FROM RefreshToken r WHERE r.tokenHash = :tokenHash AND r.user = :user")
    Optional<RefreshToken> findByTokenHashAndUser(@Param("tokenHash") String tokenHash, @Param("user") User user);

    void deleteByUser(User user);
}
