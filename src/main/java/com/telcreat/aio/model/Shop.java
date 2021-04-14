package com.telcreat.aio.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToMany
    private List<Category> categories;

    @OneToOne
    private Picture picture;

    @OneToOne
    private Picture backgroundPicture;

    @OneToOne
    private User owner;

    private String name;
    private String description;

    private String addressName;
    private String addressSurname;
    private String addressAddress; //calle + portal + piso
    private String addressPostNumber;
    private String addressCity;
    private String addressCountry;
    private String addressTelNumber;

    private String billingName;
    private String billingSurname;
    private String billingAddress; //calle + portal + piso
    private String billingPostNumber;
    private String billingCity;
    private String billingCountry;
    private String billingTelNumber;

    private String longitude;
    private String latitude;

    private LocalDateTime registrationDateTime = LocalDateTime.now();

    public enum Status{
        ACTIVE,
        INACTIVE
    }

    private Status status;
}
