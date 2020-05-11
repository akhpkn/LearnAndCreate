package com.lac.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lac.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {
    private Long userId;
    private String name;
    private String surname;
    private String username;
    @JsonIgnore
    private String email;
    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long userId, String name, String surname, String username,
                         String email, String password, Collection<? extends  GrantedAuthority> authorities) {

        this.userId = userId;
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream().map(role ->
                new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());
        return new UserPrincipal(
                user.getUserId(),
                user.getName(),
                user.getSurname(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserPrincipal another = (UserPrincipal) obj;
        return Objects.equals(userId, another.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
