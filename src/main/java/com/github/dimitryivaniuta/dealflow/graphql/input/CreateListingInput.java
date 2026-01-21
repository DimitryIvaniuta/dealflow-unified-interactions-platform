package com.github.dimitryivaniuta.dealflow.graphql.input;

import java.math.BigDecimal;
import java.util.UUID;

import com.github.dimitryivaniuta.dealflow.domain.listing.ListingStatus;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.types.GraphQLType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@GraphQLType(name = "CreateListingInput")
public class CreateListingInput {
    @GraphQLInputField
    private UUID workspaceId;

    @GraphQLInputField
    private String title;

    @GraphQLInputField
    private String city;

    @GraphQLInputField
    private BigDecimal askingPrice;

    @GraphQLInputField
    private UUID customerId;

    @GraphQLInputField
    @GraphQLNonNull
    private ListingStatus status;
}
