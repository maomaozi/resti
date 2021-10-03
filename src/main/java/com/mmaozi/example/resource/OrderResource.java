package com.mmaozi.example.resource;

import com.mmaozi.example.entity.Order;
import com.mmaozi.example.service.ItemService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

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
        return new OrderItemsResource(order.getOrderId(), new ItemService());
    }

}
