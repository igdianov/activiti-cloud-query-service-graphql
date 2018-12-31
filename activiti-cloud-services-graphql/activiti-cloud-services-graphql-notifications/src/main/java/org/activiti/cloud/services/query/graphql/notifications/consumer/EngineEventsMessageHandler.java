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

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.cloud.services.query.graphql.notifications.config.NotificationsChannels;
import org.activiti.cloud.services.query.graphql.notifications.model.EngineEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

public class EngineEventsMessageHandler {

    private static Logger logger = LoggerFactory.getLogger(EngineEventsMessageHandler.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    private final FluxSink<Message<EngineEvent>> engineEventsSink;
    private final EngineEventsTransformer transformer;
    
    public EngineEventsMessageHandler(EngineEventsTransformer transformer,
                                      FluxSink<Message<EngineEvent>> engineEventsSink)
    {
        this.engineEventsSink = engineEventsSink;
        this.transformer = transformer;
    }

    @StreamListener(NotificationsChannels.NOTIFICATIONS_CONSUMER)
    public void receive(Message<List<Map<String,Object>>> source) {
        List<Map<String,Object>> events = source.getPayload();
        String sourceRoutingKey = (String) source.getHeaders().get("routingKey");

        logger.info("Recieved source message with routingKey: {}", sourceRoutingKey);
        
        Flux.fromIterable(transformer.transform(events))
            .map(engineEvent -> MessageBuilder.createMessage(engineEvent, source.getHeaders()))
            .doOnNext(engineEventsSink::next)
            .doOnError(error -> logger.error("Error handling message with routingKey: " + sourceRoutingKey, error))
            .retry()
            .subscribe();
    }
}
