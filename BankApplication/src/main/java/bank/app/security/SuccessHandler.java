package bank.app.security;

import bank.app.model.Account;
import bank.app.model.AccountDetails;
import bank.app.model.Customer;
import bank.app.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

public class SuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private CustomerService customerService;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        AccountDetails accountDetails = (AccountDetails) authentication.getPrincipal();
        Account account = accountDetails.getAccount();
        Customer customer = customerService.getCustomerByAccountId(account.getId());

        HttpSession session = request.getSession();
        session.setAttribute("customerId", customer.getId());
        response.sendRedirect("/customer/dashboard");
    }
}