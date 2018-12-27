package org.activiti.cloud.graphql.config;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

public class GraphQLSchemaFactoryBean extends AbstractFactoryBean<GraphQLSchema>{
	
	private final GraphQLSchema[] managedGraphQLSchemas;

	public GraphQLSchemaFactoryBean(GraphQLSchema[] managedGraphQLSchemas) {
		this.managedGraphQLSchemas = managedGraphQLSchemas;
	}

	@Override
	protected GraphQLSchema createInstance() throws Exception {
		
		GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema();
		
		List<GraphQLFieldDefinition> mutations = Stream.of(managedGraphQLSchemas)
			.map(GraphQLSchema::getMutationType)
			.filter(Objects::nonNull)
			.map(GraphQLObjectType::getFieldDefinitions)
			.flatMap(children -> children.stream())
			.collect(Collectors.toList());

		List<GraphQLFieldDefinition> queries = Stream.of(managedGraphQLSchemas)
			.map(GraphQLSchema::getQueryType)
			.filter(Objects::nonNull)
			.map(GraphQLObjectType::getFieldDefinitions)
			.flatMap(children -> children.stream())
			.collect(Collectors.toList());
		
		List<GraphQLFieldDefinition> subscriptions = Stream.of(managedGraphQLSchemas)
			.map(GraphQLSchema::getSubscriptionType)
			.filter(Objects::nonNull)
			.map(GraphQLObjectType::getFieldDefinitions)
			.flatMap(children -> children.stream())
			.collect(Collectors.toList());

		if(!mutations.isEmpty())
			schemaBuilder.mutation(GraphQLObjectType.newObject().name("Mutation").fields(mutations));

		if(!queries.isEmpty())
			schemaBuilder.query(GraphQLObjectType.newObject().name("Query").fields(queries));

		if(!subscriptions.isEmpty())
			schemaBuilder.subscription(GraphQLObjectType.newObject().name("Subscription").fields(subscriptions));
		
		return schemaBuilder.build();
	}

	@Override
	public Class<?> getObjectType() {
		return GraphQLSchema.class;
	}

}