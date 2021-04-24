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
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    private Shop shop;

    @ManyToOne
    private Category itemCategory;

    @OneToOne
    private Picture picture;

    private String shortDescription;
    private String longDescription;
    private Float price;
    private String name;
    private LocalDateTime creationDateTime = LocalDateTime.now();

    public enum Status{
        ACTIVE,
        INACTIVE
    }

    private Status status;
}
