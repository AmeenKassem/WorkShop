package workshop.demo.InfrastructureLayer;

import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.springframework.stereotype.Repository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import workshop.demo.DomainLayer.Authentication.AuthoResponse;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.config.JwtConfig;

@Repository
public class AuthenticationRepo implements IAuthRepo {

     private final SecretKey SECRET;
    private final long EXPIRATION_MS;

    public AuthenticationRepo(JwtConfig jwtConfig) {
        this.SECRET = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
        this.EXPIRATION_MS = jwtConfig.getExpiration();
    }
    private String generateToken(String tokenValue) {
        return Jwts.builder()
                .setSubject(tokenValue)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(SECRET)
                .compact();
    }

    private String extractTokenValue(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    private boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getUserName(String token) throws UIException {
        if (!validateToken(token)) {
            throw new UIException("Invalid or expired token!", ErrorCodes.INVALID_TOKEN);
        }
        return new AuthoResponse(extractTokenValue(token)).userName;
    }

    @Override
    public int getUserId(String token) throws UIException {
        if (!validateToken(token)) {
            throw new UIException("Invalid or expired token!", ErrorCodes.INVALID_TOKEN);
        }
        return new AuthoResponse(extractTokenValue(token)).id;
    }

    @Override
    public boolean isRegistered(String token) throws UIException {
        if (!validateToken(token)) {
            throw new UIException("Invalid or expired token!", ErrorCodes.INVALID_TOKEN);
        }
        return new AuthoResponse(extractTokenValue(token)).userName != null;
    }

    @Override
    public String generateGuestToken(int id) {
        AuthoResponse req = new AuthoResponse(null, id);
        return generateToken(req.toJson());
    }

    @Override
    public String generateUserToken(int id, String username) {
        AuthoResponse req = new AuthoResponse(username, id);
        return generateToken(req.toJson());
    }

    @Override
    public boolean validToken(String token) {
        return validateToken(token);
    }

    @Override
    public void checkAuth_ThrowTimeOutException(String token, Logger logger) throws UIException {
        if (!validToken(token)) {
            logger.error("Invalid token ");
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
    }
}
