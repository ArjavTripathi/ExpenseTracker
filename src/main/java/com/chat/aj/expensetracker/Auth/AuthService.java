package com.chat.aj.expensetracker.Auth;

import com.chat.aj.expensetracker.common.DTOs.LoginRequest;
import com.chat.aj.expensetracker.common.DTOs.RegisterRequest;
import com.chat.aj.expensetracker.common.Entities.User;
import com.chat.aj.expensetracker.common.Entities.UserRepository;
import com.chat.aj.expensetracker.common.Exceptions.DuplicateResourceException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class AuthService {
    public UserRepository userRepository;
    private PasswordEncoder passwordEncoder;


    public void Login(LoginRequest login){

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
