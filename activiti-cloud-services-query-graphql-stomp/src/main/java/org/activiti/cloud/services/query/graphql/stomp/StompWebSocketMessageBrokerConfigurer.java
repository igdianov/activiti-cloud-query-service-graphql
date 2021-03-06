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
package org.activiti.cloud.services.query.graphql.stomp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
public class StompWebSocketMessageBrokerConfigurer implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.rabbitmq.host:rabbitmq}")
    private String relayHost;

    @Value("${spring.rabbitmq.username:guest}")
    private String login;

    @Value("${spring.rabbitmq.password:guest}")
    private String passcode;


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/stomp")
            .setHandshakeHandler(new DefaultHandshakeHandler())
        	.addInterceptors(new HttpSessionHandshakeInterceptor())
        	.setAllowedOrigins("*")
        	.withSockJS()
        	;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry
        	.enableStompBrokerRelay("/topic")
            .setRelayHost(relayHost)
            .setClientLogin(login)
            .setClientPasscode(passcode)
        	;
    }

    @Bean
    public MessageConverter webSocketMessageConverter() {
        return new MappingJackson2MessageConverter();
    }

}
