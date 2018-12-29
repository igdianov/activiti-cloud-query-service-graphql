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
package org.activiti.cloud.services.graphql.ws.schema.datafetcher;

import java.util.List;

import graphql.schema.DataFetchingEnvironment;
import org.activiti.cloud.services.query.graphql.notifications.RoutingKeyResolver;
import org.activiti.cloud.services.query.graphql.notifications.model.EngineEvent;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Flux;

public class EngineEventsFluxPublisherFactory implements EngineEventsPublisherFactory {

    private static Logger log = LoggerFactory.getLogger(EngineEventsFluxPublisherFactory.class);

    private DataFetcherDestinationResolver destinationResolver = new AntPathDestinationResolver();

    private final Flux<EngineEvent> engineEventsFlux;
    private final AntPathMatcher pathMatcher = new AntPathMatcher(".");
    private final RoutingKeyResolver routingKeyResolver ;
    
    public EngineEventsFluxPublisherFactory(Flux<EngineEvent> engineEventsFlux,
                                            RoutingKeyResolver routingKeyResolver) {
        this.engineEventsFlux = engineEventsFlux;
        this.routingKeyResolver = routingKeyResolver;
    }
    
    @Override
    public Publisher<EngineEvent> getPublisher(DataFetchingEnvironment environment) {
        
        List<String> destinations = destinationResolver.resolveDestinations(environment);

        return engineEventsFlux.filter(engineEvent -> {
            String path = routingKeyResolver.resolveRoutingKey(engineEvent);
            
            return destinations.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
        });
    }

    /**
     * @param destinationResolver
     */
    public EngineEventsFluxPublisherFactory destinationResolver(DataFetcherDestinationResolver destinationResolver) {
        this.destinationResolver = destinationResolver;

        return this;
    }

}
