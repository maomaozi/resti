package com.mmaozi.intg.example.resource;

import com.mmaozi.intg.example.entity.Customer;
import com.mmaozi.intg.example.service.OrderService;
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

//    @GET
//    public Response getCustomer() {
//        if (Objects.isNull(customer)) {
//            return Response.status(404).build();
//        }
//        return Response.ok(customer).build();
//    }

    @Path("/orders")
    public OrdersResource getOrders() {
        return new OrdersResource(customer.getCustomerId(), new OrderService());
    }

}
