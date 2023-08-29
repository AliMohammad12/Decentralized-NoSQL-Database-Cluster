package atypon.app.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientController {

    @Autowired
    private ClientService clientService;

    @GetMapping("/create-collection")
    public ResponseEntity<APIResponse> createCollection() throws IllegalAccessException {
        ResponseEntity<APIResponse> response = clientService.callCreateCollectionEndpoint();
        System.out.println("Response = " + response.getStatusCode());
        return response;
    }

    @GetMapping("/create-document")
    public ResponseEntity<APIResponse> createDocument()  {
        ResponseEntity<APIResponse> response = clientService.callCreateDocumentEndpoint();
        System.out.println("Response = " + response.getBody().getMessage());
        return response;
    }

    @GetMapping("/create-database")
    public ResponseEntity<APIResponse> createDatabase()  {
        ResponseEntity<APIResponse> response = clientService.callCreateDatabaseEndpoint();
        System.out.println("Response = " + response.getBody().getMessage());
        return response;
    }
}