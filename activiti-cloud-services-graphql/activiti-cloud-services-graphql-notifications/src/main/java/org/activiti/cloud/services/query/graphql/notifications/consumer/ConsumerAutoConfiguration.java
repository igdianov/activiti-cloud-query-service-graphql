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
package org.activiti.cloud.services.query.graphql.notifications.consumer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.activiti.cloud.services.query.graphql.notifications.model.EngineEvent;
import org.activiti.cloud.services.query.graphql.notifications.transformer.EngineEventsTransformer;
import org.activiti.cloud.services.query.graphql.notifications.transformer.Transformer;
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
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.TopicProcessor;

/**
 * Notification Gateway configuration that enables messaging channel bindings
 * and scans for MessagingGateway on interfaces to create GatewayProxyFactoryBeans.
 *
 */
@Configuration
@EnableBinding(ConsumerChannels.class)
@EnableConfigurationProperties(ConsumerProperties.class)
@ConditionalOnProperty(name="spring.activiti.cloud.services.graphql.notifications.enabled", matchIfMissing = true)
@PropertySource("classpath:META-INF/graphql-notifications.properties")
public class ConsumerAutoConfiguration implements SmartLifecycle  {

    private final ConsumerProperties properties;
    private final List<Subscriber<Message<EngineEvent>>> subscribers = new ArrayList<>();
    boolean running;
    
    private TopicProcessor<Message<EngineEvent>> engineEventsProcessor = TopicProcessor.<Message<EngineEvent>> builder()
                                                                                      .name("engineEventsProcessor")
                                                                                      .autoCancel(false)
                                                                                      .share(true)
                                                                                      .bufferSize(1024)
                                                                                      .build();    

    @Autowired
    public ConsumerAutoConfiguration(ConsumerProperties properties) {
        this.properties = properties;
    }

    @Autowired(required=false)
    public void setSubscribers(List<Subscriber<Message<EngineEvent>>> subscribers) {
        this.subscribers.addAll(subscribers);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public Transformer engineEventsTransformer() {
        return new EngineEventsTransformer(
            Arrays.asList(properties.getProcessEngineEventAttributeKeys().split(",")),
            properties.getProcessEngineEventTypeKey()
        ); 
    }
        
    @Bean
    @ConditionalOnMissingBean
    public ConsumerMessageHandler engineEventsMessageHandler(Transformer engineEventsTransformer,
                                                                 FluxSink<Message<EngineEvent>> engineEventsSink)    {
        return new ConsumerMessageHandler(engineEventsTransformer, engineEventsSink);
    }

    @Bean
    @ConditionalOnMissingBean
    public Flux<Message<EngineEvent>> engineEventsFlux() {
        return engineEventsProcessor.share();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public FluxSink<Message<EngineEvent>> engineEventsSink() {
        return engineEventsProcessor.sink();
    }
    
    @Override
    public void start() {
        subscribers.forEach(s -> engineEventsProcessor.subscribe(s));
        running = true;
    }

    @Override
    public void stop() {
        try {
            engineEventsProcessor.awaitAndShutdown(Duration.ofSeconds(5));
        } finally {
            running = false;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
