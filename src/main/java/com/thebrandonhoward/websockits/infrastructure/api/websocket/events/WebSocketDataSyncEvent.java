package com.thebrandonhoward.websockits.infrastructure.api.websocket.events;

import org.springframework.context.ApplicationEvent;

public class WebSocketDataSyncEvent extends ApplicationEvent {
    public WebSocketDataSyncEvent(Object source) {
        super(source);
    }
}
