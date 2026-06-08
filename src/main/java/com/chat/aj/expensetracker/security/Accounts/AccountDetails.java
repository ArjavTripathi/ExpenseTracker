package com.chat.aj.expensetracker.security.Accounts;

import com.chat.aj.expensetracker.common.Entities.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;

@Data
@NoArgsConstructor
@SuppressWarnings("NullableProblems")
public class AccountDetails implements UserDetails {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String email;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public AccountDetails(Long id, String username, String password, String email, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.authorities = authorities;
    }

    public static AccountDetails build(User acc){
        return new AccountDetails(
                acc.getId(),
                acc.getName(),
                acc.getPassword(),
                acc.getEmail(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
