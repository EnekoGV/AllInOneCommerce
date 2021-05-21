package com.telcreat.aio.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactForm {

    private String name;
    private String lastName;
    private String email;
    private String subject;
    private String message;
}
