package org.yomirein.sochatserver.utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

public class JwtService {

    // Actually really simple JWT Service
    // It's so simple I just copypasted from previous try then I made sochat in Spring

    private static final String SECRET;
    private static final Key SIGNING_KEY;

    // Different security key so every reload every session become invalid
    // I guess it needed because users don't need to trust server owner
    static {
        byte[] keyBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(keyBytes);
        SECRET = Base64.getEncoder().encodeToString(keyBytes);
        SIGNING_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET));
    }

    // Generate JWT Token active for 1 day
    public static String generateToken(String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + 1000L * 60 * 60 * 24);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // Parse token
    private static Claims parseClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Extract user username from token so we can understand who is token owner
    public static String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public static <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = parseClaims(token);
        return resolver.apply(claims);
    }

    // Check if token even valid
    public static boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            Date expiration = claims.getExpiration();
            return expiration == null || expiration.after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}