/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.cloud.services.qraphql.ws.security;

import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Order(Ordered.HIGHEST_PRECEDENCE + 98)
public class JWSTokenChannelSecurityContextConfigurer implements WebSocketMessageBrokerConfigurer {

    private static final String X_AUTHORIZATION = "X-Authorization";
    private static final String BEARER = "Bearer";

    @Autowired
    private KeycloakWebSocketAuthenticationManager authenticationManager;
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                SimpMessageHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
                                                                                       SimpMessageHeaderAccessor.class);
                if (accessor != null) {
                    Optional.ofNullable(accessor.getHeader(X_AUTHORIZATION))
                            .map(String.class::cast)
                            .map(header -> header.replace(BEARER, "").trim())
                            .ifPresent(bearer -> {
                                Authentication jwsAuthToken = new JWSAuthenticationToken(bearer);

                                Principal principal = authenticationManager.authenticate(jwsAuthToken);
                                
                                accessor.setUser(principal);
                            });
                }
                return message;
            }
        });
    }
}