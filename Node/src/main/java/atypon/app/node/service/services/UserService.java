package atypon.app.node.service.services;

import java.io.IOException;

public interface UserService {
    void addUser(String username, String password) throws IOException;
}
