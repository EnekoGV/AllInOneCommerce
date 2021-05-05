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
    private List<Variant> variants;

    private LocalDateTime orderingDateTime = LocalDateTime.now();
    private float price;

    public enum ShopOrderStatus{
        ENTREGATUTA,
        BIDEAN,
        PRESTATZEN,
        PRESTATUTA,
        EZEZTATUTA,
        BAIEZTATUTA,
        BAIEZTATZEARREN
    }

    private ShopOrderStatus shopOrderStatus;

    public ShopOrder(Shop shop, User user, List<Variant> variants, float price, ShopOrderStatus shopOrderStatus) {
        this.shop = shop;
        this.user = user;
        this.variants = variants;
        this.price = price;
        this.shopOrderStatus = shopOrderStatus;
    }
}
