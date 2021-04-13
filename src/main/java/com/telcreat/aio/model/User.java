package com.telcreat.aio.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

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

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDay;
    private String email;
    private String password;

    @OneToOne
    private Picture picture;

    private LocalDateTime registrationDateTime = LocalDateTime.now();

    private String addressString;
    private String addressNumber;
    private String addressFlat;
    private String addressDoor;
    private String addressCountry;
    private int postCode;
    private String addressCity;
    private String addressRegion;

    private boolean shopOwner;

    @ManyToMany
    private List<Shop> favouriteShops;

    public enum Status{
        ACTIVE,
        INACTIVE
    }

    private Status status;

}
