package bank.app.model;

import atypon.cluster.client.annotation.CreateCollection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@CreateCollection
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
    private String id;
    private String accountId;
    private String username;
    private String accountType;
    private double salary;
    private int age;

    public Customer(String accountId, String username, String accountType, double salary, int age) {
        this.accountId = accountId;
        this.username = username;
        this.accountType = accountType;
        this.salary = salary;
        this.age = age;
    }
}
