package atypon.app.node.service.Impl;

import atypon.app.node.indexing.IndexObject;
import atypon.app.node.indexing.Property;
import atypon.app.node.indexing.bplustree.BPlusTree;
import atypon.app.node.model.Node;
import atypon.app.node.model.User;
import atypon.app.node.request.document.DocumentRequestByProperty;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.service.services.ValidatorService;
import atypon.app.node.utility.DiskOperations;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Service
public class ValidatorServiceImpl implements ValidatorService {
    private static final Logger logger = LoggerFactory.getLogger(ValidatorService.class);
    private final HashMap<IndexObject, BPlusTree> indexRegistry;
    @Autowired
    public ValidatorServiceImpl(@Qualifier("indexRegistry") HashMap<IndexObject, BPlusTree> indexRegistry) {
        this.indexRegistry = indexRegistry;
    }
    private static Path getPath() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        Path path = Path.of("Storage", Node.getName(), "Users", user.getUsername(), "Databases");
        return path;
    }
    @Override
    public ValidatorResponse isDatabaseExists(String databaseName) {
        logger.info("Checking if database '" + databaseName + "' exists!");
        Path path = getPath().resolve(databaseName);
        ValidatorResponse validatorResponse = new ValidatorResponse(DiskOperations.isDirectoryExists(path.toString()));
        if (validatorResponse.isValid()) {
            validatorResponse.setMessage("Database with the name '" + databaseName + "' exists!");
        } else {
            validatorResponse.setMessage("Database with the name '" + databaseName + "' does not exist!");
        }
        logger.info(validatorResponse.getMessage());
        return validatorResponse;
    }
    @Override
    public ValidatorResponse isCollectionExists(String databaseName, String collectionName) {
        ValidatorResponse validateDatabase = isDatabaseExists(databaseName);
        if (!validateDatabase.isValid()) {
            validateDatabase.setMessage("Database with the name '" + databaseName + "' doesn't exist!");
            logger.info(validateDatabase.getMessage());
            return validateDatabase;
        }
        logger.info("Checking if collection '" + collectionName + "' exists inside '" + databaseName + "' !");
        Path path = getPath().resolve(databaseName).resolve("Collections").resolve(collectionName);
        ValidatorResponse validatorResponse = new ValidatorResponse(DiskOperations.isDirectoryExists(path.toString()));
        if (validatorResponse.isValid()) {
            validatorResponse.setMessage("The collection '" + collectionName + "' exists within '" + databaseName + "' database!");
        } else {
            validatorResponse.setMessage("The collection '" + collectionName + "' does not exist within '" + databaseName + "' database!");
        }
        logger.info(validatorResponse.getMessage());
        return validatorResponse;
    }
    @Override
    public ValidatorResponse isDocumentValid(String database, String collection, JsonNode targetDocument) {
        logger.info("Checking if the document is valid:\n" + targetDocument.toPrettyString());

        // let json service do some action here
        Path path = getPath().resolve(database).resolve("Collections").resolve(collection);
        ObjectMapper objectMapper = new ObjectMapper();

        File collectionsDir = new File(path.toString());
        File schemaFile = new File(collectionsDir, "schema.json");

        JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)).objectMapper(objectMapper).build();
        JsonSchema schema = schemaFactory.getSchema(new File(schemaFile.getAbsolutePath()).toURI());

        Set<ValidationMessage> validationResult = schema.validate(targetDocument);
        ValidatorResponse validatorResponse = new ValidatorResponse(validationResult.size() == 0);
        String validatorMessage = "";
        if (validatorResponse.isValid()) {
            validatorMessage += "Document is valid!";
        } else {
            validatorMessage += "Document data is not valid. Validation errors:\n" ;
            for (ValidationMessage result : validationResult) {
                validatorMessage += result.getMessage() + '\n';
            }
        }
        validatorResponse.setMessage(validatorMessage);
        logger.info(validatorResponse.getMessage());
        return validatorResponse;
    }
    @Override
    public ValidatorResponse isDocumentExists(String database, String collection, JsonNode document) {
        ValidatorResponse validateCollection = isCollectionExists(database, collection);
        if (!validateCollection.isValid()) {
            return validateCollection;
        }
        logger.info("Checking if the document exists " +
                "in database '" + database +"' in collection '" + collection + "' :" + document.toPrettyString());
        if (document.get("id") == null) {
            logger.info("Invalid Document, Please send the Id!");
            return new ValidatorResponse("Invalid Document, Please send the Id!", false);
        }
        String id = document.get("id").asText();
        Path path = getPath().resolve(database).resolve("Collections").resolve(collection).resolve("Documents").resolve(id + ".json");
        ValidatorResponse validatorResponse = new ValidatorResponse(DiskOperations.isFileExists(path.toString()));
        if (validatorResponse.isValid()) {
            validatorResponse.setMessage("The requested document within '" + collection + "' collection exists !");
        } else {
            validatorResponse.setMessage("The requested document within '" + collection + "' collection doesn't exist !");
        }
        logger.info(validatorResponse.getMessage());
        return validatorResponse;
    }
    @Override
    public ValidatorResponse isDocumentUpdateRequestValid(String database, String collection, JsonNode documentData, JsonNode documentInfo) {
        ValidatorResponse validatorResponse = isDocumentExists(database, collection, documentInfo);
        if (!validatorResponse.isValid()) {
            return validatorResponse;
        }
        logger.info("Validating document update request!");
        if (documentInfo.get("version") == null) {
            logger.info("Update request invalid, Please include the 'version' in the document info");
            return new ValidatorResponse("Update request invalid, Please include the 'version' in the document info", false);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        Path path = getPath().resolve(database).resolve("Collections").resolve(collection);

        File collectionsDir = new File(path.toString());
        File schemaFile = new File(collectionsDir, "schema.json");

        JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder(JsonSchemaFactory.
                getInstance(SpecVersion.VersionFlag.V201909)).objectMapper(objectMapper).build();
        JsonSchema schema = schemaFactory.getSchema(new File(schemaFile.getAbsolutePath()).toURI());

        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = documentData.fields();
        while (fieldsIterator.hasNext()) {
            Map.Entry<String, JsonNode> field = fieldsIterator.next();
            String fieldName = field.getKey();
            JsonNode fieldValue = field.getValue();

            JsonNode jsonNode = schema.getSchemaNode().get("properties").get(fieldName);
            if (jsonNode == null) {
                logger.info("The requested property '" + fieldName + "' doesn't exist in the collection!");
                return new ValidatorResponse("The requested property '" + fieldName + "' doesn't exist in the collection!", false);
            }
            String type = jsonNode.get("type").asText();
            boolean valid = false;
            if (type.equals("string") && fieldValue.isTextual()) {
                valid = true;
            } else if (type.equals("integer") && fieldValue.isInt()) {
                valid = true;
            } else if (type.equals("number") && fieldValue.isDouble()) {
                valid = true;
            } else if (type.equals("boolean") && fieldValue.isBoolean()) {
                valid = true;
            } else {
                logger.info("The type of the property '{"+ fieldName + ", " + fieldValue + "}' doesn't match the schema type '" + type + "'!");
                return new ValidatorResponse("The type of the property '{"+ fieldName + ", " + fieldValue + "}' doesn't match the schema type '" + type + "'!", false);
            }
        }
        logger.info("Update request valid!");
        return new ValidatorResponse("Update request valid!", true);
    }
    @Override
    public ValidatorResponse isUsernameExists(String username) throws IOException {
        logger.info("Checking if username '" + username +"' exists!");

        File jsonFile = new File("Storage/" + Node.getName() + "/Users.json");
        ObjectMapper objectMapper = new ObjectMapper();
        User[] users = objectMapper.readValue(jsonFile, User[].class);
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return new ValidatorResponse("User with the name '" + username + "' exists!", true);
            }
        }
        return new ValidatorResponse("User with the name " + username + " doesn't exist!", false);
    }
    @Override
    public ValidatorResponse isIndexCreationAllowed(IndexObject indexObject) {
        String database = indexObject.getDatabase();
        String collection = indexObject.getCollection();
        ValidatorResponse validatorResponse = isCollectionExists(database, collection);
        if (!validatorResponse.isValid()) {
            return validatorResponse;
        }
        logger.info("Validating index creation request!");

        ObjectMapper objectMapper = new ObjectMapper();
        Path path = getPath().resolve(database).resolve("Collections").resolve(collection);

        File collectionsDir = new File(path.toString());
        File schemaFile = new File(collectionsDir, "schema.json");

        JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)).objectMapper(objectMapper).build();
        JsonSchema schema = schemaFactory.getSchema(new File(schemaFile.getAbsolutePath()).toURI());

        String property = indexObject.getProperty();

        if (schema.getSchemaNode().get("properties").get(property) == null) {
            return new ValidatorResponse("The requested property doesn't exist in the collection", false);
        }
        if (indexRegistry.containsKey(indexObject)) {
            return new ValidatorResponse("Index for property '" + property + "' already exists!", false);
        }
        return new ValidatorResponse("Index creation for property '" + property + "' is valid!" , true);
    }

    @Override
    public ValidatorResponse IsIndexDeletionAllowed(IndexObject indexObject) {
        logger.info("Validating index deletion request!");

        String property = indexObject.getProperty();
        if (indexRegistry.containsKey(indexObject)) {
            return new ValidatorResponse("Index deletion for property '" + property+ "' is valid!", true);
        }
        return new ValidatorResponse("Index deletion for property '" + property + "' is invalid!", false);
    }

    @Override
    public ValidatorResponse isDocumentRequestValid(DocumentRequestByProperty documentRequestByProperty) {
        String database = documentRequestByProperty.getDatabase();
        String collection = documentRequestByProperty.getCollection();

        ValidatorResponse validatorResponse = isCollectionExists(database, collection);
        if (!validatorResponse.isValid()) {
            return validatorResponse;
        }
        logger.info("Validating document request!");


        ObjectMapper objectMapper = new ObjectMapper();
        Path path = getPath().resolve(database).resolve("Collections").resolve(collection);

        File collectionsDir = new File(path.toString());
        File schemaFile = new File(collectionsDir, "schema.json");

        JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder(JsonSchemaFactory.
                getInstance(SpecVersion.VersionFlag.V201909)).objectMapper(objectMapper).build();
        JsonSchema schema = schemaFactory.getSchema(new File(schemaFile.getAbsolutePath()).toURI());

        Property property = documentRequestByProperty.getProperty();

        JsonNode jsonNode = schema.getSchemaNode().get("properties").get(property.getName());
        if (jsonNode == null) {
            return new ValidatorResponse("The requested property '" + property + "' doesn't exist in the collection!", false);
        }
        String type = jsonNode.get("type").asText();

        boolean valid = false;
        if (type.equals("string") && property.isStringValue()) {
            valid = true;
        } else if (type.equals("integer") && property.isIntegerValue()) {
            valid = true;
        } else if (type.equals("number") && property.isDoubleValue()) {
            valid = true;
        } else if (type.equals("boolean") && property.isBooleanValue()) {
            valid = true;
        } else {
            return new ValidatorResponse("The type of the property '"+ property +"'doesn't match the schema type '" + type + "'!", false);
        }
        return new ValidatorResponse("The document request is valid!", true);
    }
}
