package com.mmaozi.example.resource;

import com.mmaozi.example.entity.Customer;
import com.mmaozi.example.service.CustomerService;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.List;

@Path("/customers")
public class CustomersResource {

    private final CustomerService customerService;

    @Inject
    public CustomersResource(CustomerService customerService) {
        this.customerService = customerService;
        init();
    }

    private void init() {
        customerService.addCustomer(Customer.of(123, "maozi"));
    }

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
