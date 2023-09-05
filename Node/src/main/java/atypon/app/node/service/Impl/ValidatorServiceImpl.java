package atypon.app.node.service.Impl;

import atypon.app.node.model.Node;
import atypon.app.node.model.User;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.security.MyUserDetails;
import atypon.app.node.service.services.ValidatorService;
import atypon.app.node.utility.FileOperations;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

@Service
public class ValidatorServiceImpl implements ValidatorService {
    private static Path getPath() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        Path path = Path.of("Storage", Node.getName(), "Users", user.getUsername(), "Databases");
        return path;
    }
    @Override
    public ValidatorResponse isDatabaseExists(String databaseName) {
        // todo: fix this
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        Path path = getPath().resolve(databaseName);

        ValidatorResponse validatorResponse = new ValidatorResponse(FileOperations.isDirectoryExists(path.toString()));
        if (validatorResponse.isValid()) {
            validatorResponse.setMessage("Database with the name " + databaseName + " exists!");
        } else {
            validatorResponse.setMessage("Database with the name " + databaseName + " does not exist!");
        }
        return validatorResponse;
    }
    @Override
    public ValidatorResponse isCollectionExists(String databaseName, String collectionName) {
        ValidatorResponse validateDatabase = isDatabaseExists(databaseName);
        if (!validateDatabase.isValid()) {
            validateDatabase.setMessage("Database with the name " + databaseName + " doesn't exist!");
            return validateDatabase;
        }

        // todo: fix this
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        Path path = getPath().resolve(databaseName).resolve("Collections").resolve(collectionName);

        ValidatorResponse validatorResponse = new ValidatorResponse(FileOperations.isDirectoryExists(path.toString()));
        if (validatorResponse.isValid()) {
            validatorResponse.setMessage("The collection " + collectionName + " exists within " + databaseName + " database!");
        } else {
            validatorResponse.setMessage("The collection " + collectionName + " does not exist within " + databaseName + " database!");
        }
        return validatorResponse;
    }
    @Override
    public ValidatorResponse isDocumentValid(String database, String collection, JsonNode targetDocument) {
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
        return validatorResponse;
    }
    @Override
    public ValidatorResponse isDocumentExists(String database, String collection, String id) {
        ValidatorResponse validateCollection = isCollectionExists(database, collection);
        if (!validateCollection.isValid()) {
            return validateCollection;
        }
        Path path = getPath().resolve(database).resolve("Collections").resolve(collection).resolve("Documents").resolve(id + ".json");
        ValidatorResponse validatorResponse = new ValidatorResponse(FileOperations.isFileExists(path.toString()));
        if (validatorResponse.isValid()) {
            validatorResponse.setMessage("The requested document within " + collection + " exists !");
        } else {
            validatorResponse.setMessage("The requested document within " + collection + " doesn't exist !");
        }
        return validatorResponse;
    }
    @Override
    public ValidatorResponse isUsernameExists(String username) throws IOException {
        File jsonFile = new File("Storage/" + Node.getName() + "/Users.json");
        ObjectMapper objectMapper = new ObjectMapper();
        User[] users = objectMapper.readValue(jsonFile, User[].class);
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return new ValidatorResponse("User with the name " + username + " exists!", true);
            }
        }
        return new ValidatorResponse("User with the name " + username + " doesn't exist!", false);
    }
}
