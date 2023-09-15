package atypon.app.node.service.Impl;

import atypon.app.node.indexing.IndexObject;
import atypon.app.node.model.Node;
import atypon.app.node.model.User;
import atypon.app.node.service.services.JsonService;
import atypon.app.node.utility.DiskOperations;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@Service
public class JsonServiceImpl implements JsonService {
    private static Path getPath() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        Path path = Path.of("Storage", Node.getName(), "Users", user.getUsername(), "Databases");
        return path;
    }
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
            if (propertyType.equals("double") || propertyType.equals("float")) {
                propertyType = "number";
            }
            if (propertyType.equals("long")) {
                propertyType = "integer";
            }
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
    @Override // use disk operations here
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
    @Override // use disk operations here
    public User findByUsername(String username) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        File jsonFile = new File("Storage/" + Node.getName() + "/Users.json");
        if (!jsonFile.exists()) {
            return null;
        }

        User[] users = objectMapper.readValue(jsonFile, User[].class);

        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    @Override // reading from file!
    public String getPropertyTypeFromSchema(IndexObject indexObject) {
        ObjectMapper objectMapper = new ObjectMapper();
        String database = indexObject.getDatabase();
        String collection = indexObject.getCollection();
        Path path = getPath().resolve(database).resolve("Collections").resolve(collection);

        File collectionsDir = new File(path.toString());
        File schemaFile = new File(collectionsDir, "schema.json");

        JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)).objectMapper(objectMapper).build();
        JsonSchema schema = schemaFactory.getSchema(new File(schemaFile.getAbsolutePath()).toURI());

        String property = indexObject.getProperty();
        JsonNode jsonNode = schema.getSchemaNode().get("properties").get(property).get("type");
        return jsonNode.asText();
    }

    @Override
    public JsonNode readJsonNode(String path) throws IOException {
        String jsonContent = DiskOperations.readFile(path);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(jsonContent);
    }
}
