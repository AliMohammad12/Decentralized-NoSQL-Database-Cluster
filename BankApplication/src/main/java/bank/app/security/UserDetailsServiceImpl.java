package bank.app.security;

import bank.app.model.Account;
import bank.app.model.AccountDetails;
import bank.app.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private AccountService accountService;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountService.readAccountByUsername(username);
        if (account == null) {
            throw new UsernameNotFoundException("Could not find user");
        }
        return new AccountDetails(account);
    }
}