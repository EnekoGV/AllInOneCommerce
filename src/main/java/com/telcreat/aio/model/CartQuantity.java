package com.telcreat.aio.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CartQuantity {
    private Variant variant;
    private int quantity;
}
