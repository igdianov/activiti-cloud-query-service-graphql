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
package org.activiti.cloud.services.query.graphql.notifications;

import org.activiti.cloud.services.query.graphql.notifications.config.NotificationsChannels;
import org.activiti.cloud.services.query.graphql.notifications.model.EngineEvent;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.GatewayHeader;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

@MessagingGateway
public interface NotificationsGateway {

    @Gateway(requestChannel = NotificationsChannels.NOTIFICATIONS_GATEWAY,
        headers={@GatewayHeader(name="content-type", value="application/json")}
    )
    public void send(@Payload EngineEvent notification, @Header("routingKey") String routingKey);
}
