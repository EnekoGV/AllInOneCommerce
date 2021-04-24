package com.telcreat.aio.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEditForm {

    private int id;
    private String alias;
    private String name;
    private String lastName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDay;

    private String email;

    private String addressStreet;
    private String addressNumber;
    private String addressFlat;
    private String addressDoor;
    private String addressCountry;
    private int postCode;
    private String addressCity;
    private String addressRegion;

}
