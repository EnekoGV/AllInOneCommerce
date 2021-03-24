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

    @OneToMany
    private List<Variant> variants; //Data-Base redundancy?? ManytoOne item in variant

    @ManyToOne
    private Category itemCategory;

    //private List<String> pictures; HAY QUE PENSAR ALTERNATIVA INT?????

    private String shortDescription;
    private String longDescription;
    private Float price;
    private String name;
    private LocalDateTime creationDate;

}
