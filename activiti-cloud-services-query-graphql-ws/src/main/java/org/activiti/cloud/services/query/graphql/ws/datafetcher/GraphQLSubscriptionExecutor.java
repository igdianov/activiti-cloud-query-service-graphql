/*
 * Copyright 2017 IntroPro Ventures Inc. and/or its affiliates.
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
package org.activiti.cloud.services.query.graphql.ws.datafetcher;

import java.util.Collections;
import java.util.Map;

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

/**
 * WebSockets specific GraphQLExecutor implementation with support to execute GraphQL subscription queries
 *
 */
public class GraphQLSubscriptionExecutor implements GraphQLExecutor {

    private final GraphQL graphQL;

    /**
     * Creates instance using GraphQLSchema parameter.
     *
     * @param graphQLSchema instance
     */
    public GraphQLSubscriptionExecutor(GraphQLSchema graphQLSchema) {
        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    @Override
    public ExecutionResult execute(String query) {
        return graphQL.execute(query);
    }

    @Override
    public ExecutionResult execute(String query, Map<String, Object> arguments) {

        // Need to inject variables in context to support parameter bindings in reverse queries
        Map<String, Object> context = Collections.singletonMap("variables", arguments);

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(query)
                .variables(arguments)
                .root(context)
                .context(context)
                .build();

        if (arguments == null)
            return graphQL.execute(query);
        else
            return graphQL.execute(executionInput);
    }

}
