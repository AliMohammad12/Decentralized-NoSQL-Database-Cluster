package bank.app.service;

import atypon.cluster.client.service.ClusterDocumentService;
import bank.app.model.Customer;
import com.fasterxml.jackson.core.JsonProcessingException;
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
}
