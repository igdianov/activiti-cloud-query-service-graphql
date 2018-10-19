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
package org.activiti.cloud.services.query.graphql.notifications.config;

import java.util.Arrays;

import org.activiti.cloud.services.query.graphql.notifications.NotificationsGateway;
import org.activiti.cloud.services.query.graphql.notifications.RoutingKeyResolver;
import org.activiti.cloud.services.query.graphql.notifications.consumer.NotificationsConsumerChannelHandler;
import org.activiti.cloud.services.query.graphql.notifications.consumer.ProcessEngineNotificationTransformer;
import org.activiti.cloud.services.query.graphql.notifications.graphql.GraphQLProcessEngineNotificationTransformer;
import org.activiti.cloud.services.query.graphql.notifications.graphql.SpELTemplateRoutingKeyResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;

/**
 * Notification Gateway configuration that enables messaging channel bindings
 * and scans for MessagingGateway on interfaces to create GatewayProxyFactoryBeans.
 *
 */
@Configuration
@EnableBinding(NotificationsGatewayChannels.class)
@IntegrationComponentScan(basePackageClasses=NotificationsGateway.class)
@EnableConfigurationProperties(ActivitiNotificationsGatewayProperties.class)
@ConditionalOnProperty(name="spring.activiti.cloud.services.graphql.notifications.enabled", matchIfMissing = true)
public class NotificationsGatewayAutoConfiguration {

    private final ActivitiNotificationsGatewayProperties properties;

    @Autowired
    public NotificationsGatewayAutoConfiguration(ActivitiNotificationsGatewayProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public RoutingKeyResolver routingKeyResolver() {
        return new SpELTemplateRoutingKeyResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessEngineNotificationTransformer processEngineEventNotificationTransformer() {
        return new GraphQLProcessEngineNotificationTransformer(
            Arrays.asList(properties.getProcessEngineEventAttributeKeys().split(",")),
            properties.getProcessEngineEventTypeKey()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public NotificationsConsumerChannelHandler notificationsConsumerChannelHandler(
               NotificationsGateway notificationsGateway,
               ProcessEngineNotificationTransformer processEngineNotificationTransformer,
               RoutingKeyResolver routingKeyResolver)
    {
        return new NotificationsConsumerChannelHandler(
                       notificationsGateway,
                       processEngineNotificationTransformer,
                       routingKeyResolver);
    }

}
