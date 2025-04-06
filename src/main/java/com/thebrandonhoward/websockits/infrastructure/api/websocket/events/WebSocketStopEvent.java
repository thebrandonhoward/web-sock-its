package com.thebrandonhoward.websockits.infrastructure.api.websocket.events;

import org.springframework.context.ApplicationEvent;

public class WebSocketStopEvent extends ApplicationEvent {
    public WebSocketStopEvent(Object source) {
        super(source);
    }
}