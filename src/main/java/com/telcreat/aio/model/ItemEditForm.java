package com.telcreat.aio.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemEditForm {

    private int id;
    private String shortDescription;
    private String longDescription;
    private Float price;
    private String name;
    private Category itemCategory;
}
