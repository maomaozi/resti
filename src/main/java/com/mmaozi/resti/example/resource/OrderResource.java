package com.mmaozi.resti.example.resource;

import com.mmaozi.resti.example.entity.Order;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class OrderResource {

    private Order order;

    @GET
    public Order getOrder() {
        return order;
    }

    @Path("/items")
    public OrderItemsResource getOrders() {
        return new OrderItemsResource(order.getOrderId());
    }

}
