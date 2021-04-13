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
public class  ShopOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    private Shop shop;

    @ManyToOne
    private User user;

    @ManyToMany
    private List<Item> items;

    private LocalDateTime orderingDateTime = LocalDateTime.now();
    private float price;

    private enum ShopOrderStatus{
        DELIVERED,
        ON_THE_WAY,
        PREPARING,
        READY_TO_DELIVER,
        CANCELLED,
        ACCEPTED,
        PENDING
    }

    private ShopOrderStatus shopOrderStatus;

}
