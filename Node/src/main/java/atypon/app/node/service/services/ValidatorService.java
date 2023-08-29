package atypon.app.node.service.services;

import atypon.app.node.response.ValidatorResponse;
import com.fasterxml.jackson.databind.JsonNode;

public interface ValidatorService {
    ValidatorResponse isDatabaseExists(String databaseName);
    ValidatorResponse isCollectionExists(String databaseName, String collectionName);
    ValidatorResponse isDocumentValid(String database, String targetCollection, JsonNode targetDocument);
    ValidatorResponse isDocumentExists(String database, String collection, String id);
}
