package com.github.dimitryivaniuta.dealflow.service.listing;

import com.github.dimitryivaniuta.dealflow.domain.listing.Listing;
import com.github.dimitryivaniuta.dealflow.graphql.input.CreateListingInput;
import com.github.dimitryivaniuta.dealflow.graphql.input.DeleteListingInput;
import com.github.dimitryivaniuta.dealflow.graphql.input.ListingFilterInput;
import com.github.dimitryivaniuta.dealflow.graphql.input.UpdateListingInput;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListingService {
    Page<Listing> search(UUID workspaceId, ListingFilterInput filter, Pageable pageable);

    /**
     * Returns a single listing scoped to workspace.
     */
    Listing get(UUID workspaceId, UUID listingId);

    Listing create(CreateListingInput input);

    /**
     * Patch-style update.
     */
    Listing update(UpdateListingInput input);

    /**
     * Soft delete (ARCHIVED).
     */
    Listing delete(DeleteListingInput input);

    Listing publish(UUID workspaceId, UUID listingId);
}
