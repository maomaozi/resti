package com.mmaozi.resti.example.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class Order {

    private int orderId;
    private LocalDateTime orderTime;
}
