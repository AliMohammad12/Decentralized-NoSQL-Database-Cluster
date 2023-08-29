package atypon.app.node.service.services;

import atypon.app.node.request.AuthenticationRequest;
import atypon.app.node.response.AuthenticationResponse;

import java.io.IOException;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request) throws IOException;
}
