package com.telcreat.aio.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

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

    @ManyToOne
    private Item item;

    @OneToMany
    private List<Variant> subVariants;


    private int stock; // propiedad del último de nivel de subvariante.

    private enum Status{
        ACTIVE,
        INACTIVE
    }

    private Status status;
}
