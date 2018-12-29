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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.activiti.cloud.services.query.graphql.notifications.NotificationsGateway;
import org.activiti.cloud.services.query.graphql.notifications.RoutingKeyResolver;
import org.activiti.cloud.services.query.graphql.notifications.consumer.DefaultEngineEventsTransformer;
import org.activiti.cloud.services.query.graphql.notifications.consumer.EngineEventsMessageHandler;
import org.activiti.cloud.services.query.graphql.notifications.consumer.EngineEventsTransformer;
import org.activiti.cloud.services.query.graphql.notifications.model.EngineEvent;
import org.activiti.cloud.services.query.graphql.notifications.producer.NotificationGatewayProducer;
import org.activiti.cloud.services.query.graphql.notifications.producer.SpELTemplateRoutingKeyResolver;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.annotation.IntegrationComponentScan;
import reactor.core.Disposable;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.WorkQueueProcessor;

/**
 * Notification Gateway configuration that enables messaging channel bindings
 * and scans for MessagingGateway on interfaces to create GatewayProxyFactoryBeans.
 *
 */
@Configuration
@EnableBinding(NotificationsChannels.class)
@IntegrationComponentScan(basePackageClasses=NotificationsGateway.class)
@EnableConfigurationProperties(NotificationsConsumerProperties.class)
@ConditionalOnProperty(name="spring.activiti.cloud.services.graphql.notifications.enabled", matchIfMissing = true)
@PropertySource("classpath:META-INF/graphql-notifications.properties")
public class NotificationsGatewayAutoConfiguration {

    private final NotificationsConsumerProperties properties;

    @Autowired
    public NotificationsGatewayAutoConfiguration(NotificationsConsumerProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public RoutingKeyResolver routingKeyResolver() {
        return new SpELTemplateRoutingKeyResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public EngineEventsTransformer engineEventsTransformer() {
        return new DefaultEngineEventsTransformer(
            Arrays.asList(properties.getProcessEngineEventAttributeKeys().split(",")),
            properties.getProcessEngineEventTypeKey()
        );
    }
    
    @Bean
    @ConditionalOnMissingBean
    public Subscriber<EngineEvent> notificationGatewayProducer(NotificationsGateway notificationsGateway,
                                                               RoutingKeyResolver routingKeyResolver) {
        return new NotificationGatewayProducer(notificationsGateway,
                                               routingKeyResolver);
    }    

    @Bean
    @ConditionalOnMissingBean
    public EngineEventsMessageHandler engineEventsMessageHandler(EngineEventsTransformer engineEventsTransformer,
                                                                 FluxSink<EngineEvent> engineEventsSink)    {
        return new EngineEventsMessageHandler(engineEventsTransformer, engineEventsSink);
    }

    private WorkQueueProcessor<EngineEvent> engineEventsProcessor = WorkQueueProcessor.<EngineEvent>builder()
                                                                                      .name("engineEventsProcessor")
                                                                                      .autoCancel(false)
                                                                                      .share(true)
                                                                                      .bufferSize(1024)
                                                                                      .build();
    
//    private EmitterProcessor<EngineEvent> engineEventsProcessor = EmitterProcessor.create(1024, false);

    
    @Bean
    @ConditionalOnMissingBean
    public Flux<EngineEvent> engineEventsFlux() {
        return engineEventsProcessor.share();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public FluxSink<EngineEvent> engineEventsSink() {
        return engineEventsProcessor.sink();
    }
    
    @Configuration
    static class EngineEventsFluxSubscribersConfigurer implements SmartLifecycle {

        private final ConnectableFlux<EngineEvent> connectableEngineEventsFlux;
        
        private List<Subscriber<EngineEvent>> subscribers = new ArrayList<>();

        private Disposable control;        

        @Autowired
        public EngineEventsFluxSubscribersConfigurer(Flux<EngineEvent> engineEventsFlux) {
            this.connectableEngineEventsFlux = engineEventsFlux.publish();
        }
        
        @Autowired(required=false)
        public void setSubscribers(List<Subscriber<EngineEvent>> subscribers) {
            subscribers.forEach(s -> connectableEngineEventsFlux.subscribe(s));
        }
        
        @Override
        public void start() {
           this.control = connectableEngineEventsFlux.connect();
        }

        @Override
        public void stop() {
            this.control.dispose();
        }

        @Override
        public boolean isRunning() {
            return this.control != null;
        }
    }
}
