package com.telcreat.aio.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchForm {

    private String search;
    private int categoryId;
    private int orderCriteriaId;
    private int orderDirection;
}
