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
package org.activiti.cloud.services.graphql.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import org.activiti.cloud.services.graphql.web.ActivitiGraphQLController.GraphQLQueryRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.StandardWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ActivitiGraphQLControllerIT {

    private static final String TASK_NAME = "task1";
    private static final String GRPAPHQL_URL = "/graphql";
    private static final Duration TIMEOUT = Duration.ofMillis(10000);
    
    @LocalServerPort
    private String port;
    
    @Autowired
    private TestRestTemplate rest;

    @SpringBootApplication
    static class Application {
        // Nothing
    }

    @Test
    public void echo() throws Exception {
        int count = 1;
        Flux<String> input = Flux.range(1, count).map(index -> "msg-" + index);
        ReplayProcessor<Object> output = ReplayProcessor.create(count);

        WebSocketClient client = new StandardWebSocketClient();
        client.execute(getUrl("/ws/graphql"),
                       new WebSocketHandler() {
                            @Override
                            public List<String> getSubProtocols() {
                                return Collections.singletonList("graphql-ws");
                            }
                            @Override
                            public Mono<Void> handle(WebSocketSession session) {
                                return session
                                        .send(input.map(session::textMessage))
                                        .thenMany(session.receive().take(count).map(WebSocketMessage::getPayloadAsText))
                                        .subscribeWith(output)
                                        .then();
                            }
                        })                       
                        .block(Duration.ofMillis(5000));

        assertThat(input.collectList().block(Duration.ofMillis(5000))).isEqualTo(output.collectList().block(Duration.ofMillis(5000)));
    }

    protected URI getUrl(String path) throws URISyntaxException {
        return new URI("ws://localhost:" + this.port + path);
    }    
    

    @Test
    public void testWebSockets() throws URISyntaxException {
        WebSocketClient client = new ReactorNettyWebSocketClient();
        ReplayProcessor<String> output = ReplayProcessor.create();

        String initMessage = "{\"type\":\"connection_init\",\"payload\":{}}";

        client.execute(getUrl("/ws/graphql"),
                       session -> session.send(Mono.just(session.textMessage(initMessage)))
                                         .thenMany(session.receive()
                                                          .take(2)
                                                          .map(WebSocketMessage::getPayloadAsText)
                                                          .log())
                                         .subscribeWith(output)
                                         .then())
              .subscribe();
        
        String ackMessage = "{\"payload\":{},\"id\":null,\"type\":\"connection_ack\",\"headers\":{}}";
        String kaMessage = "{\"payload\":{},\"id\":null,\"type\":\"ka\",\"headers\":{}}";
    
        StepVerifier.create(output)
                    .expectNext(ackMessage)
                    .expectNext(kaMessage)
                    .expectComplete()
                    .verify(TIMEOUT);
    }
    
    @Test
    public void testGraphql() {
        GraphQLQueryRequest query = new GraphQLQueryRequest("{Tasks(where:{name:{EQ: \"" + TASK_NAME + "\"}}){select{id assignee priority}}}");

        ResponseEntity<Result> entity = rest.postForEntity(GRPAPHQL_URL, new HttpEntity<>(query), Result.class);

        assertThat(HttpStatus.OK)
            .describedAs(entity.toString())
            .isEqualTo(entity.getStatusCode());

        Result result = entity.getBody();

        assertThat(result).isNotNull();
        assertThat(result.getErrors().isEmpty())
            .describedAs(result.getErrors().toString())
            .isTrue();

        assertThat("{Tasks={select=[{id=1, assignee=assignee, priority=5}]}}")
            .isEqualTo(result.getData().toString());

    }

    @Test
    public void testGraphqlWhere() {
        // @formatter:off
        GraphQLQueryRequest query = new GraphQLQueryRequest(
        	    "query {" +
        	    "	  ProcessInstances(page: {start: 1, limit: 10}," + 
        	    "	    where: {status : {EQ: COMPLETED }}) {" +
        	    "	    pages" +
        	    "	    total" +
        	    "	    select {" +
        	    "	      id" +
        	    "	      processDefinitionId" +
        	    "	      processDefinitionKey" +
        	    "	      status" +
        	    "	      tasks {" +
        	    "	        name" +
        	    "	        status" +
        	    "	      }" +
        	    "	    }" +
        	    "	  }" +
        	    "	}");
       // @formatter:on

        ResponseEntity<Result> entity = rest.postForEntity(GRPAPHQL_URL, new HttpEntity<>(query), Result.class);

        assertThat(HttpStatus.OK)
            .describedAs(entity.toString())
            .isEqualTo(entity.getStatusCode());

        Result result = entity.getBody();

        assertThat(result).isNotNull();
        assertThat(result.getErrors().isEmpty())
            .describedAs(result.getErrors().toString())
            .isTrue();
        assertThat(((Map<String, Object>) result.getData()).get("ProcessInstances")).isNotNull();
    }

    @Test
    public void testGraphqlNesting() {
        // @formatter:off
        GraphQLQueryRequest query = new GraphQLQueryRequest(
                "query {"
                + "ProcessInstances {"
                + "    select {"
                + "      id"
                + "      tasks {"
                + "        id"
                + "        name"
                + "        variables {"
                + "          name"
                + "          value"
                + "        }"
                + "        taskCandidateUsers {"
                + "           taskId"
                + "           userId"
                + "        }"
                + "        taskCandidateGroups {"
                + "           taskId"
                + "           groupId"
                + "        }"
                + "      }"
                + "      variables {"
                + "        name"
                + "        value"
                + "      }"
                + "    }"
                + "  }"
                + "}");
       // @formatter:on

        ResponseEntity<Result> entity = rest.postForEntity(GRPAPHQL_URL, new HttpEntity<>(query), Result.class);

        assertThat(HttpStatus.OK)
            .describedAs(entity.toString())
            .isEqualTo(entity.getStatusCode());

        Result result = entity.getBody();

        assertThat(result).isNotNull();
        assertThat(result.getErrors().isEmpty())
            .describedAs(result.getErrors().toString())
            .isTrue();
        assertThat(((Map<String, Object>) result.getData()).get("ProcessInstances")).isNotNull();
    }

    @Test
    public void testGraphqlReverse() {
        // @formatter:off
        GraphQLQueryRequest query = new GraphQLQueryRequest(
        		" query {"
        	    + " ProcessVariables {"
        	    + "    select {"
        	    + "      id"
        	    + "      name"
        	    + "      value"
        	    + "      processInstance(where: {status: {EQ: RUNNING}}) {"
        	    + "        id"
        	    + "      }"
        	    + "    }"
        	    + "  }"
        	    + "}"
        		);
       // @formatter:on

        ResponseEntity<Result> entity = rest.postForEntity(GRPAPHQL_URL, new HttpEntity<>(query), Result.class);

        assertThat(HttpStatus.OK)
            .describedAs(entity.toString())
            .isEqualTo(entity.getStatusCode());

        Result result = entity.getBody();

        assertThat(result).isNotNull();
        assertThat(result.getErrors().isEmpty())
            .describedAs(result.getErrors().toString())
            .isTrue();
        assertThat(((Map<String, Object>) result.getData()).get("ProcessVariables")).isNotNull();
    }
    
    @Test
    public void testGraphqlArguments() throws JsonParseException, JsonMappingException, IOException {
        GraphQLQueryRequest query = new GraphQLQueryRequest("query TasksQuery($name: String!) {Tasks(where:{name:{EQ: $name}}) {select{id assignee priority}}}");

        HashMap<String, Object> variables = new HashMap<>();
        variables.put("name", TASK_NAME);

        query.setVariables(variables);

        ResponseEntity<Result> entity = rest.postForEntity(GRPAPHQL_URL, new HttpEntity<>(query), Result.class);

        assertThat(HttpStatus.OK)
            .describedAs(entity.toString())
            .isEqualTo(entity.getStatusCode());

        Result result = entity.getBody();

        assertThat(result).isNotNull();
        assertThat(result.getErrors().isEmpty())
            .describedAs(result.getErrors().toString())
            .isTrue();

        assertThat("{Tasks={select=[{id=1, assignee=assignee, priority=5}]}}")
            .isEqualTo(result.getData().toString());
    }
}

class Result implements ExecutionResult {

    private Map<String, Object> data;
    private List<GraphQLError> errors;
    private Map<Object, Object> extensions;

    /**
     * Default
     */
    Result() {
    }

    /**
     * @param data the data to set
     */
    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    /**
     * @param errors the errors to set
     */
    public void setErrors(List<GraphQLError> errors) {
        this.errors = errors;
    }

    /**
     * @param extensions the extensions to set
     */
    public void setExtensions(Map<Object, Object> extensions) {
        this.extensions = extensions;
    }

    @Override
    public <T> T getData() {
        return (T) data;
    }

    @Override
    public List<GraphQLError> getErrors() {
        return errors;
    }

    @Override
    public Map<Object, Object> getExtensions() {
        return extensions;
    }

    @Override
    public Map<String, Object> toSpecification() {
        return null;
    }
}
