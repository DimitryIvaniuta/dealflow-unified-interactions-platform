package com.github.dimitryivaniuta.dealflow.graphql.input;

import com.github.dimitryivaniuta.dealflow.domain.customer.CustomerStatus;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.types.GraphQLType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@GraphQLType(name = "CustomerFilterInput") // must match test
public class CustomerFilterInput {

    @GraphQLInputField
    private String text;

    @GraphQLInputField
    private CustomerStatus status;

    @GraphQLInputField
    private UUID ownerMemberId;
}
