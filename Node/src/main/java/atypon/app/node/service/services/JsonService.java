package atypon.app.node.service.services;

import atypon.app.node.indexing.IndexObject;
import atypon.app.node.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface JsonService {
    JsonNode generateJsonSchema(Map<String, Object> properties);
    String convertJsonToString(JsonNode jsonNode) throws JsonProcessingException;
    ArrayNode readJsonArray(String directory) throws IOException;
    ArrayNode readAsJsonArray(List<String> documentsId, Path path) throws IOException;
    JsonNode readJsonNode(String path) throws IOException;
    User findByUsername(String username) throws IOException;
    String getPropertyTypeFromSchema(IndexObject indexObject);
}