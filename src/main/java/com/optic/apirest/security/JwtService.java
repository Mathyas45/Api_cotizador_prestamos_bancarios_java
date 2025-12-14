package com.optic.apirest.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio JWT - Maneja la creación y validación de tokens JWT
 * 
 * JWT (JSON Web Token) es un token que contiene información del usuario
 * codificada y firmada digitalmente
 * 
 * ESTRUCTURA JWT: header.payload.signature
 * - header: tipo de token y algoritmo
 * - payload: datos del usuario (username, roles, etc.)
 * - signature: firma digital para verificar autenticidad
 * 
 * FLUJO:
 * 1. Usuario hace login → Backend genera JWT
 * 2. Frontend guarda JWT
 * 3. Frontend envía JWT en cada request (header Authorization)
 * 4. Backend valida JWT y permite acceso
 */
@Service
public class JwtService {

    /**
     * Clave secreta para firmar los tokens
     * 
     * IMPORTANTE: En producción, esta clave debe ser:
     * - Larga y compleja (mínimo 256 bits)
     * - Guardada en variable de entorno, NO en código
     * - Única por aplicación
     * 
     * Esta es una clave de ejemplo generada con algoritmo HS256
     */
    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String SECRET_KEY;

    /**
     * Tiempo de expiración del token en milisegundos
     * 
     * 86400000 ms = 24 horas
     * 
     * Puedes configurarlo en application.yml
     */
    @Value("${jwt.expiration:86400000}")
    private long JWT_EXPIRATION;

    /**
     * Extrae el username del token JWT
     * 
     * @param token JWT token
     * @return username del usuario
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae un claim específico del token
     * 
     * @param token JWT token
     * @param claimsResolver función para extraer el claim deseado
     * @return valor del claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Genera un token JWT para un usuario
     * 
     * @param userDetails información del usuario
     * @return JWT token
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Genera un token JWT con claims adicionales
     * 
     * @param extraClaims información adicional a incluir en el token
     * @param userDetails información del usuario
     * @return JWT token
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valida si un token es válido para un usuario
     * 
     * Verifica:
     * 1. El username del token coincide con el del usuario
     * 2. El token no ha expirado
     * 
     * @param token JWT token
     * @param userDetails información del usuario
     * @return true si el token es válido
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Verifica si un token ha expirado
     * 
     * @param token JWT token
     * @return true si el token expiró
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrae la fecha de expiración del token
     * 
     * @param token JWT token
     * @return fecha de expiración
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae todos los claims (datos) del token
     * 
     * @param token JWT token
     * @return todos los claims del token
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Obtiene la clave de firma para JWT
     * 
     * Decodifica la SECRET_KEY de Base64 a bytes
     * y crea una clave HMAC para firmar tokens
     * 
     * @return clave de firma
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
