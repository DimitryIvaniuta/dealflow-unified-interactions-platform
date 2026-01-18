package com.github.dimitryivaniuta.dealflow.graphql.api.output;

import com.github.dimitryivaniuta.dealflow.domain.customer.Customer;

import java.util.List;

import org.springframework.data.domain.Page;

public record CustomerConnection(
        List<Customer> items,
        PageInfo pageInfo
) {
    public static CustomerConnection from(Page<Customer> page) {
        return new CustomerConnection(
                page.getContent(),
                new PageInfo(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages(),
                        page.hasNext(),
                        page.hasPrevious()
                )
        );
    }
}
