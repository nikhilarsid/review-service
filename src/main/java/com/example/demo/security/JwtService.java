package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    public String extractUsername(String token) { return extractClaim(token, Claims::getSubject); }
    public Long extractUserId(String token) { return extractClaim(token, claims -> claims.get("userId", Long.class)); }
    public <T> T extractClaim(String token, Function<Claims, T> resolver) { return resolver.apply(extractAllClaims(token)); }
    private Claims extractAllClaims(String token) { return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody(); }
    private Key getSignInKey() { return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)); }
    public boolean isTokenValid(String token, String username) { return (extractUsername(token).equals(username) && !isTokenExpired(token)); }
    private boolean isTokenExpired(String token) { return extractClaim(token, Claims::getExpiration).before(new Date()); }
}