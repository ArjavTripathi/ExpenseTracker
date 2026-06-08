package com.chat.aj.expensetracker.Auth;

import com.chat.aj.expensetracker.Auth.DTOs.LoginRequest;
import com.chat.aj.expensetracker.Auth.DTOs.RegisterRequest;
import com.chat.aj.expensetracker.common.Entities.User;
import com.chat.aj.expensetracker.common.Entities.UserRepository;
import com.chat.aj.expensetracker.common.Exceptions.DuplicateResourceException;
import com.chat.aj.expensetracker.common.Exceptions.ResourceNotFoundException;
import com.chat.aj.expensetracker.security.Accounts.AccountDetails;
import com.chat.aj.expensetracker.security.JWT.JWTAuthenticationResponse;
import com.chat.aj.expensetracker.security.JWT.JWTService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class AuthService {
    private final JWTService jWTService;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;

    public User findUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
    }


    public JWTAuthenticationResponse Login(LoginRequest login){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        AccountDetails userDetails = (AccountDetails) authentication.getPrincipal();
        String jwt = jWTService.generateToken(userDetails);
        return new JWTAuthenticationResponse(jwt);

    }

    public void Register(RegisterRequest register){
        if(userRepository.findByEmail(register.getEmail()).isPresent()){
            throw new DuplicateResourceException("Email already exists");
        }

        User user = new User();

        user.setName(register.getName());
        user.setEmail(register.getEmail());
        user.setPassword(passwordEncoder.encode(register.getPassword()));
        user.setCreated_at(LocalDateTime.now());
        user.setVerified(true);
        userRepository.save(user);
    }
}
