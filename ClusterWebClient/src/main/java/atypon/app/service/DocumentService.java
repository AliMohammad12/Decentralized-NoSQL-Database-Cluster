package atypon.app.service;

import atypon.app.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class DocumentService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);
    private final RestTemplate restTemplate;
    @Autowired
    public DocumentService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    public void updateDocument(String id, int version,
                               Map<String, Object> newProperties,
                               CollectionData collectionData) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode request = objectMapper.createObjectNode();
        ObjectNode updateRequest = objectMapper.createObjectNode();
        ObjectNode info = objectMapper.createObjectNode();
        ObjectNode data = objectMapper.createObjectNode();

        for (Map.Entry<String, Object> entry : newProperties.entrySet()) {
            String fieldName = entry.getKey();
            if (fieldName.equals("id") || fieldName.equals("version")) continue;
            Object fieldValue = entry.getValue();
            String fieldType = getFieldInfoFieldType(collectionData.getFieldInfoList(), fieldName);
            String stringValue = fieldValue.toString();
            if (fieldType.equals("boolean")) {
                data.put(entry.getKey(), Boolean.valueOf(stringValue));
            } else if (fieldType.equals("number")) {
                data.put(entry.getKey(), Double.valueOf(stringValue));
            } else if (fieldType.equals("integer")) {
                data.put(entry.getKey(), Integer.valueOf(stringValue));
            } else {
                data.put(entry.getKey(), stringValue);
            }
        }

        info.put("version", version);
        info.put("id", id);
        info.put("NodeName", Node.getName());
        updateRequest.put("CollectionName", collectionData.getCollectionName());
        updateRequest.put("DatabaseName", collectionData.getDatabaseName());
        updateRequest.put("info", info);
        updateRequest.put("data", data);
        request.put("updateRequest", updateRequest);

        User user = new User(UserInfo.getUsername(), UserInfo.getPassword());
        WriteRequest writeRequest = new WriteRequest(user, request.toString(), "document/update");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> httpEntity = new HttpEntity<>(writeRequest, headers);
        String url = "http://localhost:9000/load-balance/write";

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
            logger.info("The document with id '" + id + "' has been updated successfully!");

        } catch (HttpClientErrorException e) {
            logger.error(e.getResponseBodyAsString());
         //   throw new DocumentUpdateException(e.getResponseBodyAsString());
        } catch(HttpServerErrorException e) {
            logger.error("There's an issue within the cluster, please try connecting later!");
       //     throw new ClusterOperationalIssueException();
        }
    }

    private String getFieldInfoFieldType(List<FieldInfo> fieldInfoList, String fieldName) {
        for (FieldInfo fieldInfo : fieldInfoList) {
            if (fieldName.equals(fieldInfo.getFieldName())) {
                return fieldInfo.getFieldType();
            }
        }
        return null;
    }
    public void updateDocumentById(String id, CollectionData collectionData) {
        String collectionName = collectionData.getCollectionName();

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        ObjectNode documentNode = objectMapper.createObjectNode();
        ObjectNode idNode = objectMapper.createObjectNode();
        idNode.put("id", id);
        documentNode.put("CollectionName", collectionName);
        documentNode.put("DatabaseName", collectionData.getDatabaseName());
        documentNode.put("data", idNode);
        objectNode.put("documentNode", documentNode);

        User user = new User(UserInfo.getUsername(), UserInfo.getPassword());
        WriteRequest writeRequest = new WriteRequest(user, objectNode.toString(), "document/delete-id");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> httpEntity = new HttpEntity<>(writeRequest, headers);
        String url = "http://localhost:9000/load-balance/write";

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
            logger.info("The document with id '" + id + "' has been successfully deleted");
        } catch (HttpClientErrorException e) {
            logger.error(e.getResponseBodyAsString());
         //   throw new DocumentDeletionException(id);
        } catch(HttpServerErrorException e) {
            logger.error("There's an issue within the cluster, please try connecting later!");
           // throw new ClusterOperationalIssueException();
        }
    }
}
