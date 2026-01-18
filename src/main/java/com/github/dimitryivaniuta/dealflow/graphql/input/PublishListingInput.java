package com.github.dimitryivaniuta.dealflow.graphql.input;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

public record PublishListingInput(
        UUID workspaceId,
        UUID listingId
) {
}