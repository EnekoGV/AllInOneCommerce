package com.telcreat.aio.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.graalvm.compiler.replacements.amd64.AMD64StringUTF16CompressNode;

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

    @OneToMany
    private List<Category> shopCategories;

    @OneToOne
    private Pictures shopPicture;

    private String name;
    private String description;
    //add address information(Check on internet)
    //add billing information(Check)
    private LocalDateTime registrationDate;

}
