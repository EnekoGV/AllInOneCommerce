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

    @ManyToOne
    private User owner;

    private String name;
    private String description;

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

    public Shop(List<Category> categories, Picture picture, Picture backgroundPicture, User owner, String name, String description, String addressAddress, String addressPostNumber, String addressCity, String addressCountry, String addressTelNumber, String billingName, String billingSurname, String billingAddress, String billingPostNumber, String billingCity, String billingCountry, String billingTelNumber, String longitude, String latitude, Status status) {
        this.categories = categories;
        this.picture = picture;
        this.backgroundPicture = backgroundPicture;
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.addressAddress = addressAddress;
        this.addressPostNumber = addressPostNumber;
        this.addressCity = addressCity;
        this.addressCountry = addressCountry;
        this.addressTelNumber = addressTelNumber;
        this.billingName = billingName;
        this.billingSurname = billingSurname;
        this.billingAddress = billingAddress;
        this.billingPostNumber = billingPostNumber;
        this.billingCity = billingCity;
        this.billingCountry = billingCountry;
        this.billingTelNumber = billingTelNumber;
        this.longitude = longitude;
        this.latitude = latitude;
        this.status = status;
    }
}
