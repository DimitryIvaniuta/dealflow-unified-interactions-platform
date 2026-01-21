package com.github.dimitryivaniuta.dealflow.graphql.api;

import com.github.dimitryivaniuta.dealflow.domain.pipeline.Opportunity;
import com.github.dimitryivaniuta.dealflow.graphql.input.CreateOpportunityInput;
import com.github.dimitryivaniuta.dealflow.graphql.input.DeleteOpportunityInput;
import com.github.dimitryivaniuta.dealflow.graphql.input.MoveOpportunityStageInput;
import com.github.dimitryivaniuta.dealflow.graphql.input.OpportunityFilterInput;
import com.github.dimitryivaniuta.dealflow.graphql.input.UpdateOpportunityInput;
import com.github.dimitryivaniuta.dealflow.service.pipeline.OpportunityService;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@GraphQLApi
@Component
@RequiredArgsConstructor
public class PipelineGraphQlApi {

    private final OpportunityService opportunityService;

    @GraphQLQuery(name = "opportunity")
    @PreAuthorize("@wsSec.hasPermission(#workspaceId, T(com.github.dimitryivaniuta.dealflow.domain.security.PermissionCode).PIPELINE_READ)")
    public Opportunity opportunity(
        @GraphQLArgument(name = "workspaceId") UUID workspaceId,
        @GraphQLArgument(name = "opportunityId") UUID opportunityId
    ) {
        return opportunityService.get(workspaceId, opportunityId);
    }

    @GraphQLQuery(name = "opportunities")
    @PreAuthorize("@wsSec.hasPermission(#workspaceId, T(com.github.dimitryivaniuta.dealflow.domain.security.PermissionCode).PIPELINE_READ)")
    public Page<Opportunity> opportunities(
        @GraphQLArgument(name = "workspaceId") UUID workspaceId,
        @GraphQLArgument(name = "filter") OpportunityFilterInput filter,
        @GraphQLArgument(name = "page") int page,
        @GraphQLArgument(name = "size") int size
    ) {
        return opportunityService.search(workspaceId, filter,
            PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 200)));
    }

    @GraphQLMutation(name = "createOpportunity")
    @PreAuthorize("@wsSec.hasPermission(#input.workspaceId, T(com.github.dimitryivaniuta.dealflow.domain.security.PermissionCode).PIPELINE_WRITE)")
    public Opportunity createOpportunity(@GraphQLArgument(name = "input") CreateOpportunityInput input) {
        return opportunityService.create(input);
    }

    @GraphQLMutation(name = "updateOpportunity")
    @PreAuthorize("@wsSec.hasPermission(#input.workspaceId, T(com.github.dimitryivaniuta.dealflow.domain.security.PermissionCode).PIPELINE_WRITE)")
    public Opportunity updateOpportunity(@GraphQLArgument(name = "input") UpdateOpportunityInput input) {
        return opportunityService.update(input);
    }

    @GraphQLMutation(name = "deleteOpportunity")
    @PreAuthorize("@wsSec.hasPermission(#input.workspaceId, T(com.github.dimitryivaniuta.dealflow.domain.security.PermissionCode).PIPELINE_WRITE)")
    public Opportunity deleteOpportunity(@GraphQLArgument(name = "input") DeleteOpportunityInput input) {
        return opportunityService.delete(input);
    }

    @GraphQLMutation(name = "moveOpportunityStage")
    @PreAuthorize("@wsSec.hasPermission(#input.workspaceId, T(com.github.dimitryivaniuta.dealflow.domain.security.PermissionCode).PIPELINE_WRITE)")
    public Opportunity moveOpportunityStage(@GraphQLArgument(name = "input") MoveOpportunityStageInput input) {
        return opportunityService.moveStage(input.getWorkspaceId(), input.getOpportunityId(), input.getStage());
    }
}
