package atypon.app.node.kafka;

public enum TopicType {
    Create_Document_Topic("createDocumentTopic");
    private final String topicValue;

    TopicType(String topicValue) {
        this.topicValue = topicValue;
    }
    public String getTopicValue() {
        return topicValue;
    }
}
