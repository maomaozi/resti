package com.mmaozi.example.resource;

import com.mmaozi.example.entity.Customer;
import com.mmaozi.example.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@AllArgsConstructor
@NoArgsConstructor
public class CustomerResource {

    private Customer customer;

    @GET
    public Customer getCustomer() {
        return customer;
    }

    @Path("/orders")
    public OrdersResource getOrders() {
        return new OrdersResource(customer.getCustomerId(), new OrderService());
    }

}
