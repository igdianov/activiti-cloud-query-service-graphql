package org.activiti.cloud.graphql.config;

import java.util.LinkedHashSet;
import java.util.Set;

import graphql.schema.GraphQLSchema;

public class GraphQLShemaRegistration {

	Set<GraphQLSchema> managedGraphQLSchemas = new LinkedHashSet<GraphQLSchema>();

	public void register(GraphQLSchema graphQLSchema) {
		managedGraphQLSchemas.add(graphQLSchema);
	}

	public GraphQLSchema[] getManagedGraphQLSchemas() {
		return managedGraphQLSchemas.toArray(new GraphQLSchema[] {});
	}

}
