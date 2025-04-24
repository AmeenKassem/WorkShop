package workshop.demo.InfrastructureLayer;

import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import workshop.demo.DomainLayer.Authentication.*;
import workshop.demo.DomainLayer.Exceptions.TokenNotFoundException;

public class AuthenticationRepo  implements IAuthRepo {
    

    private  SecretKey SECRET =Keys.secretKeyFor(SignatureAlgorithm.HS256); // Use env/config in real apps
    private final long EXPIRATION_MS = 1000 * 60 * 60; // 1 hour

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
        return new AuthoResponse(extractTokenValue(token)  ).userName!=null;
    }

    @Override
    public String generateGuestToken(int id) {
        AuthoResponse req = new AuthoResponse(null,id);
        System.out.println(req.toJson());
        return generateToken(req.toJson());
    }

    @Override
    public String generateUserToken(int id, String username) {
        AuthoResponse req = new AuthoResponse(username,id);
        return generateToken(req.toJson());
    }

    public static void main(String[] args){
        AuthenticationRepo a = new AuthenticationRepo();
        String  token = a.generateGuestToken(5);
        System.out.println(a.getUserId(token));
        System.out.println(a.getUserName(token));
        System.out.println(a.isRegistered(token));
    }
    
}
