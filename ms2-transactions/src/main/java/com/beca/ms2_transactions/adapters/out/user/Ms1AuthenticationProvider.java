package com.beca.ms2_transactions.adapters.out.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.beca.ms2_transactions.application.port.out.UserInfo;

@Component
public class Ms1AuthenticationProvider implements AuthenticationProvider {

    private final RestClient restClient;

    public Ms1AuthenticationProvider(
            RestClient.Builder builder,
            @Value("${ms1.users.base-url:http://localhost:8080}") String baseUrl) {

        this.restClient = builder.clone()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        String email = authentication.getName();
        String senha = authentication.getCredentials().toString();

        try {
            UserInfo user = restClient.post()
                    .uri("/users/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AuthenticationRequest(email, senha))
                    .retrieve()
                    .body(UserInfo.class);

            if (user == null || user.id() == null) {
                throw new AuthenticationServiceException(
                        "O MS1 retornou uma resposta de autenticação inválida.");
            }

            return UsernamePasswordAuthenticationToken.authenticated(
                    user,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );

        } catch (HttpClientErrorException.Unauthorized exception) {

            throw new BadCredentialsException(
                    "E-mail ou senha inválidos.");

        } catch (RestClientException exception) {

            throw new AuthenticationServiceException(
                    "Não foi possível comunicar com o MS1.",
                    exception
            );
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class
                .isAssignableFrom(authentication);
    }

    private record AuthenticationRequest(String email, String senha) {
    }
}