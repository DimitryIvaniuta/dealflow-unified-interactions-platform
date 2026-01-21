package com.github.dimitryivaniuta.dealflow.service.pipeline;

import com.github.dimitryivaniuta.dealflow.domain.pipeline.Opportunity;
import com.github.dimitryivaniuta.dealflow.domain.pipeline.OpportunityStage;
import com.github.dimitryivaniuta.dealflow.graphql.input.CreateOpportunityInput;
import com.github.dimitryivaniuta.dealflow.graphql.input.DeleteOpportunityInput;
import com.github.dimitryivaniuta.dealflow.graphql.input.OpportunityFilterInput;
import com.github.dimitryivaniuta.dealflow.graphql.input.UpdateOpportunityInput;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OpportunityService {
    Page<Opportunity> search(UUID workspaceId, OpportunityFilterInput filter, Pageable pageable);

    /** Returns a single opportunity scoped to workspace. */
    Opportunity get(UUID workspaceId, UUID opportunityId);

    Opportunity create(CreateOpportunityInput input);

    /** Patch-style update. */
    Opportunity update(UpdateOpportunityInput input);

    /** Soft delete (ARCHIVED). */
    Opportunity delete(DeleteOpportunityInput input);

    Opportunity moveStage(UUID workspaceId, UUID opportunityId, OpportunityStage stage);
}
