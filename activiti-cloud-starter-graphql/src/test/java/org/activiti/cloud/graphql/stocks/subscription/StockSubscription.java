package org.activiti.cloud.graphql.stocks.subscription;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLSubscription;
import org.activiti.cloud.graphql.stocks.GraphQLApi;
import org.activiti.cloud.graphql.stocks.model.StockPriceUpdate;
import org.activiti.cloud.graphql.stocks.publisher.StockTickerPublisher;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class StockSubscription implements GraphQLApi {

    private StockTickerPublisher stockTickerPublisher;

    public StockSubscription(StockTickerPublisher stockTickerPublisher) {
        this.stockTickerPublisher = stockTickerPublisher;
    }

    @GraphQLSubscription
    public Publisher<List<StockPriceUpdate>> stockQuotes(List<String> stockCodes, 
    		@GraphQLArgument(defaultValue="100", name = "maxSize") Integer maxSize, 
    		@GraphQLArgument(defaultValue="1", name = "maxTimeSeconds") Integer maxTimeSeconds) 
    {
        return subscribe(stockCodes)
        		.bufferTimeout(maxSize, Duration.ofSeconds(maxTimeSeconds));
    }
    
    public Flux<StockPriceUpdate> subscribe(List<String> stockCodes) {
    	return Optional.ofNullable(stockCodes)
    				.map(it -> stockTickerPublisher.getPublisher()
    						.filter(stockPriceUpdate -> stockCodes.contains(stockPriceUpdate.getStockCode()))
    					)
    				.orElse(stockTickerPublisher.getPublisher());
    }    

}