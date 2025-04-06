package com.thebrandonhoward.websockits.infrastructure.api.websocket.interceptors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.client.WebSocketGraphQlClientInterceptor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Slf4j
public class GraphQLClientInterceptor implements WebSocketGraphQlClientInterceptor {

    @Override
    public Mono<Object> connectionInitPayload() {
        log.info("GraphQLClientInterceptor connectionInitPayload");
        return Mono.empty();
    }

    @Override
    public Mono<Void> handleConnectionAck(Map<String, Object> ackPayload) {
        log.info("GraphQLClientInterceptor handleConnectionAck: {}", ackPayload);
        return Mono.empty();
    }

}
