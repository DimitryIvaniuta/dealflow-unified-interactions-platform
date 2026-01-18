package com.github.dimitryivaniuta.dealflow.graphql.api.output;

import com.github.dimitryivaniuta.dealflow.domain.listing.Listing;

import java.util.List;

import org.springframework.data.domain.Page;

public record ListingConnection(
        List<Listing> items,
        PageInfo pageInfo
) {
    public static ListingConnection from(Page<Listing> page) {
        return new ListingConnection(
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
