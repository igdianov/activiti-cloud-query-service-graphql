package org.activiti.cloud.services.query.graphql.notifications.producer;

import org.activiti.cloud.services.query.graphql.notifications.NotificationsGateway;
import org.activiti.cloud.services.query.graphql.notifications.RoutingKeyResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationsGatewayProducerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RoutingKeyResolver routingKeyResolver() {
        return new SpELTemplateRoutingKeyResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public NotificationGatewayProducer notificationGatewayProducer(NotificationsGateway notificationsGateway,
                                                               RoutingKeyResolver routingKeyResolver) {
        return new NotificationGatewayProducer(notificationsGateway,
                                               routingKeyResolver);
    }    
}
