package bank.app.model;

import atypon.cluster.client.annotation.CreateCollection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@CreateCollection
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private String id;
    private String receiverId;
    private String senderId;
    private double amount;
    private String date;
    public Transaction(String receiverId, String senderId, double amount, String date) {
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.amount = amount;
        this.date = date;
    }
}