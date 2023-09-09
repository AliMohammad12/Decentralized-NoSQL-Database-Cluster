package atypon.app.node.service.services;

import atypon.app.node.request.document.DocumentRequestByProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public interface DocumentService {
    void addDocument(String databaseName, String targetCollection, JsonNode document) throws JsonProcessingException;
    JsonNode readDocumentProperty(DocumentRequestByProperty documentRequestByProperty) throws IOException;
    void deleteDocumentByProperty(DocumentRequestByProperty documentRequestByProperty) throws IOException;
    void updateDocument();
}
