package workshop.demo.InfrastructureLayer;

import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import workshop.demo.DomainLayer.Authentication.*;
import workshop.demo.DomainLayer.Exceptions.TokenNotFoundException;

public class AuthenticationRepo  implements IAuthRepo {
    

    private final String SECRET = "secret_key_123"; // Use env/config in real apps
    private final long EXPIRATION_MS = 1000 * 60 * 60; // 1 hour

    private String generateToken(String tokenValue) {
        return Jwts.builder()
                .setSubject(tokenValue)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    private String extractTokenValue(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(SECRET.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    private boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(SECRET.getBytes())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    

    @Override
    public String getUserName(String token) {
        if(!validateToken(token))
            throw new TokenNotFoundException();
        return new AuthoResponse(extractTokenValue(token)  ).userName;
    }

    @Override
    public int getUserId(String token) {
        if(!validateToken(token))
            throw new TokenNotFoundException();
        return new AuthoResponse(extractTokenValue(token)  ).id;
    }

    @Override
    public boolean isRegistered(String token) {
        if(!validateToken(token))
            throw new TokenNotFoundException();
        return new AuthoResponse(extractTokenValue(token)  ).isRegisterd();
    }

    @Override
    public String generateGuestToken(int id) {
        AuthoResponse req = new AuthoResponse(null,id);
        return generateToken(req.toJson());
    }

    @Override
    public String generateUserToken(int id, String username) {
        AuthoResponse req = new AuthoResponse(username,id);
        return generateToken(req.toJson());
    }
    
}
