package profect.group1.goormdotcom.common.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import profect.group1.goormdotcom.user.domain.enums.UserRole;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    // ==================== 설정 값 ====================

    /**
     * PKCS#8 형식의 RSA Private Key PEM
     * -----BEGIN PRIVATE KEY-----
     * ...
     * -----END PRIVATE KEY-----
     */
    @Value("${spring.jwt.private-key}")
    private String privateKeyPem;

    @Value("${spring.jwt.issuer}")
    private String issuer;

    @Value("${spring.jwt.audience}")
    private String audience;

    @Value("${spring.jwt.access-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${spring.jwt.refresh-expiration-ms}")
    private long refreshTokenExpirationMs;

    // ==================== 내부 필드 ====================

    private RSAPrivateKey privateKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== 초기화 ====================

    @PostConstruct
    public void init() {
        try {
            this.privateKey = loadPrivateKey(privateKeyPem);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA private key", e);
        }
    }

    private RSAPrivateKey loadPrivateKey(String pem) throws Exception {
        String clean = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(clean);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey key = keyFactory.generatePrivate(keySpec);
        return (RSAPrivateKey) key;
    }

    // ==================== 토큰 생성 ====================

    public String generateAccessToken(UUID userId, String roleCode, String jti) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);

        UserRole userRole = UserRole.fromCode(roleCode);
        if (userRole == null) {
            throw new IllegalArgumentException("Unknown role code: " + roleCode);
        }

        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", userRole.name())
                .claim("iss", issuer)      // issuer
                .claim("aud", audience)    // audience
                .id(jti)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(privateKey, Jwts.SIG.RS256)   // RSA private key로 서명
                .compact();
    }

    public String generateRefreshToken(UUID userId, String roleCode, String jti) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpirationMs);

        UserRole userRole = UserRole.fromCode(roleCode);
        if (userRole == null) {
            throw new IllegalArgumentException("Unknown role code: " + roleCode);
        }

        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", userRole.name())
                .claim("iss", issuer)
                .claim("aud", audience)
                .id(jti)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    // ==================== 토큰 파싱 ====================

    /** "Bearer " 프리픽스 제거 */
    private String stripBearer(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Token is null");
        }
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    /**
     * 서명 검증 없이 payload만 파싱하는 메서드
     * - RS256, HS256 등 어떤 alg라도 상관 없이 payload 부분만 Base64URL 디코딩
     */
    public Claims parseClaimsWithoutVerify(String token) {
        try {
            String compact = stripBearer(token);
            String[] parts = compact.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT format");
            }

            // payload (두 번째 파트) Base64URL 디코딩
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);

            // JSON -> Map -> Claims
            Map<String, Object> map = objectMapper.readValue(
                    payloadJson,
                    new TypeReference<Map<String, Object>>() {}
            );

            // JJWT의 Claims로 변환 (sub, jti, iss, aud 등도 그대로 들어감)
            return Jwts.claims(map);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse JWT", e);
        }
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMs / 1000;
    }

    public long getRefreshTokenExpirationSeconds() {
        return refreshTokenExpirationMs / 1000;
    }

    public UUID getUserId(String token) {
        Claims claims = parseClaimsWithoutVerify(token);
        return UUID.fromString(claims.getSubject()); // sub
    }

    public String getRoleName(String token) {
        Claims claims = parseClaimsWithoutVerify(token);
        return claims.get("role", String.class);
    }
    public UserRole getRoleEnum(String token) {
        String name = getRoleName(token);
        return UserRole.valueOf(name);
    }

    public String getRoleCode(String token) {
        return getRoleEnum(token).getCode();
    }

    public String getJti(String token) {
        Claims claims = parseClaimsWithoutVerify(token);
        return claims.getId(); // jti
    }
}