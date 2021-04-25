package com.telcreat.aio.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ShopEditForm {
    private int id;
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

}
