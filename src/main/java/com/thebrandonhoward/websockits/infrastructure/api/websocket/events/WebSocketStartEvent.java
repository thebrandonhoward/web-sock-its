package com.thebrandonhoward.websockits.infrastructure.api.websocket.events;

import org.springframework.context.ApplicationEvent;

public class WebSocketStartEvent extends ApplicationEvent {
    public WebSocketStartEvent(Object source) {
        super(source);
    }
}
