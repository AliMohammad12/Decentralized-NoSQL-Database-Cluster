package bank.app.service;

import atypon.cluster.client.exception.DocumentReadingException;
import atypon.cluster.client.request.Property;
import atypon.cluster.client.service.ClusterDocumentService;
import bank.app.model.Account;
import bank.app.model.Customer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {
    private final ClusterDocumentService clusterDocumentService;
    @Autowired
    public CustomerService(ClusterDocumentService clusterDocumentService) {
        this.clusterDocumentService = clusterDocumentService;
    }
    public String createCustomer(Customer customer) throws JsonProcessingException {
        return clusterDocumentService.createDocument(Customer.class, customer);
    }
    public Customer getCustomerByAccountId(String accountId) {
        Property property = new Property("accountId", accountId);
        ArrayNode result = clusterDocumentService.readDocumentByProperty(Customer.class, property);
        if (result.isEmpty()) return null;
        JsonNode jsonNode = result.get(0);
        String username = jsonNode.get("username").asText();
        String accountType = jsonNode.get("accountType").asText();
        double balance = jsonNode.get("balance").asDouble();
        int age = jsonNode.get("age").asInt();
        String id = jsonNode.get("id").asText();
        return new Customer(id, accountId, username, accountType, balance, age);
    }
    public Customer getCustomerById(String id)  {
        JsonNode result;
        try {
            result = clusterDocumentService.readDocumentById(Customer.class, id);
        } catch (DocumentReadingException e) {
            return null;
        }
        String username = result.get("username").asText();
        String accountType = result.get("accountType").asText();
        double balance = result.get("balance").asDouble();
        int age = result.get("age").asInt();
        String accountId = result.get("accountId").asText();
        return new Customer(id, accountId, username, accountType, balance, age);
    }
    public void updateBalance(String id, double newBalance) throws DocumentReadingException {
        clusterDocumentService.updateDocument(Customer.class, id,
                new Property("balance", newBalance));
    }
}
