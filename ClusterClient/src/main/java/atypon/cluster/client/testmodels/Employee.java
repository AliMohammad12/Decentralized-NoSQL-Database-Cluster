package atypon.cluster.client.testmodels;

import atypon.cluster.client.annotation.CreateCollection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class Employee {
    private double salary;
    private String name;
}
