package org.raflab.studsluzba.security;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.repositories.security.UserAccountRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAccountDetailsService implements UserDetailsService {
    private final UserAccountRepository repo;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repo.findByUsername(username)
                .map(CurrentUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user"));
    }
}
