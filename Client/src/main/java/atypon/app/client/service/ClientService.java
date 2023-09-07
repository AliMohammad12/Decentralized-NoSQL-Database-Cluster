package atypon.app.client.service;

import atypon.app.client.response.APIResponse;
import atypon.app.client.schema.CollectionSchema;
import atypon.app.client.model.Employee;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Service
public class ClientService {
    public void connect(String username, String password) {
        // if not connected throw custom exception

    }
}