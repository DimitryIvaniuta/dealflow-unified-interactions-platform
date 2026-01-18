package com.github.dimitryivaniuta.dealflow.graphql.api.output;

import com.github.dimitryivaniuta.dealflow.domain.timeline.CustomerEvent;
import java.util.List;
import org.springframework.data.domain.Page;

public record CustomerEventConnection(
        List<CustomerEvent> items,
        PageInfo pageInfo
) {
    public static CustomerEventConnection from(Page<CustomerEvent> page) {
        return new CustomerEventConnection(
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
