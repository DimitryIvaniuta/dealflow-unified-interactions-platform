package com.github.dimitryivaniuta.dealflow.graphql.api.output;

public record PageInfo(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}