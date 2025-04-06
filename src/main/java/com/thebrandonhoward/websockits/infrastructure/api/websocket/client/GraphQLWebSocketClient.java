package com.thebrandonhoward.websockits.infrastructure.api.websocket.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.ClientResponseField;
import org.springframework.graphql.client.WebGraphQlClient;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class GraphQLWebSocketClient {
    private final WebGraphQlClient webGraphQlClient;
    private final WebSocketClient webSocketClient;
    private final ObjectMapper objectMapper;

    public Flux<String> subscribeToGQL(String graphQLQuery) {
        return webGraphQlClient.document(graphQLQuery)
                .executeSubscription()
                .log()
                .map((response) -> {
                    if (!response.isValid()) { //Contains data and data is not null
                        log.error("Invalid GraphQL response: {}", response);
                    }

                    ClientResponseField field = response.field("data"); //Get a particular field

                    if (field.getValue() == null) {
                        if (field.getErrors().isEmpty()) {
                            log.info("GraphQL query field: {}", graphQLQuery);
                        }
                        else {
                            log.info("GraphQL query missing field: {}", graphQLQuery);
                        }
                    }

                    try {
                        return objectMapper.writeValueAsString(response.getData());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    //return field.toEntity(String.class);
                })
                .retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofMinutes(2)));
    }

    public Flux<JsonNode> subscribeToGraphQL(String url, String subscriptionQuery, Map<String, Object> variables, String protocol) {
        /*
            CONNECTION_INIT("connection_init", false),
            CONNECTION_ACK("connection_ack", false),
            PING("ping", false),
            PONG("pong", false),
            SUBSCRIBE("subscribe", true),
            NEXT("next", true),
            ERROR("error", true),
            COMPLETE("complete", false),
            NOT_SPECIFIED("", false);
         */
        URI uri = URI.create(url);

        return webSocketClient.execute(uri, new WebSocketHandler() {
                    private WebSocketSession session;

                    @Override
                    public Mono<Void> handle(WebSocketSession session) {
                        this.session = session;

                        // 1. Construct Basic Auth header
                        String username = "user2";
                        String password = "password";
                        String authString = username + ":" + password;
                        String base64Auth = Base64.getEncoder().encodeToString(authString.getBytes());
                        String authorizationHeader = "Basic " + base64Auth;

                        // 2. GQL_CONNECTION_INIT with Authorization header
                        Map<String, Object> connectionInitPayload = new HashMap<>();
                        connectionInitPayload.put("Authorization", authorizationHeader); // Set the header

                        Map<String, Object> connectionInitMessage = new HashMap<>();
                        connectionInitMessage.put("type", "connection_init");
                        connectionInitMessage.put("payload", connectionInitPayload);

                        Disposable disposable = session.send(Mono.just(session.textMessage(toJson(connectionInitMessage))))
                                .subscribe();

                        session.receive()
                                .flatMap(webSocketMessage -> {
                                    String payload = webSocketMessage.getPayloadAsText();
                                    try {
                                        JsonNode jsonNode = objectMapper.readTree(payload);
                                        String type = jsonNode.get("type").asText();

                                        switch (type) {
                                            case "connection_ack":
                                                log.info("WebSocket connection acknowledged.");
                                                // 2. GQL_SUBSCRIBE
                                                Map<String, Object> subscribePayload = new HashMap<>();
                                                subscribePayload.put("query", subscriptionQuery);
                                                if (variables != null && !variables.isEmpty()) {
                                                    subscribePayload.put("variables", variables);
                                                }
                                                Map<String, Object> subscribeMessage = new HashMap<>();
                                                subscribeMessage.put("id", "1"); // Unique ID for the subscription
                                                subscribeMessage.put("type", "subscribe");
                                                subscribeMessage.put("payload", subscribePayload);
                                                session.send(Mono.just(session.textMessage(toJson(subscribeMessage)))).subscribe();
                                                break;
                                            case "data":
                                                log.info("Received data: {}", jsonNode.get("payload").get("data"));
                                                return Mono.just(jsonNode.get("payload").get("data"));
                                            case "error":
                                                log.error("Received error: {}", jsonNode.get("payload"));
                                                break;
                                            case "complete":
                                                log.info("Subscription completed.");
                                                // 2. GQL_SUBSCRIBE
//                                                Map<String, Object> subscribePayload = new HashMap<>();
//                                                subscribePayload.put("query", subscriptionQuery);
//                                                if (variables != null && !variables.isEmpty()) {
//                                                    subscribePayload.put("variables", variables);
//                                                }
//                                                Map<String, Object> subscribeMessage = new HashMap<>();
//                                                subscribeMessage.put("id", "1"); // Unique ID for the subscription
//                                                subscribeMessage.put("type", "subscribe");
//                                                subscribeMessage.put("payload", subscribePayload);
//                                                log.info("Subscribing: {}", subscribePayload);
//                                                session.send(Mono.just(session.textMessage(toJson(subscribeMessage)))).subscribe();
                                                break;
                                            case "connection_error":
                                                log.error("WebSocket connection error: {}", jsonNode.get("payload"));
                                                break;
                                            default:
                                                log.warn("Received unknown message type: {}", type);
                                        }
                                    } catch (IOException e) {
                                        log.error("Error processing WebSocket message: {}", payload, e);
                                    }
                                    return Mono.empty();
                                })
                                .doFinally(signalType -> {
                                    log.info("WebSocket session closed: {}", signalType);
                                });
                        return Mono.empty();
                    }
                })
                .ofType(JsonNode.class)
                .flux(); // Filter out non-data messages
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            log.error("Error converting object to JSON", e);
            return "";
        }
    }
}
