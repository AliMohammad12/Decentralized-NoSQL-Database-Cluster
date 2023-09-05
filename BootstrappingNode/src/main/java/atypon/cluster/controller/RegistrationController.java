package atypon.cluster.controller;

import atypon.cluster.model.User;
import atypon.cluster.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {
    private final RegistrationService registrationService;
    @Autowired
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }
    @PostMapping("/register")
    public String register(@RequestParam("username") String username,
                                      @RequestParam("password") String password,
                                      Model model) {
        ResponseEntity<?> responseEntity = registrationService.registerUser(new User(username, password));
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            model.addAttribute("errorMessage", responseEntity.getBody());
            return "register";
        }
        model.addAttribute("successMessage", "Your account has been registered successfully!");
        return "register";
    }
    @GetMapping("/register")
    public String register() {
        return "register";
    }
}
