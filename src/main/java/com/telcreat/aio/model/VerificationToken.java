package com.telcreat.aio.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerificationToken {

    @Id
    private String token;

    private String code;

    @OneToOne
    private User user;
}
