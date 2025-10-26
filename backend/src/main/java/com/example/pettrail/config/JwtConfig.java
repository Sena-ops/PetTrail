package com.example.pettrail.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    
    private String secret = "mySecretKey"; // This should be overridden in application.properties
    private int expiration = 86400000; // 24 hours in milliseconds
    private String issuer = "pettrail";
    private String audience = "pettrail-users";
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public int getExpiration() {
        return expiration;
    }
    
    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }
    
    public String getIssuer() {
        return issuer;
    }
    
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    
    public String getAudience() {
        return audience;
    }
    
    public void setAudience(String audience) {
        this.audience = audience;
    }
}


