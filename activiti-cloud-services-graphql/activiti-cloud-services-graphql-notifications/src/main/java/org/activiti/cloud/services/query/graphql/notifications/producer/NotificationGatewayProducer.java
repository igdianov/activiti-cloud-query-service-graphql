package org.activiti.cloud.services.query.graphql.notifications.producer;


import org.activiti.cloud.services.query.graphql.notifications.NotificationsGateway;
import org.activiti.cloud.services.query.graphql.notifications.RoutingKeyResolver;
import org.activiti.cloud.services.query.graphql.notifications.model.EngineEvent;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.SignalType;

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

    @Override
    public void dispose() {
        log.info("dispose");
        super.dispose();
    }
  
    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        log.info("hookOnSubscribe {}", subscription);
        super.hookOnSubscribe(subscription);
    }

    @Override
    protected void hookOnComplete() {
        log.info("hookOnComplete");
        super.hookOnComplete();
    }

    @Override
    protected void hookOnError(Throwable throwable) {
        log.error("hookOnError", throwable);
        super.hookOnError(throwable);
    }

    @Override
    protected void hookOnCancel() {
        log.info("hookOnCancel");
        super.hookOnCancel();
    }

    @Override
    protected void hookFinally(SignalType type) {
        log.info("hookFinally {}", type);
        super.hookFinally(type);
    }

}