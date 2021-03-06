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
package org.activiti.cloud.services.query.graphql.ws.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.cloud.services.query.graphql.ws.config.EnableActivitiGraphQLNotifications;

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.messaging.MessageHandler;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
    properties="spring.activiti.cloud.services.query.graphql.ws.enabled=false")
public class EnableActivitiGraphQLNotificationsTest {

    @Autowired(required=false)
    private MessageHandler graphQLBrokerMessageHandler;

    @Autowired(required=false)
    private GraphQLExecutor graphQLExecutor;

    @EnableAutoConfiguration
    @SpringBootConfiguration
    @EnableActivitiGraphQLNotifications
    static class EnableActivitiGraphQLNotificationsTestApplication {

    }

    @Test
    public void testContextLoads() {
        assertThat(graphQLBrokerMessageHandler).isNotNull();
        assertThat(graphQLExecutor).isNotNull();
    }
}