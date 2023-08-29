package atypon.app.node.service.Impl;

import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.service.services.ValidatorService;
import atypon.app.node.utility.FileOperations;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Set;

@Service
public class ValidatorServiceImpl implements ValidatorService {
    @Override
    public ValidatorResponse isDatabaseExists(String databaseName) {
        ValidatorResponse validatorResponse = new ValidatorResponse(FileOperations.isDirectoryExists("Databases/"+databaseName));
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
            validateDatabase.setMessage("Database with the name " + databaseName + "doesn't exist!");
            return validateDatabase;
        }
        ValidatorResponse validatorResponse = new ValidatorResponse(FileOperations.isDirectoryExists("Databases/"+ databaseName + "/Collections/" + collectionName));
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

        ObjectMapper objectMapper = new ObjectMapper();
        File collectionsDir = new File("Databases/"+ database +"/Collections/" + collection);
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
        ValidatorResponse validatorResponse = new ValidatorResponse(FileOperations.isFileExists("Databases/"+ database + "/Collections/" + collection + "/Documents/"+id+".json"));
        if (validatorResponse.isValid()) {
            validatorResponse.setMessage("The requested document within " + collection + " exists !");
        } else {
            validatorResponse.setMessage("The requested document within " + collection + " doesnt exist !");
        }
        return validatorResponse;
    }

}
