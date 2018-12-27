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
package org.activiti.cloud.services.graphql.ws.schema.config;

import graphql.GraphQL;
import org.activiti.cloud.graphql.config.GraphQLSchemaConfigurer;
import org.activiti.cloud.graphql.config.GraphQLShemaRegistration;
import org.activiti.cloud.services.graphql.ws.schema.GraphQLSubscriptionSchemaBuilder;
import org.activiti.cloud.services.graphql.ws.schema.GraphQLSubscriptionSchemaProperties;
import org.activiti.cloud.services.graphql.ws.schema.datafetcher.GraphQLStompRelayDataFetcherDestinationResolver;
import org.activiti.cloud.services.graphql.ws.schema.datafetcher.StompRelayDataFetcher;
import org.activiti.cloud.services.graphql.ws.schema.datafetcher.StompRelayDestinationResolver;
import org.activiti.cloud.services.graphql.ws.schema.datafetcher.StompRelayFluxPublisherFactory;
import org.activiti.cloud.services.graphql.ws.schema.datafetcher.StompRelayPublisherFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.ReactorNettyTcpStompClient;

@Configuration
@ConditionalOnClass({GraphQL.class, ReactorNettyTcpStompClient.class})
@ConditionalOnProperty(name="spring.activiti.cloud.services.query.graphql.ws.enabled", matchIfMissing = true)
public class GraphQLSubscriptionsSchemaAutoConfiguration {

    @Configuration
    static class DefaultGraphQLSubscriptionsSchemaConfiguration {

        @Autowired
        private GraphQLSubscriptionSchemaProperties subscriptionProperties;

        @Bean
        @ConditionalOnMissingBean
        public ReactorNettyTcpStompClient stompClient() {
            ReactorNettyTcpStompClient stompClient = new ReactorNettyTcpStompClient(subscriptionProperties.getRelayHost(),
                                                                                    subscriptionProperties.getRelayPort());
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());

            return stompClient;
        }

        @Bean
        @ConditionalOnMissingBean
        public StompRelayPublisherFactory stompRelayPublisherFactory(ReactorNettyTcpStompClient stompClient,
                                                                     StompRelayDestinationResolver stompRelayDestinationResolver) {
            return new StompRelayFluxPublisherFactory(stompClient).login(subscriptionProperties.getClientLogin())
                                                              .passcode(subscriptionProperties.getClientPasscode())
                                                              .destinationResolver(stompRelayDestinationResolver);
        }        
        
        @Bean
        @ConditionalOnMissingBean
        public StompRelayDestinationResolver stompRelayDestinationResolver() {
            return new GraphQLStompRelayDataFetcherDestinationResolver(subscriptionProperties.getSubscriptionArgumentNames());
        }

        @Bean
        @ConditionalOnMissingBean
        public StompRelayDataFetcher stompRelayDataFetcher(StompRelayPublisherFactory stompRelayPublisherFactory) {
            return new StompRelayDataFetcher(stompRelayPublisherFactory);
        }

        
        @Bean
        @ConditionalOnMissingBean
        public GraphQLSubscriptionSchemaBuilder graphQLSubscriptionSchemaBuilder( StompRelayDataFetcher stompRelayDataFetcher) {
            GraphQLSubscriptionSchemaBuilder schemaBuilder = new GraphQLSubscriptionSchemaBuilder(subscriptionProperties.getGraphqls());
            
            schemaBuilder.withSubscription(subscriptionProperties.getSubscriptionFieldName(), stompRelayDataFetcher);
            
            return schemaBuilder;
        }
        
    }

    @Configuration
    static class DefaultGraphQLSubscriptionsConfigurer implements GraphQLSchemaConfigurer {

        @Autowired
        private GraphQLSubscriptionSchemaBuilder graphQLSubscriptionSchemaBuilder;

        @Override
        public void configure(GraphQLShemaRegistration registry) {
            registry.register(graphQLSubscriptionSchemaBuilder.getGraphQLSchema());
            
        }
    }


}
