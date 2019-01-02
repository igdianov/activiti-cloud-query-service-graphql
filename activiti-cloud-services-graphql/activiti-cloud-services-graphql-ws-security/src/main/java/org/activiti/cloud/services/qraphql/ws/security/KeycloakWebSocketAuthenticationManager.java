package org.activiti.cloud.services.qraphql.ws.security;

import java.util.Collection;

import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.Attributes2GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAttributes2GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.User;

@Qualifier("websoket")
public class KeycloakWebSocketAuthenticationManager implements AuthenticationManager {

    private final KeycloakTokenVerifier tokenVerifier;
    private Attributes2GrantedAuthoritiesMapper authoritiesMapper = new SimpleAttributes2GrantedAuthoritiesMapper();

    public KeycloakWebSocketAuthenticationManager(KeycloakTokenVerifier tokenVerifier) {
        this.tokenVerifier = tokenVerifier;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JWSAuthenticationToken token = null;
        try {
            token = JWSAuthenticationToken.class.cast(authentication);

            String credentials = (String) token.getCredentials();

            AccessToken accessToken = tokenVerifier.verifyToken(credentials);

            Collection<? extends GrantedAuthority> authorities = authoritiesMapper.getGrantedAuthorities(accessToken.getRealmAccess()
                                                                                                                    .getRoles());
            User user = new User(accessToken.getPreferredUsername(), credentials, authorities);

            token = new JWSAuthenticationToken(credentials, user, authorities);
            token.setDetails(accessToken);

        } catch (VerificationException e) {
            throw new BadCredentialsException("Invalid token", e);
        }

        return token;
    }

    public void setAuthoritiesMapper(Attributes2GrantedAuthoritiesMapper authoritiesMapper) {
        this.authoritiesMapper = authoritiesMapper;
    }

}