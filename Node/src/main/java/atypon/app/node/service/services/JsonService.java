package atypon.app.node.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.Map;

public interface JsonService {
    JsonNode generateJsonSchema(Class<?> clazz);
    JsonNode generateJsonSchema2(Map<String, Object> properties);
    String convertJsonToString(JsonNode jsonNode) throws JsonProcessingException;
    ArrayNode readJsonArray(String directory);
    JsonNode readJsonNode(String path) throws IOException;
}