package org.activiti.cloud.services.query.graphql.notifications.producer;

import org.activiti.cloud.services.query.graphql.notifications.RoutingKeyResolver;
import org.activiti.cloud.services.query.graphql.notifications.SpELTemplateRoutingKeyResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;

@Configuration
@EnableBinding(ProducerChannels.class)
@IntegrationComponentScan(basePackageClasses=NotificationsGateway.class)
public class ProducerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RoutingKeyResolver routingKeyResolver() {
        return new SpELTemplateRoutingKeyResolver();
    }

    //@Bean
    @ConditionalOnMissingBean
    public NotificationsProducer notificationProducer(NotificationsGateway notificationsGateway,
                                                      RoutingKeyResolver routingKeyResolver) {
        return new NotificationsProducer(notificationsGateway,
                                         routingKeyResolver);
    }    
}
