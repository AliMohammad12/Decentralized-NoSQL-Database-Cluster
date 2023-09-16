package atypon.app.node.kafka;

public enum TopicType {
    Create_Document("createDocumentTopic"),
    Create_Database("createDatabaseTopic"),
    Update_Database("updateDatabaseTopic"),
    Delete_Database("deleteDatabaseTopic"),
    Create_Collection("createCollectionTopic"),
    Update_Collection("updateCollectionTopic"),
    Delete_Collection("deleteCollectionTopic"),
    Delete_Documents_ByProperty("deleteDocumentsByPropertyTopic"),
    Delete_Document_ById("deleteDocumentsByIdTopic"),
    Update_Document("updateDocumentTopic"),
    Create_User("createUserTopic"),
    Create_Indexing("createIndexingTopic"),
    Delete_Indexing("deleteIndexingTopic"),
    Share_locking("shareLockingTopic"),
    Remove_lock("removeLockTopic");
    private final String topicValue;
    TopicType(String topicValue) {
        this.topicValue = topicValue;
    }
    public String getTopicValue() {
        return topicValue;
    }
}
