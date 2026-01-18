package com.github.dimitryivaniuta.dealflow.graphql.input;

import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.types.GraphQLType;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@GraphQLType(name = "CreateCustomerInput") // must match test
public class CreateCustomerInput {

    @GraphQLInputField
    private UUID workspaceId;

    @GraphQLInputField
    private String displayName;

    @GraphQLInputField
    private String email;

    @GraphQLInputField
    private String phone;

    @GraphQLInputField
    private String externalRef;

    @GraphQLInputField
    private UUID ownerMemberId;
}
