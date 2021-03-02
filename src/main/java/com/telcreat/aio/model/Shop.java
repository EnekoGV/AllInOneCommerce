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

    private String name;
    private String description;
    //add address information(Check on internet)
    private String picture;
    private String backgroundPicture;
    //add billing information(Check)
    @OneToMany
    private List<Category> shopCategories;
    private LocalDateTime registrationDate;

}
