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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.cloud.services.query.graphql.notifications.NotificationsGateway;
import org.activiti.cloud.services.query.graphql.notifications.RoutingKeyResolver;
import org.activiti.cloud.services.query.graphql.notifications.config.NotificationsGatewayChannels;
import org.activiti.cloud.services.query.graphql.notifications.model.ProcessEngineNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;

public class NotificationsConsumerChannelHandler {

    private static Logger log = LoggerFactory.getLogger(NotificationsConsumerChannelHandler.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    private final ProcessEngineNotificationTransformer transformer;
    private final NotificationsGateway notificationsGateway;
    private final RoutingKeyResolver routingKeyResolver;


    public NotificationsConsumerChannelHandler(NotificationsGateway notificationsGateway,
                                               ProcessEngineNotificationTransformer transformer,
                                               RoutingKeyResolver routingKeyResolver)
    {

        this.transformer = transformer;
        this.notificationsGateway = notificationsGateway;
        this.routingKeyResolver = routingKeyResolver;
    }

    @StreamListener(NotificationsGatewayChannels.NOTIFICATIONS_CONSUMER)
    public synchronized void receive(Message<List<Map<String,Object>>> source) throws JsonProcessingException {
        List<Map<String,Object>> events = source.getPayload();
        String sourceRoutingKey = (String) source.getHeaders().get("routingKey");

        log.info("Recieved source message with routingKey: {}", sourceRoutingKey);
        
        if(log.isDebugEnabled())
            log.debug("Source events: {}", objectMapper.writeValueAsString(events));
    	
        List<ProcessEngineNotification> notifications = transformer.transform(events);

        if(log.isDebugEnabled())
            log.debug("Transformed notifications {}",objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(notifications));

        for (ProcessEngineNotification notification : notifications) {

            String routingKey = routingKeyResolver.resolveRoutingKey(notification);

            log.info("Routing notification to: {}", routingKey);
            
            notificationsGateway.send(notification, routingKey);
        }
    }




}
