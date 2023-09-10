package atypon.app.node.kafka.listener;

import atypon.app.node.kafka.event.WriteEvent;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface EventListener {
    void onEvent(WriteEvent event) throws JsonProcessingException;
}
