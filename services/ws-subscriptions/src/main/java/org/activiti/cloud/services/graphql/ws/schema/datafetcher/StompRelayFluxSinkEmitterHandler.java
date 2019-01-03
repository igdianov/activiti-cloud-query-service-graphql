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

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.activiti.cloud.services.query.graphql.notifications.model.EngineEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSession.Subscription;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.Disposable;
import reactor.core.publisher.FluxSink;

public class StompRelayFluxSinkEmitterHandler extends StompSessionHandlerAdapter implements Disposable {

    private static Logger log = LoggerFactory.getLogger(StompRelayFluxSinkEmitterHandler.class);

	private final List<String> destinations;
	private final FluxSink<Message<EngineEvent>> emitter;

	private StompSession session;

	/**
	 * @param destinations
	 * @param emitter
	 */
	public StompRelayFluxSinkEmitterHandler(List<String> destinations, FluxSink<Message<EngineEvent>> emitter) {
		this.destinations = destinations;
		this.emitter = emitter;

		emitter.onCancel(this);
	}

	@Override
	public Type getPayloadType(StompHeaders headers) {
		return EngineEvent.class;
	}

	@Override
	public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
		this.session = session;

		log.info("Stomp session connected: {}", session.getSessionId());

		for (String destination : destinations) {
		    StompHeaders headers = new StompHeaders();
	        headers.setDestination("/topic/" + destination);
	        // Let's try setting up durable subscription
//            headers.add("durable", "true");
//            headers.add("auto-delete", "false");
//            headers.setId("id");
            
			Subscription subs = session.subscribe(headers, this);
			log.info("Created subscription {} for destination ['{}'] in Stomp session: {}", subs.getSubscriptionId(), destination, session.getSessionId());
		}
	}

	@Override
	public void handleFrame(StompHeaders headers, Object payload) {
	    
        Message<EngineEvent> message = MessageBuilder.withPayload(EngineEvent.class.cast(payload))
                                                     .copyHeaders(toSingleValueMap(headers))
                                                     .build();
	    
		emitter.next(message);
	}
	
    private Map<String, Object> toSingleValueMap(StompHeaders headers) {
        LinkedHashMap<String, Object> singleValueMap = new LinkedHashMap<>(headers.size());
        headers.forEach((key, value) -> singleValueMap.put(key, value.get(0)));
        return singleValueMap;
    }
	

	@Override
	public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload,
			Throwable exception) {
		log.error(exception.getMessage(), exception);
		emitter.error(exception);
	}

	@Override
	public void handleTransportError(StompSession session, Throwable exception) {
		log.error(exception.getMessage(), exception);
		emitter.error(exception);
	}

    @Override
    public void dispose() {
        if(session != null) {
            log.info("Stomp session canceled: {}", session.getSessionId());
            session.disconnect();
        }
    }

}
