package org.activiti.cloud.services.query.graphql.notifications.producer;


import org.activiti.cloud.services.query.graphql.notifications.NotificationsGateway;
import org.activiti.cloud.services.query.graphql.notifications.RoutingKeyResolver;
import org.activiti.cloud.services.query.graphql.notifications.model.EngineEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;

public class NotificationGatewayProducer extends BaseSubscriber<EngineEvent>{

    private static Logger log = LoggerFactory.getLogger(NotificationGatewayProducer.class);

    private final NotificationsGateway notificationsGateway;
    private final RoutingKeyResolver routingKeyResolver;


    public NotificationGatewayProducer(NotificationsGateway notificationsGateway,
                                       RoutingKeyResolver routingKeyResolver)
    {
        this.notificationsGateway = notificationsGateway;
        this.routingKeyResolver = routingKeyResolver;
    }

    @Override
    protected void hookOnNext(EngineEvent notification) {
        String routingKey = routingKeyResolver.resolveRoutingKey(notification);

        log.info("Routing notification to: {}", routingKey);
        
        notificationsGateway.send(notification, routingKey);
    }

}