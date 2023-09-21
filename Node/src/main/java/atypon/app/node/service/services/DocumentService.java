package atypon.app.node.service.services;

import atypon.app.node.request.document.DocumentRequest;
import atypon.app.node.request.document.DocumentUpdateRequest;
import atypon.app.node.request.document.DocumentRequestByProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;

public interface DocumentService {
    void addDocument(DocumentRequest request) throws JsonProcessingException;
    ArrayNode readDocumentProperty(DocumentRequestByProperty documentRequestByProperty) throws IOException;
    void deleteDocumentByProperty(DocumentRequestByProperty documentRequestByProperty) throws IOException;
    JsonNode readDocumentById(String database, String collection, JsonNode document) throws IOException;
    void updateDocument(DocumentUpdateRequest documentUpdateRequest) throws IOException;
    void deleteDocumentById(String database, String collection, JsonNode documentData) throws IOException;
}
