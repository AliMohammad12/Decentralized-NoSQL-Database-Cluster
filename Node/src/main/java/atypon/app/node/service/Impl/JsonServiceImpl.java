package atypon.app.node.service.Impl;

import atypon.app.node.service.services.JsonService;
import atypon.app.node.utility.FileOperations;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.*;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
public class JsonServiceImpl implements JsonService {
    @Override
    public JsonNode generateJsonSchema(Class<?> clazz) {
        SchemaGeneratorConfigBuilder configBuilder =
                new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON);
        SchemaGeneratorConfig config = configBuilder.build();

        configBuilder.forTypesInGeneral()
                .withAdditionalPropertiesResolver(scope -> Object.class);
        configBuilder.forFields()
                .withAdditionalPropertiesResolver(field -> field.getType().getErasedType() == Object.class
                        ? null : Void.class);
        configBuilder.forFields().withRequiredCheck(field -> true);
        configBuilder.forMethods()
                .withAdditionalPropertiesResolver(method -> method.getType().getErasedType() == Map.class
                        ? method.getTypeParameterFor(Map.class, 1) : Void.class);

        SchemaGenerator generator = new SchemaGenerator(config);
        JsonNode jsonSchema = generator.generateSchema(clazz);
        return jsonSchema;
    }

    @Override
    public JsonNode generateJsonSchema2(Map<String, Object> properties) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("$schema", "https://json-schema.org/draft/2020-12/schema");
        schema.put("type", "object");
        ObjectNode propertiesNode = objectMapper.createObjectNode();
        schema.set("properties", propertiesNode);
        ArrayNode requiredArray = objectMapper.createArrayNode();
        schema.set("required", requiredArray);
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            ObjectNode propertyNode = objectMapper.createObjectNode();
            String propertyType = entry.getValue().getClass().getSimpleName().toLowerCase();
            propertyNode.put("type", propertyType);
            propertiesNode.set(entry.getKey(), propertyNode);
            requiredArray.add(entry.getKey());
        }
        schema.put("additionalProperties", false);
        return schema;
    }
    @Override
    public String convertJsonToString(JsonNode jsonNode) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);  // Enables pretty-printing
        return objectMapper.writeValueAsString(jsonNode);
    }

    @Override
    public ArrayNode readJsonArray(String path) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode documentsArray = objectMapper.createArrayNode();

        File directory = new File(path);
        File[] documentFiles = directory.listFiles();

        if (documentFiles != null) {
            for (File documentFile : documentFiles) {
                if (documentFile.isFile() && documentFile.getName().endsWith(".json")) {
                    try {
                        JsonNode documentNode = objectMapper.readTree(documentFile);
                        documentsArray.add(documentNode);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return documentsArray;
    }
    @Override
    public JsonNode readJsonNode(String path) throws IOException {
        String jsonContent = FileOperations.readFileAsString(path);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(jsonContent);
    }
}
