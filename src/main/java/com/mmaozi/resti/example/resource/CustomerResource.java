package com.mmaozi.resti.example.resource;

import com.mmaozi.resti.example.entity.Customer;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

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
        return new OrdersResource(customer.getCustomerId());
    }

}
