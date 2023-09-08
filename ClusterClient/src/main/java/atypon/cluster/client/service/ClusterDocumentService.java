package atypon.cluster.client.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ClusterDocumentService {
    private static final Logger logger = LoggerFactory.getLogger(ClusterConnectionService.class);
    private final RestTemplate restTemplate;
    @Autowired
    public ClusterDocumentService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    public String createDocument() { // returns id
        return "";
    }
    public void deleteDocumentById(String id) {

    }
    public void deleteDocumentBy(Object property) {

    }
    public void readDocumentById(String id) {

    }
    public void readDocumentBy(Object property) {

    }
    public void updateDocumentProperty(String id, Object property, Object propertyNewValue) {

    }
}
