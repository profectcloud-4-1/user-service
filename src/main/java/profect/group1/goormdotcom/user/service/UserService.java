package profect.group1.goormdotcom.user.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import profect.group1.goormdotcom.apiPayload.ApiResponse;
import profect.group1.goormdotcom.common.security.JwtTokenProvider;
import profect.group1.goormdotcom.user.domain.User;
import profect.group1.goormdotcom.user.domain.enums.UserRole;
import profect.group1.goormdotcom.user.domain.mapper.UserMapper;
import profect.group1.goormdotcom.user.infrastructure.client.CartClient;
import profect.group1.goormdotcom.user.repository.UserRepository;
import profect.group1.goormdotcom.user.repository.entity.UserEntity;
import profect.group1.goormdotcom.user.service.dto.CreateUserDto;
import profect.group1.goormdotcom.user.service.dto.LoginTokensDto;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository repo;
    private final PasswordService passwordService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CartClient cartClient;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String ACCESS_PREFIX = "access:";
    private static final String REFRESH_PREFIX = "refresh:";

    public User findById(UUID id) {
        UserEntity entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return this.userMapper.toDomain(entity);
    }

    public boolean isEmailExists(String email) {
        return repo.findByEmail(email).isPresent();
    }

    @Transactional
    public User register(CreateUserDto body) {
        if (!passwordService.validate(body.getPassword())) throw new IllegalArgumentException("Invalid password");
        String encryptedPassword = passwordService.encrypt(body.getPassword());

        if (isEmailExists(body.getEmail())) throw new IllegalArgumentException("Email already exists");

        String role = UserRole.CUSTOMER.getCode();

        UserEntity entity = UserEntity.builder()
            .name(body.getName())
            .role(role)
            .email(body.getEmail())
            .password(encryptedPassword)
            .build();
        entity = repo.save(entity);
        UUID userId = entity.getId();

        // Cart client 수정
        ApiResponse<UUID> result = cartClient.create(userId, role);
        UUID cartId =result.getResult();
        if (cartId == null)
            throw new IllegalStateException("Failed to create cart");
        

        return this.userMapper.toDomain(entity);
    }

    @Transactional
    public LoginTokensDto login(String email, String password) {
        UserEntity entity = repo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        String encoded = entity.getPassword();
        if (!passwordService.isMatch(password, encoded)) throw new IllegalArgumentException("Invalid credentials");

        entity.setLastLoginAt(LocalDateTime.now());
        repo.save(entity);
        String role = entity.getRole();
        UUID userId = entity.getId();

        String accessJti = UUID.randomUUID().toString();
        String refreshJti = UUID.randomUUID().toString();

        String accessToken = jwtTokenProvider.generateAccessToken(userId, role, accessJti);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId, role, refreshJti);

        long accessTtl = jwtTokenProvider.getAccessTokenExpirationSeconds();
        long refreshTtl = jwtTokenProvider.getRefreshTokenExpirationSeconds();

        String value = userId + ":" + role;

        // Redis 장애 시 저장X, Token만 반환
        try {
            redisTemplate.opsForValue().set(
                    ACCESS_PREFIX + accessJti,
                    value,
                    accessTtl,
                    TimeUnit.SECONDS
            );
            redisTemplate.opsForValue().set(
                    REFRESH_PREFIX + refreshJti,
                    value,
                    refreshTtl,
                    TimeUnit.SECONDS
            );
        } catch (Exception e) {
            log.error("Failed to save tokens to Redis on login. userId={}, email={}", userId, email, e);
        }

        return new LoginTokensDto(accessToken, refreshToken);
    }

    @Transactional
    public LoginTokensDto refresh(String refreshToken) {
        // 1) JWT 구조 검증 + 정보 추출
        UUID userId = jwtTokenProvider.getUserId(refreshToken);
        String role = jwtTokenProvider.getRole(refreshToken);
        String refreshJti = jwtTokenProvider.getJti(refreshToken);

        String key = REFRESH_PREFIX + refreshJti;

        // 2) Redis에서 refresh 유효성 확인
        String value = null;
        boolean redisError = false;
        try {
            value = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            redisError = true;
            log.error("Failed to read refresh token from Redis. Proceeding without Redis validation. key={}, userId={}", key, userId, e);
        }

        if (!redisError && value == null) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // 3) 토큰 회전: 기존 refresh 삭제 (Redis 정상일 때만)
        if (!redisError) {
            try {
                redisTemplate.delete(key);
            } catch (Exception e) {
                log.error("Failed to delete old refresh token from Redis. key={}, userId={}", key, userId, e);
            }
        }

        // 4) 새 jti 생성 + 토큰 재발급
        String newAccessJti = UUID.randomUUID().toString();
        String newRefreshJti = UUID.randomUUID().toString();

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, role, newAccessJti);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, role, newRefreshJti);

        long accessTtl = jwtTokenProvider.getAccessTokenExpirationSeconds();
        long refreshTtl = jwtTokenProvider.getRefreshTokenExpirationSeconds();

        String newValue = userId + ":" + role;

        // 5) Redis에 새 토큰 저장 (Redis 정상일 때만)
        if (!redisError) {
            try {
                redisTemplate.opsForValue().set(
                        ACCESS_PREFIX + newAccessJti,
                        newValue,
                        accessTtl,
                        TimeUnit.SECONDS
                );
                redisTemplate.opsForValue().set(
                        REFRESH_PREFIX + newRefreshJti,
                        newValue,
                        refreshTtl,
                        TimeUnit.SECONDS
                );
            } catch (Exception e) {
                log.error("Failed to save rotated tokens to Redis. userId={}", userId, e);
            }
        } else {
            log.warn("Refresh token rotated without Redis persistence. userId={}", userId);
        }

        return new LoginTokensDto(newAccessToken, newRefreshToken);
    }

    public void edit(UUID userId, String name, String email) {
        UserEntity entity = repo.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (email != null && !email.equals(entity.getEmail())) {
            if (isEmailExists(email)) throw new IllegalArgumentException("Email already exists");
        }
        entity.setName(name);
        entity.setEmail(email);
        repo.save(entity);
    }

    public void deleteById(UUID userId) {
        UserEntity entity = repo.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        repo.delete(entity);
    }
}