package com.educba.onlinebookstore.paymentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.List;

@Configuration
public class JwtAuthConverterConfig {

    @Bean
    public JwtAuthenticationConverter  jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(
                jwt -> {
                    String role = jwt.getClaimAsString("role");
                    if(role == null){
                        return List.of();
                    }
                    return List.of(new SimpleGrantedAuthority("ROLE_"+role));
                }
        );
        return converter;
    }

}
