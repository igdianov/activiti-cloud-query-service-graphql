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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.activiti.cloud.services.query.graphql.notifications.config.ActivitiNotificationsGatewayProperties;
import org.activiti.cloud.services.query.graphql.notifications.consumer.ProcessEngineNotificationTransformer;
import org.activiti.cloud.services.query.graphql.notifications.graphql.GraphQLProcessEngineNotification;
import org.activiti.cloud.services.query.graphql.notifications.graphql.GraphQLProcessEngineNotificationTransformer;
import org.activiti.cloud.services.query.graphql.notifications.model.ProcessEngineNotification;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GraphQLProcessEngineNotificationTransformerTest {

    private static Logger LOGGER = LoggerFactory.getLogger(GraphQLProcessEngineNotificationTransformerTest.class);

    private ActivitiNotificationsGatewayProperties properties = new ActivitiNotificationsGatewayProperties();
    
    private ProcessEngineNotificationTransformer subject;

    @Before
    public void setUp() {
        String engineEventAttributeKeys = properties.getProcessEngineEventAttributeKeys();
        String eventTypeKey = properties.getProcessEngineEventTypeKey();

    	subject = new GraphQLProcessEngineNotificationTransformer(
                Arrays.asList(engineEventAttributeKeys.split(",")), eventTypeKey);
    }
    
    
    @Test
    public void transform() throws JsonProcessingException {
        // given
        List<Map<String, Object>> events = new ArrayList<Map<String, Object>>() {
            private static final long serialVersionUID = 1L;
        {
            add(new GraphQLProcessEngineNotification() {
                private static final long serialVersionUID = 1L;
            {
                put("serviceName","rb");
                put("appName","app");
                put("processInstanceId","p1");
                put("processDefinitionId","pd1");
                put("eventType","type1");
                put("entityId","e1");
            }});

            add(new GraphQLProcessEngineNotification() {
                private static final long serialVersionUID = 1L;
            {
                put("serviceName","rb");
                put("appName","app");
                put("processInstanceId","p1");
                put("processDefinitionId","pd1");
                put("eventType","type2");
                put("entityId","e1");
            }});

            add(new GraphQLProcessEngineNotification() {
                private static final long serialVersionUID = 1L;
            {
                put("serviceName","rb");
                put("appName","app");
                put("processInstanceId","p1");
                put("processDefinitionId","pd1");
                put("eventType","type2");
                put("entityId","e1");
            }});

            add(new GraphQLProcessEngineNotification() {
                private static final long serialVersionUID = 1L;
            {
                put("serviceName","rb1");
                put("appName","app");
                put("processInstanceId","p1");
                put("processDefinitionId","pd1");
                put("eventType","type1");
                put("entityId","e1");
            }});

        }};

        // when
        List<ProcessEngineNotification> notifications = subject.transform(events);

        LOGGER.info("\n{}", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(notifications));

        // then
        assertThat(notifications).hasSize(2);

        assertThat(notifications.get(1).get("serviceName")).isEqualTo("rb");
        assertThat(notifications.get(1).keySet())
                .containsOnly("serviceName","appName","type1","type2");
        assertThat(notifications.get(1).get("type2")).asList().hasSize(2);


        assertThat(notifications.get(0).get("serviceName")).isEqualTo("rb1");
        assertThat(notifications.get(0).keySet())
                .containsOnly("serviceName","appName","type1");
        assertThat(notifications.get(0).get("type1")).asList().hasSize(1);

    }

    @Test
    public void transformFilterNullAttributes() throws JsonProcessingException {
        // given
        List<Map<String, Object>> events = new ArrayList<Map<String, Object>>() {
            private static final long serialVersionUID = 1L;
        {
            add(new GraphQLProcessEngineNotification() {
                private static final long serialVersionUID = 1L;
            {
                put("serviceName", null);
                put("appName","app");
                put("processInstanceId","p1");
                put("processDefinitionId","pd1");
                put("eventType","type1");
                put("entityId","e1");
            }});

            add(new GraphQLProcessEngineNotification() {
                private static final long serialVersionUID = 1L;
            {
                put("serviceName","rb");
                put("appName","app");
                put("processInstanceId",null);
                put("processDefinitionId","pd1");
                put("eventType","type2");
                put("entityId","e1");
            }});

            add(new GraphQLProcessEngineNotification() {
                private static final long serialVersionUID = 1L;
            {
                put("serviceName","rb");
                put("appName","app");
                put("processInstanceId","p1");
                put("processDefinitionId","pd1");
                put("eventType","type2");
                put("entityId","e1");
            }});

            add(new GraphQLProcessEngineNotification() {
                private static final long serialVersionUID = 1L;
            {
                put("serviceName","rb1");
                put("appName","app");
                put("processInstanceId","p1");
                put("processDefinitionId","pd1");
                put("eventType","type1");
                put("entityId","e1");
            }});

        }};

        // when
        List<ProcessEngineNotification> notifications = subject.transform(events);

        LOGGER.info("\n{}", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(notifications));

        // then
        assertThat(notifications).hasSize(2);

        assertThat(notifications.get(1).get("serviceName")).isEqualTo("rb");
        assertThat(notifications.get(1).keySet())
                .containsOnly("serviceName","appName","type2");
        assertThat(notifications.get(1).get("type2")).asList().hasSize(2);

        assertThat(notifications.get(0).get("serviceName")).isEqualTo("rb1");
        assertThat(notifications.get(0).keySet())
                .containsOnly("serviceName","appName","type1");
        assertThat(notifications.get(0).get("type1")).asList().hasSize(1);


    }

    @Test
    public void transformFilterMissingAttributes() throws JsonProcessingException {
        // given
        List<Map<String, Object>> events = new ArrayList<Map<String, Object>>() {
            private static final long serialVersionUID = 1L;
        {
            add(new GraphQLProcessEngineNotification() {
                private static final long serialVersionUID = 1L;
            {
                put("appName","app");
                put("processInstanceId","p1");
                put("processDefinitionId","pd1");
                put("eventType","type1");
                put("entityId","e1");
            }});

            add(new GraphQLProcessEngineNotification() {
                private static final long serialVersionUID = 1L;
            {
                put("serviceName","rb");
                put("appName","app");
                put("processDefinitionId","pd1");
                put("eventType","type2");
                put("entityId","e1");
            }});

            add(new GraphQLProcessEngineNotification() {
                private static final long serialVersionUID = 1L;
            {
                put("serviceName","rb");
                put("appName","app");
                put("processInstanceId","p1");
                put("processDefinitionId","pd1");
                put("eventType","type2");
                put("entityId","e1");
            }});

            add(new GraphQLProcessEngineNotification() {
                private static final long serialVersionUID = 1L;
            {
                put("serviceName","rb1");
                put("appName","app");
                put("processInstanceId","p1");
                put("processDefinitionId","pd1");
                put("entityId","e1");
            }});

        }};

        // when
        List<ProcessEngineNotification> notifications = subject.transform(events);

        LOGGER.info("\n{}", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(notifications));

        // then
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).get("serviceName")).isEqualTo("rb");
        assertThat(notifications.get(0).keySet())
            .containsOnly("serviceName","appName","type2");
        assertThat(notifications.get(0).get("type2")).asList().hasSize(2);

    }
    
    @Test
    public void test1() throws JsonParseException, JsonMappingException, IOException {
    	// given
    	String json = "["
    			+ "{\"eventType\":\"PROCESS_CREATED\",\"id\":\"45c13b64-3080-4033-b8a0-de034de79fff\",\"timestamp\":1539759664348,\"entity\":{\"id\":\"69d56b89-d1da-11e8-9cf0-0a580a2c1105\",\"processDefinitionId\":\"SimpleProcess:1:d16b315b-d197-11e8-9cf0-0a580a2c1105\",\"processDefinitionKey\":\"SimpleProcess\",\"initiator\":\"hruser\",\"startDate\":\"2018-10-17T07:01:04.347+0000\",\"status\":\"RUNNING\"},\"appName\":\"default-app\",\"serviceFullName\":\"rb-my-app\",\"appVersion\":\"\",\"serviceName\":\"rb-my-app\",\"serviceVersion\":\"\",\"serviceType\":\"runtime-bundle\",\"entityId\":\"69d56b89-d1da-11e8-9cf0-0a580a2c1105\"},"
    			+ "{\"eventType\":\"VARIABLE_CREATED\",\"id\":\"6995a480-79d8-47f6-9fed-ce8256f00a8a\",\"timestamp\":1539759664348,\"entity\":{\"name\":\"firstName\",\"type\":\"string\",\"processInstanceId\":\"69d56b89-d1da-11e8-9cf0-0a580a2c1105\",\"value\":\"Paulo\",\"taskVariable\":false},\"appName\":\"default-app\",\"serviceFullName\":\"rb-my-app\",\"appVersion\":\"\",\"serviceName\":\"rb-my-app\",\"serviceVersion\":\"\",\"serviceType\":\"runtime-bundle\",\"entityId\":\"firstName\"},"
    			+ "{\"eventType\":\"VARIABLE_CREATED\",\"id\":\"12c5f1a2-eaee-4f9c-922c-07193d908dc1\",\"timestamp\":1539759664348,\"entity\":{\"name\":\"lastName\",\"type\":\"string\",\"processInstanceId\":\"69d56b89-d1da-11e8-9cf0-0a580a2c1105\",\"value\":\"Silva\",\"taskVariable\":false},\"appName\":\"default-app\",\"serviceFullName\":\"rb-my-app\",\"appVersion\":\"\",\"serviceName\":\"rb-my-app\",\"serviceVersion\":\"\",\"serviceType\":\"runtime-bundle\",\"entityId\":\"lastName\"},"
    			+ "{\"eventType\":\"VARIABLE_CREATED\",\"id\":\"f5755eb9-79db-4ef1-9cd0-f9ee0f2bc798\",\"timestamp\":1539759664348,\"entity\":{\"name\":\"age\",\"type\":\"integer\",\"processInstanceId\":\"69d56b89-d1da-11e8-9cf0-0a580a2c1105\",\"value\":25,\"taskVariable\":false},\"appName\":\"default-app\",\"serviceFullName\":\"rb-my-app\",\"appVersion\":\"\",\"serviceName\":\"rb-my-app\",\"serviceVersion\":\"\",\"serviceType\":\"runtime-bundle\",\"entityId\":\"age\"},"
    			+ "{\"eventType\":\"PROCESS_STARTED\",\"id\":\"5e72ecfd-9611-43fd-a363-12c7d348c051\",\"timestamp\":1539759664348,\"entity\":{\"id\":\"69d56b89-d1da-11e8-9cf0-0a580a2c1105\",\"processDefinitionId\":\"SimpleProcess:1:d16b315b-d197-11e8-9cf0-0a580a2c1105\",\"processDefinitionKey\":\"SimpleProcess\",\"initiator\":\"hruser\",\"startDate\":\"2018-10-17T07:01:04.347+0000\",\"status\":\"RUNNING\"},\"appName\":\"default-app\",\"serviceFullName\":\"rb-my-app\",\"appVersion\":\"\",\"serviceName\":\"rb-my-app\",\"serviceVersion\":\"\",\"serviceType\":\"runtime-bundle\",\"entityId\":\"69d56b89-d1da-11e8-9cf0-0a580a2c1105\"},"
    			+ "{\"eventType\":\"ACTIVITY_STARTED\",\"id\":\"40d82d95-cc0e-4b7c-8a62-5a84d11e7066\",\"timestamp\":1539759664348,\"entity\":{\"processInstanceId\":\"69d56b89-d1da-11e8-9cf0-0a580a2c1105\",\"processDefinitionId\":\"SimpleProcess:1:d16b315b-d197-11e8-9cf0-0a580a2c1105\",\"activityType\":\"startEvent\",\"elementId\":\"startEvent1\"},\"appName\":\"default-app\",\"serviceFullName\":\"rb-my-app\",\"appVersion\":\"\",\"serviceName\":\"rb-my-app\",\"serviceVersion\":\"\",\"serviceType\":\"runtime-bundle\",\"entityId\":\"69d56b89-d1da-11e8-9cf0-0a580a2c1105\",\"processDefinitionId\":\"SimpleProcess:1:d16b315b-d197-11e8-9cf0-0a580a2c1105\",\"processInstanceId\":\"69d56b89-d1da-11e8-9cf0-0a580a2c1105\"},"
    			+ "{\"eventType\":\"ACTIVITY_COMPLETED\",\"id\":\"9910b0cd-2a5a-466b-801e-699e57f49914\",\"timestamp\":1539759664348,\"entity\":{\"processInstanceId\":\"69d56b89-d1da-11e8-9cf0-0a580a2c1105\",\"processDefinitionId\":\"SimpleProcess:1:d16b315b-d197-11e8-9cf0-0a580a2c1105\",\"activityType\":\"startEvent\",\"elementId\":\"startEvent1\"},\"appName\":\"default-app\",\"serviceFullName\":\"rb-my-app\",\"appVersion\":\"\",\"serviceName\":\"rb-my-app\",\"serviceVersion\":\"\",\"serviceType\":\"runtime-bundle\",\"entityId\":\"69d56b89-d1da-11e8-9cf0-0a580a2c1105\",\"processDefinitionId\":\"SimpleProcess:1:d16b315b-d197-11e8-9cf0-0a580a2c1105\",\"processInstanceId\":\"69d56b89-d1da-11e8-9cf0-0a580a2c1105\"},"
    			+ "{\"eventType\":\"SEQUENCE_FLOW_TAKEN\",\"id\":\"22434b43-9c4d-47b9-b92c-a67f01652938\",\"timestamp\":1539759664348,\"entity\":{\"processInstanceId\":\"69d56b89-d1da-11e8-9cf0-0a580a2c1105\",\"processDefinitionId\":\"SimpleProcess:1:d16b315b-d197-11e8-9cf0-0a580a2c1105\",\"sourceActivityElementId\":\"startEvent1\",\"sourceActivityType\":\"org.activiti.bpmn.model.StartEvent\",\"targetActivityElementId\":\"sid-CDFE7219-4627-43E9-8CA8-866CC38EBA94\",\"targetActivityName\":\"Perform action\",\"targetActivityType\":\"org.activiti.bpmn.model.UserTask\"},\"appName\":\"default-app\",\"serviceFullName\":\"rb-my-app\",\"appVersion\":\"\",\"serviceName\":\"rb-my-app\",\"serviceVersion\":\"\",\"serviceType\":\"runtime-bundle\",\"entityId\":\"69d56b89-d1da-11e8-9cf0-0a580a2c1105\"},"
    			+ "{\"eventType\":\"ACTIVITY_STARTED\",\"id\":\"7b6f6fba-b8a6-441d-b369-46f61119db21\",\"timestamp\":1539759664348,\"entity\":{\"processInstanceId\":\"69d56b89-d1da-11e8-9cf0-0a580a2c1105\",\"processDefinitionId\":\"SimpleProcess:1:d16b315b-d197-11e8-9cf0-0a580a2c1105\",\"activityName\":\"Perform action\",\"activityType\":\"userTask\",\"elementId\":\"sid-CDFE7219-4627-43E9-8CA8-866CC38EBA94\"},\"appName\":\"default-app\",\"serviceFullName\":\"rb-my-app\",\"appVersion\":\"\",\"serviceName\":\"rb-my-app\",\"serviceVersion\":\"\",\"serviceType\":\"runtime-bundle\",\"entityId\":\"69d56b89-d1da-11e8-9cf0-0a580a2c1105\",\"processDefinitionId\":\"SimpleProcess:1:d16b315b-d197-11e8-9cf0-0a580a2c1105\",\"processInstanceId\":\"69d56b89-d1da-11e8-9cf0-0a580a2c1105\"},"
    			+ "{\"eventType\":\"TASK_CANDIDATE_GROUP_ADDED\",\"id\":\"50c3bc94-eb6c-4c45-9d50-c5fa6a8901d2\",\"timestamp\":1539759664350,\"entity\":{\"taskId\":\"69d5929f-d1da-11e8-9cf0-0a580a2c1105\",\"groupId\":\"hr\"},\"appName\":\"default-app\",\"serviceFullName\":\"rb-my-app\",\"appVersion\":\"\",\"serviceName\":\"rb-my-app\",\"serviceVersion\":\"\",\"serviceType\":\"runtime-bundle\",\"entityId\":\"hr\"},"
    			+ "{\"eventType\":\"TASK_CREATED\",\"id\":\"a90cb2ef-6418-4464-bd3b-baef027e041b\",\"timestamp\":1539759664351,\"entity\":{\"id\":\"69d5929f-d1da-11e8-9cf0-0a580a2c1105\",\"name\":\"Perform action\",\"status\":\"CREATED\",\"createdDate\":\"2018-10-17T07:01:04.348+0000\",\"priority\":50,\"processDefinitionId\":\"SimpleProcess:1:d16b315b-d197-11e8-9cf0-0a580a2c1105\",\"processInstanceId\":\"69d56b89-d1da-11e8-9cf0-0a580a2c1105\"},\"appName\":\"default-app\",\"serviceFullName\":\"rb-my-app\",\"appVersion\":\"\",\"serviceName\":\"rb-my-app\",\"serviceVersion\":\"\",\"serviceType\":\"runtime-bundle\",\"entityId\":\"69d5929f-d1da-11e8-9cf0-0a580a2c1105\"}"
    			+ "]"; 

    	List<Map<String,Object>> events = new ObjectMapper().readValue(json, new TypeReference<List<Map<String,Object>>>(){});
    	JsonNode jsonNode = new ObjectMapper().readTree(json);
    	
        // when
        List<ProcessEngineNotification> notifications = subject.transform(events);
        
        LOGGER.info("\n{}", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(notifications));

        // then
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0)).hasSize(10);
        
    	
    }

}
