package com.telcreat.aio.model;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder


public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String alias;
    private String name;
    private String lastName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDay;

    private String email;
    private String password;

    @OneToOne
    private Picture picture;

    private LocalDateTime registrationDateTime = LocalDateTime.now();

    private String addressStreet;
    private String addressNumber;
    private String addressFlat;
    private String addressDoor;
    private String addressCountry;
    private int postCode;
    private String addressCity;
    private String addressRegion;

    @ManyToMany
    private List<Shop> favouriteShops;

    private UserRole userRole = UserRole.CLIENT;

    public enum UserRole{
        CLIENT,
        OWNER,
        ADMIN
    };

    private boolean locked = false;

    private boolean enabled = true;



    ////////////////////////////

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        final SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(userRole.name());
        return Collections.singletonList(simpleGrantedAuthority);
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public enum Status{
        ACTIVE,
        INACTIVE
    }


    public User(String alias, String name, String lastName, LocalDate birthDay, String email, String password, Picture picture, String addressStreet, String addressNumber, String addressFlat, String addressDoor, String addressCountry, int postCode, String addressCity, String addressRegion, List<Shop> favouriteShops, UserRole userRole) {
        this.alias = alias;
        this.name = name;
        this.lastName = lastName;
        this.birthDay = birthDay;
        this.email = email;
        this.password = password;
        this.picture = picture;
        this.addressStreet = addressStreet;
        this.addressNumber = addressNumber;
        this.addressFlat = addressFlat;
        this.addressDoor = addressDoor;
        this.addressCountry = addressCountry;
        this.postCode = postCode;
        this.addressCity = addressCity;
        this.addressRegion = addressRegion;
        this.favouriteShops = favouriteShops;
        this.userRole = userRole;
    }
}
