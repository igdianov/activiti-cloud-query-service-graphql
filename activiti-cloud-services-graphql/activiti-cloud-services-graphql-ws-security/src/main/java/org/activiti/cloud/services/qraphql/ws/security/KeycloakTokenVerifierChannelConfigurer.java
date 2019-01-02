package org.activiti.cloud.services.qraphql.ws.security;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class KeycloakTokenVerifierChannelConfigurer implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakTokenVerifierChannelConfigurer.class);
    private static final String GRAPHQL_MESSAGE_TYPE = "graphQLMessageType";

    private final KeycloakTokenVerifier tokenVerifier;
    private List<String> headerValues = Arrays.asList("connection_init", "start");
    private String headerName = GRAPHQL_MESSAGE_TYPE;

    public KeycloakTokenVerifierChannelConfigurer(KeycloakTokenVerifier tokenVerifier) {
        this.tokenVerifier = tokenVerifier;
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                SimpMessageHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
                                                                                       SimpMessageHeaderAccessor.class);
                if (accessor != null) {
                    if(headerValues.contains(accessor.getHeader(headerName))) {
                        Optional.ofNullable(accessor.getUser())
                                .filter(KeycloakAuthenticationToken.class::isInstance)
                                .map(KeycloakAuthenticationToken.class::cast)
                                .map(KeycloakAuthenticationToken::getCredentials)
                                .map(KeycloakSecurityContext.class::cast)
                                .ifPresent(keycloakSecurityContext -> {
                                    try {
                                        logger.info("Verify Token for {}", accessor.getHeader(GRAPHQL_MESSAGE_TYPE));
                                        tokenVerifier.verifyToken(keycloakSecurityContext.getTokenString());
                                        
                                    } catch (Exception e) {
                                        throw new BadCredentialsException("Invalid token", e);
                                    }
                                });
                    }
                }
                return message;
            }
        });
    }

    
    public void setHeaderValues(List<String> headerValues) {
        this.headerValues = headerValues;
    }

    
    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }
}