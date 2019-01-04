package org.activiti.cloud.services.query.graphql.notifications.producer;


import org.activiti.cloud.services.query.graphql.notifications.RoutingKeyResolver;
import org.activiti.cloud.services.query.graphql.notifications.model.EngineEvent;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.SignalType;

public class NotificationsProducer extends BaseSubscriber<Message<EngineEvent>>{

    private static Logger log = LoggerFactory.getLogger(NotificationsProducer.class);

    private final NotificationsGateway notificationsGateway;
    private final RoutingKeyResolver routingKeyResolver;


    public NotificationsProducer(NotificationsGateway notificationsGateway,
                                       RoutingKeyResolver routingKeyResolver)
    {
        this.notificationsGateway = notificationsGateway;
        this.routingKeyResolver = routingKeyResolver;
    }
    
    public void send(EngineEvent event) {
        String routingKey = routingKeyResolver.resolveRoutingKey(event);

        log.info("Routing notification to: {}", routingKey);
        
        notificationsGateway.send(event, routingKey);
    }

    @Override
    protected void hookOnNext(Message<EngineEvent> message) {
        EngineEvent payload = message.getPayload();
        
        send(payload);
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