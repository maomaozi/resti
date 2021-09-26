package com.mmaozi.resti.example.entity;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class OrderItem {

    private int itemId;
    private int productId;
    private BigDecimal price;
}
