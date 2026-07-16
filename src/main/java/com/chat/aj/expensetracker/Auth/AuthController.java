package com.chat.aj.expensetracker.Auth;

import com.chat.aj.expensetracker.Auth.DTOs.LoginRequest;
import com.chat.aj.expensetracker.Auth.DTOs.RegisterRequest;
import com.chat.aj.expensetracker.security.JWT.JWTAuthenticationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest registerRequest){
        authService.Register(registerRequest);
        return ResponseEntity.ok("Success!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest loginRequest){
        JWTAuthenticationResponse jwt = authService.Login(loginRequest);
        return ResponseEntity.ok(jwt.getToken());
    }
}
