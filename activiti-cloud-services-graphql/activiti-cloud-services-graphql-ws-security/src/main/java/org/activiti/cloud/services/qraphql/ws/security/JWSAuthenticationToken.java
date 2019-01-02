package org.activiti.cloud.services.qraphql.ws.security;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class JWSAuthenticationToken extends AbstractAuthenticationToken implements Authentication {

    private static final long serialVersionUID = 1L;

    private String token;
    private User principal;

    public JWSAuthenticationToken(String token) {
        this(token, null, null);
        
        setAuthenticated(false);
    }

    public JWSAuthenticationToken(String token, User principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = token;
        this.principal = principal;
        
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

}