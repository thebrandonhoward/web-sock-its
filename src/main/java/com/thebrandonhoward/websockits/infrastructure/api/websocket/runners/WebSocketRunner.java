package com.thebrandonhoward.websockits.infrastructure.api.websocket.runners;

import com.fasterxml.jackson.databind.JsonNode;
import com.thebrandonhoward.websockits.infrastructure.api.websocket.events.WebSocketDataSyncEvent;
import com.thebrandonhoward.websockits.infrastructure.api.websocket.events.WebSocketStartEvent;
import com.thebrandonhoward.websockits.infrastructure.api.websocket.events.WebSocketStopEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketRunner implements CommandLineRunner {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void run(String... args) throws Exception {
        // Publish an event to start the WebSocket subscription
        eventPublisher.publishEvent(new WebSocketStartEvent(this));

        // Sync any data
        eventPublisher.publishEvent(new WebSocketDataSyncEvent(this));

        // The application will now continue to run, and the WebSocket client
        // will be started by the event listener.

        // You might have other logic here, or the application might naturally
        // stay alive (e.g., if it's a web application).

        // For this example, we'll rely on the application context staying active.
        // In a real scenario, you might have a condition or another event
        // to trigger the stopping of the WebSocket client.
        System.out.println("WebSocket start event published. Application running...");
    }
}