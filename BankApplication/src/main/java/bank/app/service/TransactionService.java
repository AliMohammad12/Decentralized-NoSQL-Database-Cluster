package bank.app.service;

import atypon.cluster.client.exception.DocumentReadingException;
import atypon.cluster.client.request.Property;
import atypon.cluster.client.service.ClusterDocumentService;
import bank.app.model.Customer;
import bank.app.model.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {
    private final ClusterDocumentService clusterDocumentService;
    private final CustomerService customerService;
    @Autowired
    public TransactionService(ClusterDocumentService clusterDocumentService,
                              CustomerService customerService) {
        this.clusterDocumentService = clusterDocumentService;
        this.customerService = customerService;
    }
    public String createTransaction(Transaction transaction, Customer sender, Customer receiver) throws JsonProcessingException, DocumentReadingException {
        customerService.updateBalance(receiver.getId(), transaction.getAmount() + receiver.getBalance());
        customerService.updateBalance(sender.getId(), sender.getBalance() - transaction.getAmount());
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        return clusterDocumentService.createDocument(Transaction.class, transaction);
    }
    public List<Transaction> getAllTransactionsOf(String senderId) {
        ArrayNode arrayNode = clusterDocumentService.readDocumentByProperty(Transaction.class, new Property("senderId", senderId));
        List<Transaction> transactionList = new ArrayList<>();
        for (JsonNode jsonNode : arrayNode) {
            String id = jsonNode.get("id").asText();
            String receiverId = jsonNode.get("receiverId").asText();
            String date = jsonNode.get("date").asText();
            double amount = jsonNode.get("amount").asDouble();
            transactionList.add(new Transaction(id, receiverId, senderId, amount, date));
        }
        return transactionList;
    }
}
