package com.telcreat.aio.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.aspectj.weaver.ast.Var;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Variant implements Comparable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;

    private int stock; // Propiedad de la subvariante

    @OneToOne
    private Picture picture;

    @Override
    public int compareTo(Object o) {
        Variant otro = (Variant) o;
        return name.compareTo(otro.getName());
    }

    public enum Status{
        ACTIVE,
        INACTIVE
    }

    @ManyToOne
    private Item item;

    private Status status;

    public Variant(String name, int stock, Picture picture, Item item, Status status) {
        this.name = name;
        this.stock = stock;
        this.picture = picture;
        this.item = item;
        this.status = status;
    }
}
