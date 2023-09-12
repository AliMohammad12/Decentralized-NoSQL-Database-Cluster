package atypon.app.node.service.Impl;

import atypon.app.node.model.Node;
import atypon.app.node.model.User;
import atypon.app.node.service.services.UserService;
import atypon.app.node.service.services.ValidatorService;
import atypon.app.node.utility.FileOperations;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static Path getPath() {
        Path path = Path.of("Storage", Node.getName());
        return path;
    }
    @Override
    public void addUser(String username, String password) throws IOException {
        logger.info("Registering user '" + username + "' in the cluster!");
        // split this into Reading an array
        // and writing an array to file

        File jsonFile = new File(getPath().resolve("Users.json").toString());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, User.class);

        List<User> usersList;

        if (jsonFile.exists() && jsonFile.length() > 0) {
            usersList = objectMapper.readValue(jsonFile, listType);
        } else {
            usersList = new ArrayList<>();
        }
        usersList.add(new User(username, password));
        objectMapper.writeValue(jsonFile, usersList);

        FileOperations.createDirectory(getPath().resolve("Users").toString(), username);
        FileOperations.createDirectory(getPath().resolve("Users").resolve(username).toString(), "Databases");
        FileOperations.writeJsonAtLocation("[]", getPath().
                resolve("Users").
                resolve(username).
                resolve("Databases").
                toString(), "Indexing.json");
    }
}
