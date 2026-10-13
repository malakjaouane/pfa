package ma.it6.portefeuille.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * Création et validation des jetons JWT.
 */
@Service
public class JwtService {

    private final String secret;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    private SecretKey cle() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generer(PrincipalUser utilisateur) {
        List<String> permissions = utilisateur.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        Date maintenant = new Date();
        Date expiration = new Date(maintenant.getTime() + expirationMs);

        return Jwts.builder()
                .subject(utilisateur.getUsername())
                .claim("nom", utilisateur.getUtilisateur().getNom())
                .claim("prenom", utilisateur.getUtilisateur().getPrenom())
                .claim("role", utilisateur.getUtilisateur().getRole() != null
                        ? utilisateur.getUtilisateur().getRole().getCode() : null)
                .claim("permissions", permissions)
                .issuedAt(maintenant)
                .expiration(expiration)
                .signWith(cle())
                .compact();
    }

    public String extraireEmail(String token) {
        return extraireClaim(token, Claims::getSubject);
    }

    public Date extraireExpiration(String token) {
        return extraireClaim(token, Claims::getExpiration);
    }

    @SuppressWarnings("unchecked")
    public List<String> extrairePermissions(String token) {
        return extraireClaim(token, claims -> claims.get("permissions", List.class));
    }

    public boolean estValide(String token, String email) {
        try {
            return extraireEmail(token).equals(email) && extraireExpiration(token).after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private <T> T extraireClaim(String token, Function<Claims, T> resolveur) {
        Claims claims = Jwts.parser()
                .verifyWith(cle())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolveur.apply(claims);
    }
}