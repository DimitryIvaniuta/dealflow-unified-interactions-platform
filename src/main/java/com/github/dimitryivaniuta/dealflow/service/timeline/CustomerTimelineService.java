package com.github.dimitryivaniuta.dealflow.service.timeline;

import com.github.dimitryivaniuta.dealflow.domain.timeline.CustomerEvent;
import com.github.dimitryivaniuta.dealflow.graphql.input.timeline.AppendCustomerEventInput;
import com.github.dimitryivaniuta.dealflow.graphql.input.timeline.CustomerTimelineFilterInput;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerTimelineService {

    Page<CustomerEvent> timeline(UUID workspaceId, UUID customerId, CustomerTimelineFilterInput filter, Pageable pageable);

    CustomerEvent append(AppendCustomerEventInput input, String actorSubject);
}
