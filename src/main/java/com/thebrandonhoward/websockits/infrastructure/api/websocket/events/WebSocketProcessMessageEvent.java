package com.thebrandonhoward.websockits.infrastructure.api.websocket.events;

import org.springframework.context.ApplicationEvent;

public class WebSocketProcessMessageEvent extends ApplicationEvent {
    public WebSocketProcessMessageEvent(Object source) {
        super(source);
    }
}
