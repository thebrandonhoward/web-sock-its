package com.thebrandonhoward.websockits.infrastructure.api.websocket.interceptors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.client.ClientGraphQlRequest;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.GraphQlClientInterceptor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class GQLClientInterceptor implements GraphQlClientInterceptor {

    @Override
    public Mono<ClientGraphQlResponse> intercept(ClientGraphQlRequest request, Chain chain) {
        log.info("GQLClientInterceptor intercepted request: {}", request);
        return chain.next(request);
    }

    @Override
    public Flux<ClientGraphQlResponse> interceptSubscription(ClientGraphQlRequest request, SubscriptionChain chain) {
        log.info("GQLClientInterceptor interceptSubscription request: {}", request);
        return chain.next(request);
    }

}