package bank.app.model;

import atypon.cluster.client.annotation.CreateCollection;
import lombok.*;

@CreateCollection
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Account {
    private String id;
    private String username;
    private String password;
    private String role;
    public Account(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
