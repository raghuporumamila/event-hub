package com.eventhub.site.service;

import com.eventhub.model.User;
import com.eventhub.site.config.ApiEndPointUri;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ApiEndPointUri apiEndPointUri;

    public UserDetails getUserDetails(String email) {
        User siteUser = null;
        siteUser = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "/users?email=" + email, HttpMethod.GET, null,
                new ParameterizedTypeReference<User>() {
                }).getBody();

         return org.springframework.security.core.userdetails.User
                .withUsername(siteUser.getEmail())
                .password(siteUser.getPassword())
                .authorities(siteUser.getRole().getName()) // This handles Authorization later
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getUserDetails(username);
    }
}
