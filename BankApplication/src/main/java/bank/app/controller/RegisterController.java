package bank.app.controller;

import atypon.cluster.client.exception.DocumentReadingException;
import bank.app.model.Account;
import bank.app.model.Customer;
import bank.app.model.Role;
import bank.app.service.AccountService;
import bank.app.service.CustomerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterController {
    private final AccountService accountService;
    private final CustomerService customerService;
    @Autowired
    public RegisterController(AccountService accountService,
                              CustomerService customerService) {
        this.accountService = accountService;
        this.customerService = customerService;
    }

    @GetMapping("/register")
    public String registerPage() {
        return "registration_page";
    }
    @PostMapping("/register")
    public String processRegistration(@RequestParam("username") String username,
                                      @RequestParam("password") String password,
                                      @RequestParam("repeatPassword") String repeatedPassword,
                                      @RequestParam("age") int age,
                                      @RequestParam("accountType") String accountType,
                                      RedirectAttributes redirectAttributes) throws JsonProcessingException {
        if (!password.equals(repeatedPassword)) {
            redirectAttributes.addFlashAttribute("message", "Passwords don't match!");
            return "redirect:/register";
        }
        Account account = accountService.readAccountByUsername(username);
        if (account != null) {
            redirectAttributes.addFlashAttribute("message", "Account with username '" + username + "' already exists");
            return "redirect:/register";
        }
        String accountId = accountService.createAccount(new Account(username, password, "Customer"));
        Customer customer = new Customer(accountId, username, accountType, 0.0, age);
        customerService.createCustomer(customer);
        redirectAttributes.addFlashAttribute("message", "Your account have been successfully created!");
        return "redirect:/login";
    }
}
