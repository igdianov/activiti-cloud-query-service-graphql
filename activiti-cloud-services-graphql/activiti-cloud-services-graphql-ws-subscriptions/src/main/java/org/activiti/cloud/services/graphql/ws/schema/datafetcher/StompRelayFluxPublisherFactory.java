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
import java.util.Map;

import graphql.schema.DataFetchingEnvironment;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.ReactorNettyTcpStompClient;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

public class StompRelayFluxPublisherFactory implements StompRelayPublisherFactory {

    private static Logger log = LoggerFactory.getLogger(StompRelayFluxPublisherFactory.class);

    private StompRelayDestinationResolver destinationResolver =
            new SimpleStompRelayDataFetcherDestinationResolver();

    private String login = "guest";
    private String passcode = "guest";

    private final ReactorNettyTcpStompClient stompClient;

    public StompRelayFluxPublisherFactory(ReactorNettyTcpStompClient stompClient) {
        this.stompClient = stompClient;
    }
    
    @Override
    public Publisher<Map<String,Object>> getPublisher(DataFetchingEnvironment environment) {
        
        
        Flux<Map<String,Object>> stompRelayObservable = UnicastProcessor.create(emitter -> {

            List<String> destinations = destinationResolver.resolveDestinations(environment);

            StompSessionHandler handler = new StompRelayFluxSinkEmitterHandler(destinations, emitter);

            StompHeaders stompHeaders = new StompHeaders();
            stompHeaders.setLogin(login);
            stompHeaders.setPasscode(passcode);

            stompClient.connect(stompHeaders, handler);
        });

        return stompRelayObservable.share();
        
//        ConnectableFlux<Map<String, Object>> connectableFlux =
//                stompRelayObservable
//                    .share()
//                    .publish();

        //reactor.core.Disposable handle = connectableFlux.connect();

//        return connectableFlux
//                .onBackpressureDrop()
//                .doOnCancel(() -> {
//                    handle.dispose();
//                });
    }


    public StompRelayFluxPublisherFactory login(String login) {
        this.login = login;

        return this;
    }


    public StompRelayFluxPublisherFactory passcode(String passcode) {
        this.passcode = passcode;

        return this;
    }

    /**
     * @param destinationResolver
     */
    public StompRelayFluxPublisherFactory destinationResolver(StompRelayDestinationResolver destinationResolver) {
        this.destinationResolver = destinationResolver;

        return this;
    }

}
