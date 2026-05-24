package com.dorandoran.backend.global.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "dorandoran.jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenValidityMs;
    private long refreshTokenValidityMs;
    private String issuer;
}
