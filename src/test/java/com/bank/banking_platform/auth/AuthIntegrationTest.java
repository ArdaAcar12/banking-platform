package com.bank.banking_platform.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bank.banking_platform.auth.dto.RegisterRequest;
import com.bank.banking_platform.auth.entity.Role;
import com.bank.banking_platform.auth.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Kimlik doğrulama entegrasyon testleri.
 * Gerçek PostgreSQL (Testcontainers) üzerinde Spring konteksi tam olarak yüklenerek
 * uçtan uca davranış doğrulanır.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine"))
        .withDatabaseName("banking_platform")
        .withUsername("banking")
        .withPassword("changeme");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    private String asJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    @Nested
    class Registration {

        @Test
        void shouldRegisterNewCustomerAndAssignDefaultRole() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                .email("User@Example.COM")
                .username("newcustomer")
                .password("Password123")
                .firstName("New")
                .lastName("Customer")
                .build();

            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.username").value("newcustomer"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.enabled").value(true));

            assertThat(userRepository.findByEmail("user@example.com"))
                .isPresent()
                .get()
                .satisfies(u -> {
                    assertThat(u.getRole()).isEqualTo(Role.CUSTOMER);
                    assertThat(u.getPasswordHash()).isNotNull();
                    assertThat(u.getPasswordHash()).isNotEqualTo("Password123");
                });
        }

        @Test
        void shouldRejectInvalidEmail() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                .email("not-an-email")
                .username("newcustomer")
                .password("Password123")
                .firstName("New")
                .lastName("Customer")
                .build();

            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldRejectEmptyPassword() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                .email("new@example.com")
                .username("newcustomer")
                .password("")
                .firstName("New")
                .lastName("Customer")
                .build();

            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldRejectWeakPassword() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                .email("new@example.com")
                .username("newcustomer")
                .password("1234567")
                .firstName("New")
                .lastName("Customer")
                .build();

            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldRejectDuplicateEmail() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                .email("dup@example.com")
                .username("userone")
                .password("Password123")
                .firstName("Dup")
                .lastName("User")
                .build();
            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(request)))
                .andExpect(status().isCreated());

            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(request)))
                .andExpect(status().isConflict());
        }

        @Test
        void shouldRejectDuplicateUsername() throws Exception {
            RegisterRequest first = RegisterRequest.builder()
                .email("first@example.com")
                .username("dupuser")
                .password("Password123")
                .firstName("F")
                .lastName("U")
                .build();
            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(first)))
                .andExpect(status().isCreated());

            RegisterRequest second = RegisterRequest.builder()
                .email("second@example.com")
                .username("dupuser")
                .password("Password123")
                .firstName("S")
                .lastName("U")
                .build();
            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(second)))
                .andExpect(status().isConflict());
        }
    }

    @Nested
    class Login {

        private final RegisterRequest register = RegisterRequest.builder()
            .email("login@example.com")
            .username("loginuser")
            .password("Password123")
            .firstName("Login")
            .lastName("User")
            .build();

        @BeforeEach
        void registerUser() throws Exception {
            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(register)))
                .andExpect(status().isCreated());
        }

        @Test
        void shouldLoginSuccessfullyAndIssueTokens() throws Exception {
            var login = com.bank.banking_platform.auth.dto.LoginRequest.builder()
                .identifier("login@example.com").password("Password123").build();

            String response = mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("loginuser"))
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"))
                .andReturn().getResponse().getContentAsString();

            com.bank.banking_platform.auth.dto.AuthResponse authResponse =
                objectMapper.readValue(response, com.bank.banking_platform.auth.dto.AuthResponse.class);
            assertThat(authResponse.getAccessToken()).isNotBlank();
            assertThat(authResponse.getRefreshToken()).isNotBlank();
        }

        @Test
        void shouldRejectWrongPassword() throws Exception {
            var login = com.bank.banking_platform.auth.dto.LoginRequest.builder()
                .identifier("login@example.com").password("WrongPass123").build();

            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(login)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldRejectUnknownUserWithGenericMessage() throws Exception {
            var login = com.bank.banking_platform.auth.dto.LoginRequest.builder()
                .identifier("nonexistent@example.com").password("Password123").build();

            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(login)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturnGenericMessageRegardlessOfUserExistence() throws Exception {
            // Var olan kullanıcıyla yanlış şifre
            var existingWrong = com.bank.banking_platform.auth.dto.LoginRequest.builder()
                .identifier("login@example.com").password("WrongPass123").build();
            String existingResponse = mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(existingWrong)))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

            // Yok olan kullanıcıyla yanlış şifre
            var unknown = com.bank.banking_platform.auth.dto.LoginRequest.builder()
                .identifier("nonexistent@example.com").password("Password123").build();
            String unknownResponse = mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(unknown)))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

            // Timestamp hariç mesaj alanlarının aynı olması yeterlidir (hata mesajı tutarlı olmalı)
            var existingNode = objectMapper.readTree(existingResponse);
            var unknownNode = objectMapper.readTree(unknownResponse);
            assertThat(existingNode.get("message").asText())
                .isEqualTo(unknownNode.get("message").asText());
            assertThat(existingNode.get("status").asInt()).isEqualTo(401);
            assertThat(unknownNode.get("status").asInt()).isEqualTo(401);
        }
    }

    @Nested
    class TokenValidation {

        @Test
        void shouldAcceptValidJwtOnProtectedEndpoint() throws Exception {
            // Giriş yapıp token al
            var login = com.bank.banking_platform.auth.dto.LoginRequest.builder()
                .identifier("tok@example.com").password("Password123").build();
            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(RegisterRequest.builder()
                        .email("tok@example.com").username("tokuser").password("Password123")
                        .firstName("T").lastName("U").build())))
                .andExpect(status().isCreated());

            String token = mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

            com.bank.banking_platform.auth.dto.AuthResponse authResponse =
                objectMapper.readValue(token, com.bank.banking_platform.auth.dto.AuthResponse.class);

            // Korumalı endpoint'e token ile eriş
            mockMvc.perform(get("/api/v1/auth/me")
                    .header("Authorization", "Bearer " + authResponse.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("tokuser"));
        }

        @Test
        void shouldRejectMissingToken() throws Exception {
            mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldRejectInvalidToken() throws Exception {
            mockMvc.perform(get("/api/v1/auth/me")
                    .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class Authorization {

        @Test
        @WithMockUser(username = "customer", roles = "CUSTOMER")
        void customerShouldNotAccessAdminEndpoint() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "support", roles = "SUPPORT")
        void supportShouldNotAccessAdminEndpoint() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "admin", roles = "ADMIN")
        void adminShouldAccessAdminEndpoint() throws Exception {
            // Veritabanında bir kullanıcı var olsun
            userRepository.save(com.bank.banking_platform.auth.entity.User.builder()
                .email("a@example.com").username("adminuser")
                .passwordHash("hash").firstName("A").lastName("Admin")
                .role(Role.ADMIN).enabled(true).accountNonLocked(true).build());

            mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    class RefreshAndLogout {

        @Test
        void shouldRefreshAndRotateToken() throws Exception {
            var login = com.bank.banking_platform.auth.dto.LoginRequest.builder()
                .identifier("ref@example.com").password("Password123").build();
            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(RegisterRequest.builder()
                        .email("ref@example.com").username("refuser").password("Password123")
                        .firstName("R").lastName("U").build())))
                .andExpect(status().isCreated());

            String loginResp = mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
            com.bank.banking_platform.auth.dto.AuthResponse authResponse =
                objectMapper.readValue(loginResp, com.bank.banking_platform.auth.dto.AuthResponse.class);
            String firstRefresh = authResponse.getRefreshToken();
            String firstAccess = authResponse.getAccessToken();

            // Yenileme
            var refresh = com.bank.banking_platform.auth.dto.RefreshRequest.builder()
                .refreshToken(firstRefresh).build();
            String refreshResp = mockMvc.perform(post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(refresh)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn().getResponse().getContentAsString();
            com.bank.banking_platform.auth.dto.AuthResponse rotated =
                objectMapper.readValue(refreshResp, com.bank.banking_platform.auth.dto.AuthResponse.class);

            // Eski access token artık geçerli olabilir (süresi bitene kadar), ancak eski refresh token artık iptal.
            mockMvc.perform(post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(com.bank.banking_platform.auth.dto.RefreshRequest.builder()
                    .refreshToken(firstRefresh).build())))
                .andExpect(status().isUnauthorized());

            assertThat(rotated.getRefreshToken()).isNotEqualTo(firstRefresh);
        }

        @Test
        void shouldInvalidateRefreshTokenOnLogout() throws Exception {
            var login = com.bank.banking_platform.auth.dto.LoginRequest.builder()
                .identifier("lout@example.com").password("Password123").build();
            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(RegisterRequest.builder()
                        .email("lout@example.com").username("loutuser").password("Password123")
                        .firstName("L").lastName("U").build())))
                .andExpect(status().isCreated());

            String loginResp = mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
            com.bank.banking_platform.auth.dto.AuthResponse authResponse =
                objectMapper.readValue(loginResp, com.bank.banking_platform.auth.dto.AuthResponse.class);

            // Çıkış
            var logout = com.bank.banking_platform.auth.dto.LogoutRequest.builder()
                .refreshToken(authResponse.getRefreshToken()).build();
            mockMvc.perform(post("/api/v1/auth/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(logout))
                    .header("Authorization", "Bearer " + authResponse.getAccessToken()))
                .andExpect(status().isOk());

            // Eski refresh token artık kullanılamaz
            mockMvc.perform(post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(com.bank.banking_platform.auth.dto.RefreshRequest.builder()
                        .refreshToken(authResponse.getRefreshToken()).build())))
                .andExpect(status().isUnauthorized());
        }
    }
}
