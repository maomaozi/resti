package com.mmaozi.intg.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class OrderItem {

    private int itemId;
    private int productId;
    private BigDecimal price;
}
