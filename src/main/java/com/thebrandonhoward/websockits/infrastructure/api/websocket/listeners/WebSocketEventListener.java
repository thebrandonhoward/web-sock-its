package com.thebrandonhoward.websockits.infrastructure.api.websocket.listeners;

import com.fasterxml.jackson.databind.JsonNode;
import com.thebrandonhoward.websockits.infrastructure.api.websocket.client.GraphQLWebSocketClient;
import com.thebrandonhoward.websockits.infrastructure.api.websocket.events.WebSocketDataSyncEvent;
import com.thebrandonhoward.websockits.infrastructure.api.websocket.events.WebSocketStartEvent;
import com.thebrandonhoward.websockits.infrastructure.api.websocket.events.WebSocketStopEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class WebSocketEventListener {

    /*
    http://localhost:8080/graphiql?path=/graphql


    subscription GetAddedBooksSubscription {
        bookAdded {
            id
        }
    }
     */
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    private final GraphQLWebSocketClient graphQLWebSocketClient;
    private Disposable subscription;
    private final String subscriptionUrl = "ws://localhost:8080/graphql"; // Replace
    private final String subscriptionQuery = """
        subscription {
            bookAdded {
                id
                name
                pageCount
            }
        }
    """;
    private final Map<String, Object> variables = new HashMap<>();

    @Autowired
    public WebSocketEventListener(GraphQLWebSocketClient graphQLWebSocketClient) {
        this.graphQLWebSocketClient = graphQLWebSocketClient;
    }

    @EventListener
    public void handleWebSocketStartEvent(WebSocketStartEvent event) {
        logger.info("Received WebSocketStartEvent. Starting subscription...");
        if (subscription == null || subscription.isDisposed()) {
            subscription
                    = graphQLWebSocketClient
                        .subscribeToGQL(subscriptionQuery)
//                        .map(str -> {
//                            logger.info("Subscription data: {}", str);
//                            return str;
//                        })
//                        .subscribeToGraphQL(subscriptionUrl, subscriptionQuery, variables, "graphql-transport-ws")
                        .subscribe(
                                data -> logger.info("Received: {}", data),
                                error -> logger.error("Error: {}", error.getMessage(), error),
                                () -> logger.info("Subscription completed")
                        );

            logger.info("WebSocket subscription started.");
        }
        else {
            logger.warn("WebSocket subscription is already active.");
        }
    }

    @EventListener
    public void handleWebSocketDataSyncEvent(WebSocketDataSyncEvent event) {
        logger.info("Received WebSocketDataSyncEvent...");
        try {
            Thread.sleep(Duration.ofMinutes(1));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info("WebSocketDataSyncEvent Complete");
    }

    @EventListener
    public void handleWebSocketStopEvent(WebSocketStopEvent event) {
        logger.info("Received WebSocketStopEvent. Stopping subscription...");
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            logger.info("WebSocket subscription stopped.");
        } else {
            logger.warn("No active WebSocket subscription to stop.");
        }
    }

    // You might have a mechanism to trigger the WebSocketStopEvent
    // For example, another CommandLineRunner, a scheduled task,
    // or a request handler in a web application.
}
