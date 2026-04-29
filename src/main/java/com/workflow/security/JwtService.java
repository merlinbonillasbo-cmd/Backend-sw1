package com.workflow.security;

import com.workflow.modules.users.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserId(String token) {
        return extractClaim(token, c -> c.get("userId", String.class));
    }

    public String extractRol(String token) {
        return extractClaim(token, c -> c.get("rol", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("rol", user.getRol() != null ? user.getRol().name() : null);
        claims.put("nombreCompleto", user.getNombreCompleto());
        if (user.getIdDepartamento() != null) {
            claims.put("departamentoId", user.getIdDepartamento());
        }
        return generateToken(claims, user);
    }

    /**
     * Genera token con claims enriquecidos incluyendo código y nombre del departamento.
     */
    public String generateTokenWithDept(User user, String departamentoCodigo, String departamentoNombre) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("rol", user.getRol() != null ? user.getRol().name() : null);
        claims.put("nombreCompleto", user.getNombreCompleto());
        if (user.getIdDepartamento() != null) {
            claims.put("departamentoId", user.getIdDepartamento());
        }
        if (departamentoCodigo != null) {
            claims.put("departamentoCodigo", departamentoCodigo);
        }
        if (departamentoNombre != null) {
            claims.put("departamentoNombre", departamentoNombre);
        }
        return generateToken(claims, user);
    }

    /** @deprecated Usar {@link #generateTokenWithDept(User, String, String)} */
    public String generateTokenWithDept(User user, String departamentoCodigo) {
        return generateTokenWithDept(user, departamentoCodigo, null);
    }

    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

}
