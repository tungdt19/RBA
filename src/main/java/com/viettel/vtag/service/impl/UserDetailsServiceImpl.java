package com.viettel.vtag.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final JdbcTemplate jdbc;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        throw new UsernameNotFoundException("User " + userName + " was not found in the database");
        // }
        //
        // var roleNames = appRoleRepository.getRoleNames(user.id());
        //
        // var grantList = new ArrayList<GrantedAuthority>();
        // if (roleNames != null) {
        //     for (var role : roleNames) {
        //         grantList.add(role);
        //     }
        // }
        //
        // UserDetails userDetails = (UserDetails) new User(user.getUserName(),
        //     user.getEncrytedPassword(), grantList);
        //
        // return userDetails;
    }
}
