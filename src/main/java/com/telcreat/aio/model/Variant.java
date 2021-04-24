package com.telcreat.aio.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Variant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;

    private int stock; // Propiedad de la subvariante

    @OneToOne
    private Picture picture;

    public enum Status{
        ACTIVE,
        INACTIVE
    }

    @ManyToOne
    private Item item;

    private Status status;
}
