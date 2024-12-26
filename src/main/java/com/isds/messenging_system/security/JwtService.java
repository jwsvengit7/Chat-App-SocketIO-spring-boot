package com.isds.messenging_system.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public class JwtService {


    public String extractUsername(String jwtToken)  {
        return extractClaims(jwtToken, Claims::getSubject);
    }



    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }



    public boolean isTokenValid(String token) {
        return  !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        String secretKey = "ENC(nyVagPco1hoslxDwwakFeUMjBElTUUXDeRCW4aMQyzh5XfP3vHT/TkCVK7RYQM1LQaawoF6qo9PG8vmGlcbcyrWO9fKCeqwW5R6+kvfsfpqG+OgUenGipOw+x39ljczFdgoxxW5VNUVBsFDeMibq9g==)";
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
