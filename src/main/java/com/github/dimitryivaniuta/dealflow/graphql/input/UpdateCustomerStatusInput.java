package com.github.dimitryivaniuta.dealflow.graphql.input;

import com.github.dimitryivaniuta.dealflow.domain.customer.CustomerStatus;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.types.GraphQLType;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@GraphQLType(name = "UpdateCustomerStatusInput") // must match schema name
public class UpdateCustomerStatusInput {

    @GraphQLInputField
    private UUID workspaceId;

    @GraphQLInputField
    private UUID customerId;

    @GraphQLInputField
    private CustomerStatus status;
}
