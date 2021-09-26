package com.mmaozi.resti.example.resource;

import com.mmaozi.resti.example.entity.Customer;
import com.mmaozi.resti.example.service.CustomerService;
import java.util.List;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/customers")
public class CustomersResource {

    private CustomerService customerService;


    @GET
    public List<Customer> getCustomers() {
        return customerService.getCustomers();

    }

    @POST
    public void addCustomer(@BeanParam Customer customer) {
        customerService.addCustomer(customer);
    }

    @Path("/{customerId}")
    public CustomerResource getCustomer(@PathParam("customerId") Integer customerId) {
        return new CustomerResource(customerService.getCustomer(customerId));
    }
}
