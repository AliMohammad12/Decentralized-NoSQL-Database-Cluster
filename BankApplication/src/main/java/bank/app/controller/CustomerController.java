package bank.app.controller;

import atypon.cluster.client.exception.DocumentReadingException;
import bank.app.model.Customer;
import bank.app.model.Transaction;
import bank.app.service.CustomerService;
import bank.app.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.Calendar;
import java.util.List;

@Controller
@RequestMapping("/customer")
@SessionAttributes("customer")
public class CustomerController {
    private final CustomerService customerService;
    private final TransactionService transactionService;
    @Autowired
    public CustomerController(CustomerService customerService,
                              TransactionService transactionService) {
        this.customerService = customerService;
        this.transactionService = transactionService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String customerId = (String) session.getAttribute("customerId");
        Customer customer = customerService.getCustomerById(customerId);
        model.addAttribute("customer", customer);

        model.addAttribute("balance", customer.getBalance());
        model.addAttribute("accountType", customer.getAccountType());
        model.addAttribute("username", customer.getUsername());
        return "customer-dashboard";
    }
    @PostMapping("/deposit")
    public String deposit(@RequestParam("depositAmount") double depositAmount,  Model model,
                          RedirectAttributes redirectAttributes) throws DocumentReadingException {
        Customer customer = (Customer) model.getAttribute("customer");
        redirectAttributes.addFlashAttribute("message", "You have successfully deposited '" + depositAmount + "' !");
        customerService.updateBalance(customer.getId(), customer.getBalance() + depositAmount);
        return "redirect:/customer/dashboard";
    }
    @PostMapping("/withdraw")
    public String withdraw(@RequestParam("withdrawAmount") double withdrawAmount, Model model,
                           RedirectAttributes redirectAttributes) throws DocumentReadingException {
        Customer customer = (Customer) model.getAttribute("customer");
        if (customer.getBalance() - withdrawAmount < 0) {
            redirectAttributes.addFlashAttribute("message", "You don't have that much money in your balance to withdraw!");
        } else {
            customerService.updateBalance(customer.getId(), customer.getBalance() - withdrawAmount);
            redirectAttributes.addFlashAttribute("message", "You have successfully withdrawn '" + withdrawAmount + "' !");
        }
        return "redirect:/customer/dashboard";
    }
    @GetMapping("/transaction")
    public String showTransactionPage(HttpSession session, Model model) {
        String customerId = (String) session.getAttribute("customerId");
        Customer customer = (Customer) session.getAttribute("customer");
        String username = customer.getUsername();
        List<Transaction> transactionList = transactionService.getAllTransactionsOf(customerId);
        model.addAttribute("customer", customer);
        model.addAttribute("customerId", customerId);
        model.addAttribute("transactionHistory", transactionList);
        model.addAttribute("username", username);
        model.addAttribute("balance", customer.getBalance());
        model.addAttribute("accountType", customer.getAccountType());
        return "transactions-page";
    }
    @PostMapping("/transaction")
    public String makeTransaction(@RequestParam("receiverId") String receiverId,
                                  @RequestParam("transactionAmount") double transactionAmount,
                                  HttpSession session, Model model,
                                  RedirectAttributes redirectAttributes) throws DocumentReadingException, JsonProcessingException {
        String customerId = (String) session.getAttribute("customerId");
        Customer customer = (Customer) session.getAttribute("customer");

        if (customer.getBalance() - transactionAmount < 0) {
            redirectAttributes.addFlashAttribute("message", "Transaction Failed!" +
                    " You don't have enough money in your balance!");
            return "redirect:/customer/transaction";
        }
        Customer receiver = customerService.getCustomerById(receiverId);
        if (receiver == null) {
            redirectAttributes.addFlashAttribute("message", "Transaction Failed! " +
                    "No such user with id '" + receiverId + "' exists! ");
            return"redirect:/customer/transaction";
        }

        Transaction transaction = new Transaction(receiverId, customerId, transactionAmount, getDate());
        transactionService.createTransaction(transaction, customer, receiver);

        model.addAttribute("customer", customer);
        model.addAttribute("customerId", customerId);
        return "redirect:/customer/transaction";
    }

    private String getDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String minute = String.valueOf(calendar.get(Calendar.MINUTE));
        if (hour.length() == 1) {
            hour = "0" + hour;
        }
        if (minute.length() == 1) {
            minute = "0" + minute;
        }
        return year + "/" + month + "/" + day + " - " + hour + "/" + minute;
    }
}