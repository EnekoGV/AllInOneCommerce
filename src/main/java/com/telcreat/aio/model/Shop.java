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

    @OneToMany
    private List<Category> categories;

    @ManyToOne
    private Picture picture;

    @ManyToOne
    private Picture backgroundPicture;

    @ManyToOne
    private User owner;

    private String name;
    private String description;

    private String adressName;
    private String adressSurname;
    private String adressAddress; //calle + portal + piso
    private String adressPostNumber;
    private String adressCity;
    private String adressCountry;
    private String addressTelNumber;

    private String billingName;
    private String billingSurname;
    private String billingAddress; //calle + portal + piso
    private String billingPostNumber;
    private String billingCity;
    private String billingCountry;
    private String billingTelNumber;

    private LocalDateTime registrationDate;

}
