package atypon.app.node.kafka;

import atypon.app.node.kafka.event.WriteEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {
    private final KafkaTemplate<String, WriteEvent> kafkaTemplate;
    @Autowired
    public KafkaService(KafkaTemplate<String, WriteEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public void broadCast(TopicType topicType, WriteEvent event) {
        kafkaTemplate.send(topicType.getTopicValue(), event);
    }
}
