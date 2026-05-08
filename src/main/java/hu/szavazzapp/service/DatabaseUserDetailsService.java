package hu.szavazzapp.service;

import hu.szavazzapp.model.UserAccount;
import hu.szavazzapp.repository.UserAccountRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public DatabaseUserDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount userAccount = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nincs ilyen felhasználó: " + username));

        User.UserBuilder builder = User.withUsername(userAccount.getUsername())
                .password(userAccount.getPasswordHash())
                .disabled(!userAccount.isEnabled());

        if ("ADMIN".equalsIgnoreCase(userAccount.getRole())) {
            return builder.roles("ADMIN", "USER").build();
        }

        return builder.roles("USER").build();
    }
}