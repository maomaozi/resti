package com.mmaozi.example.service;

import com.mmaozi.example.entity.Customer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerService {

    private final Map<Integer, Customer> customers = new HashMap<>();

    public List<Customer> getCustomers() {
        return new ArrayList<>(customers.values());

    }

    public Customer getCustomer(Integer uid) {
        return customers.get(uid);
    }

    public void addCustomer(Customer customer) {
        customers.putIfAbsent(customer.getCustomerId(), customer);
    }
}
