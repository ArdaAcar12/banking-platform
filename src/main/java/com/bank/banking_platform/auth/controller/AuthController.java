package com.bank.banking_platform.auth.controller;

import com.bank.banking_platform.auth.dto.ApiError;
import com.bank.banking_platform.auth.dto.AuthResponse;
import com.bank.banking_platform.auth.dto.LoginRequest;
import com.bank.banking_platform.auth.dto.LogoutRequest;
import com.bank.banking_platform.auth.dto.RegisterRequest;
import com.bank.banking_platform.auth.dto.RefreshRequest;
import com.bank.banking_platform.auth.dto.UserResponse;
import com.bank.banking_platform.auth.entity.User;
import com.bank.banking_platform.auth.exception.AuthenticationException;
import com.bank.banking_platform.auth.repository.UserRepository;
import com.bank.banking_platform.auth.security.CustomUserDetails;
import com.bank.banking_platform.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kimlik doğrulama endpoint'leri.
 * Kayıt, giriş, token yenileme, çıkış ve oturumdaki kullanıcı bilgileri burada sağlanır.
 */
@Validated
@RestController
@RequestMapping(path = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Kullanıcı kimlik doğrulama işlemleri (kayıt, giriş, token yenileme, çıkış)")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @Operation(
        summary = "Kullanıcı kaydı",
        description = "Yeni kullanıcı kaydı oluşturur. Varsayılan rol CUSTOMER'dır. " +
            "Genel kayıt endpoint'i üzerinden ADMIN veya SUPPORT rolü atanamaz."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Kayıt başarılı",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Geçersiz istek / doğrulama hatası",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "409", description = "E-posta veya kullanıcı adı zaten kayıtlı",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Giriş yap",
        description = "E-posta veya kullanıcı adı ile giriş yapar. Başarılı olduğunda " +
            "Access Token ve Refresh Token döner."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Giriş başarılı",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Geçersiz istek",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Geçersiz kimlik bilgileri",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Access Token'ı yenile",
        description = "Refresh Token ile yeni bir Access Token ve yeni Refresh Token üretir (rotation)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Yenileme başarılı",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Geçersiz / süresi dolmuş / iptal Refresh Token",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Çıkış yap",
        description = "İlgili Refresh Token'ı iptal eder. Kimlik doğrulaması gerektirir.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Çıkış başarılı"),
        @ApiResponse(responseCode = "401", description = "Kimlik doğrulaması gerekli",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = resolveCurrentUser(userDetails);
        authService.logout(request.getRefreshToken(), currentUser);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Oturumdaki kullanıcı bilgileri",
        description = "Giriş yapmış kullanıcının bilgilerini döner. Şifre hash'i gibi hassas alanlar içermez.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Kullanıcı bilgileri",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Kimlik doğrulaması gerekli",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = resolveCurrentUser(userDetails);
        return ResponseEntity.ok(authService.getCurrentUser(currentUser));
    }

    private User resolveCurrentUser(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new AuthenticationException("Oturum bulunamadı.");
        }
        return userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new AuthenticationException("Kullanıcı bulunamadı."));
    }
}
