package profect.group1.goormdotcom.common.security;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import profect.group1.goormdotcom.user.domain.enums.UserRole;

// JWT 토큰 생성/파싱: secret.yml에 만료기간 미설정 시 만료되지않음
@Component
public class JwtTokenProvider {

    private final PrivateKey privateKey;
    private final long accessTokenValidityMs;

    public JwtTokenProvider(
            @Value("${spring.jwt.private-key}") String privateKeyPem,
            @Value("${spring.jwt.accessTokenValidityMs:-1}") long accessTokenValidityMs
    ) {
        this.privateKey = parsePrivateKey(privateKeyPem);
        this.accessTokenValidityMs = accessTokenValidityMs;
    }

    private PrivateKey parsePrivateKey(String base64) {
        try {
            byte[] der = Base64.getDecoder().decode(base64.trim());
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (Exception e) {
            throw new IllegalStateException("Invalid RSA private key", e);
        }
    }

    public String generateAccessToken(UUID userId, String roleCode) {
        Date now = new Date();

        UserRole role = UserRole.fromCode(roleCode);
        System.out.println("role::: " + role.name());

        var builder =Jwts.builder()
                .header().add("kid", "key-2025-11-06-01").and()
                .subject(userId.toString())
                .claim("role", role.name())
                .issuedAt(now)
                .issuer("https://public.goorm.store")
                .audience().add("goormdotcom-aud").and()
                .signWith(privateKey, Jwts.SIG.RS256);

        if (accessTokenValidityMs > 0) {
            Date exp = new Date(now.getTime() + accessTokenValidityMs);
            builder.expiration(exp);
        }

        return builder.compact();

    }
}