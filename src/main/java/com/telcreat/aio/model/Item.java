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

    @Column(columnDefinition = "LONGTEXT")
    private String shortDescription;
    @Column(columnDefinition = "LONGTEXT")
    private String longDescription;
    private Float price;
    private String name;
    private LocalDateTime creationDateTime = LocalDateTime.now();

    public enum Status{
        ACTIVE,
        INACTIVE
    }

    private Status status;

    public Item(Shop shop, Category itemCategory, Picture picture, String shortDescription, String longDescription, Float price, String name, Status status) {
        this.shop = shop;
        this.itemCategory = itemCategory;
        this.picture = picture;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.price = price;
        this.name = name;
        this.status = status;
    }
}
