package atypon.app.controller;

import atypon.app.model.WriteRequest;

import atypon.app.service.WriteRequestsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/load-balance")
public class WriteRequestsController {
    private final WriteRequestsService writeRequestsService;
    @Autowired
    public WriteRequestsController(WriteRequestsService writeRequestsService) {
        this.writeRequestsService = writeRequestsService;
    }
    @PostMapping("/write")
    public ResponseEntity<?> handleWriteRequest(@RequestBody WriteRequest writeRequest) {
        return writeRequestsService.sendWriteRequest(
                writeRequest.getRequestData(),
                writeRequest.getEndpoint(),
                writeRequest.getUser());
    }
}
