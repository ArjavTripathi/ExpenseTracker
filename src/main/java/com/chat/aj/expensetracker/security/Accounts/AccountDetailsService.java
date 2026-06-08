package com.chat.aj.expensetracker.security.Accounts;

import com.chat.aj.expensetracker.common.Entities.User;
import com.chat.aj.expensetracker.common.Entities.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@SuppressWarnings("NullableProblems")
@Service
public class AccountDetailsService implements UserDetailsService {
    UserRepository accountsRepository;

    @Autowired
    public AccountDetailsService(UserRepository accountsRepository) {
        this.accountsRepository = accountsRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = accountsRepository.findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + username));
        return AccountDetails.build(user);
    }
}
