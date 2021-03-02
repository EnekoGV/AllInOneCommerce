package com.telcreat.aio.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String alias;
    private String name;
    private String lastName;
    private LocalDate birthDay;
    private String email;
    private String password;

    private String picture; // Client image directory

    private LocalDateTime registrationDateTime;

    private String addressString;
    private String addressNumber;
    private String addressFlat;
    private String addressDoor;
    private String addressCountry;
    private int postCode;
    private String addressTown;
    private String addressRegion;

    private boolean shopOwner;
    @ManyToOne
    private Shop shop;

    @OneToMany
    private List<Shop> favouriteShops;

    @ManyToOne
    private Cart cart;

}
