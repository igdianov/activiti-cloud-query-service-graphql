package org.activiti.cloud.graphql.stocks;

import java.util.List;

import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import io.leangen.graphql.generator.mapping.common.ObjectScalarAdapter;
import org.activiti.cloud.graphql.config.GraphQLSchemaConfigurer;
import org.activiti.cloud.graphql.config.GraphQLShemaRegistration;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StocksGraphQLSchemaConfigurer implements GraphQLSchemaConfigurer {

	final List<GraphQLApi> resolvers;

	public StocksGraphQLSchemaConfigurer(List<GraphQLApi> resolvers) {
		super();
		this.resolvers = resolvers;
	}
	
	@Override
	public void configure(GraphQLShemaRegistration registry) {

		GraphQLSchema graphQLSchema = new GraphQLSchemaGenerator()
				.withOutputConverters((config, defaults) -> defaults.drop(ObjectScalarAdapter.class))
				.withOperationsFromSingletons(resolvers.toArray()) // register the beans
				.generate();

		registry.register(graphQLSchema);
	}
}
