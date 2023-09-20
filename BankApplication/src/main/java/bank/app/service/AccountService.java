package bank.app.service;

import atypon.cluster.client.exception.DocumentReadingException;
import atypon.cluster.client.request.Property;
import atypon.cluster.client.service.ClusterDocumentService;
import bank.app.model.Account;


import bank.app.model.Role;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final ClusterDocumentService clusterDocumentService;
    @Autowired
    public AccountService(ClusterDocumentService clusterDocumentService) {
        this.clusterDocumentService = clusterDocumentService;
    }
    public String createAccount(Account account) throws JsonProcessingException {
        return clusterDocumentService.createDocument(Account.class, account);
    }
    public Account readAccountById(String id) throws DocumentReadingException {
        JsonNode jsonNode = clusterDocumentService.readDocumentById(Account.class, id);
        if (jsonNode.isEmpty()) return null;
        String password = jsonNode.get("password").asText();
        String username = jsonNode.get("username").asText();
        String role = jsonNode.get("role").asText();
        return new Account(id, username, password, (role.equals("Customer") ? Role.Customer : Role.Admin) );
    }
    public Account readAccountByUsername(String username) {
        Property property = new Property("username", username);
        JsonNode jsonNode = clusterDocumentService.readDocumentByProperty(Account.class, property);
        if (jsonNode.isEmpty()) return null;
        String password = jsonNode.get("password").asText();
        String id = jsonNode.get("id").asText();
        String role = jsonNode.get("role").asText();
        return new Account(id, username, password, (role.equals("Customer") ? Role.Customer : Role.Admin));
    }
}
