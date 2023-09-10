package atypon.app.node.service.services;

import atypon.app.node.indexing.IndexObject;
import atypon.app.node.request.document.DocumentRequestByProperty;
import atypon.app.node.response.ValidatorResponse;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public interface ValidatorService {
    ValidatorResponse isDatabaseExists(String databaseName);
    ValidatorResponse isCollectionExists(String databaseName, String collectionName);
    ValidatorResponse isDocumentValid(String database, String targetCollection, JsonNode targetDocument);
    ValidatorResponse isDocumentExists(String database, String collection, JsonNode document);
    ValidatorResponse isDocumentUpdateRequestValid(String database, String collection, JsonNode document, JsonNode documentInfo);
    ValidatorResponse isUsernameExists(String username) throws IOException;
    ValidatorResponse isIndexCreationAllowed(IndexObject indexObject);
    ValidatorResponse IsIndexDeletionAllowed(IndexObject indexObject);
    ValidatorResponse isDocumentRequestValid(DocumentRequestByProperty documentRequestByProperty);
}
