package atypon.app.node.service.Impl;

import atypon.app.node.model.Node;
import atypon.app.node.model.User;
import atypon.app.node.service.services.UserService;
import atypon.app.node.utility.FileOperations;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public void addUser(String username, String password) throws IOException {
        // split this into Reading an array
        // and writing an array to file

        File jsonFile = new File("Storage/"+ Node.getName()+"/Users.json");
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

        FileOperations.createDirectory("Storage/"+ Node.getName() + "/Users", username);
        FileOperations.createDirectory("Storage/"+ Node.getName() + "/Users/"+username, "Databases");
    }
}
