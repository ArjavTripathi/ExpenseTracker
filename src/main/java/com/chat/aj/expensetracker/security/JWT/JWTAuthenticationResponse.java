package com.chat.aj.expensetracker.security.JWT;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JWTAuthenticationResponse {
    private String token;
}
