package com.mmaozi.resti.example.resource;

import com.mmaozi.resti.example.entity.Order;
import com.mmaozi.resti.example.service.OrderService;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.time.LocalDateTime;
import java.util.List;

public class OrdersResource {

    private final Integer customerId;
    private final OrderService orderService;

    public OrdersResource(Integer customerId, OrderService orderService) {
        this.customerId = customerId;
        this.orderService = orderService;
        init();
    }

    public void init() {
        orderService.addOrder(123, Order.of(1, LocalDateTime.now()));
    }

    @GET
    public List<Order> getOrders() {
        return orderService.getOrders(customerId);
    }

    @POST
    public void addOrder(@BeanParam Order order) {
        orderService.addOrder(customerId, order);
    }

    @Path("/{orderId}")
    public OrderResource getOrder(@PathParam("orderId") Integer orderId) {
        Order order = orderService.getOrder(customerId, orderId);
        return new OrderResource(order);
    }
}
