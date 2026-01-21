package com.github.dimitryivaniuta.dealflow.graphql.api;

import com.github.dimitryivaniuta.dealflow.domain.listing.Listing;
import com.github.dimitryivaniuta.dealflow.graphql.api.output.ListingConnection;
import com.github.dimitryivaniuta.dealflow.graphql.input.CreateListingInput;
import com.github.dimitryivaniuta.dealflow.graphql.input.DeleteListingInput;
import com.github.dimitryivaniuta.dealflow.graphql.input.ListingFilterInput;
import com.github.dimitryivaniuta.dealflow.graphql.input.PublishListingInput;
import com.github.dimitryivaniuta.dealflow.graphql.input.UpdateListingInput;
import com.github.dimitryivaniuta.dealflow.service.listing.ListingService;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;

@GraphQLApi
@RequiredArgsConstructor
public class ListingGraphQlApi {
    @GraphQLQuery(name = "listing")
    @PreAuthorize("@wsSec.hasPermission(#workspaceId, T(com.github.dimitryivaniuta.dealflow.domain.security.PermissionCode).LISTING_READ)")
    public Listing listing(
            @GraphQLArgument(name = "workspaceId") UUID workspaceId,
            @GraphQLArgument(name = "listingId") UUID listingId
    ) {
        return listingService.get(workspaceId, listingId);
    }


    private final ListingService listingService;

    @GraphQLQuery(name = "listings")
    @PreAuthorize("@wsSec.hasPermission(#workspaceId, T(com.github.dimitryivaniuta.dealflow.domain.security.PermissionCode).LISTING_READ)")
    public ListingConnection listings(
            @GraphQLArgument(name = "workspaceId") UUID workspaceId,
            @GraphQLArgument(name = "filter") ListingFilterInput filter,
            @GraphQLArgument(name = "page") int page,
            @GraphQLArgument(name = "size") int size
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 200);

        Page<Listing> result = listingService.search(workspaceId, filter, PageRequest.of(safePage, safeSize));
        return ListingConnection.from(result);
    }

    @GraphQLMutation(name = "createListing")
    @PreAuthorize("@wsSec.hasPermission(#input.workspaceId, T(com.github.dimitryivaniuta.dealflow.domain.security.PermissionCode).LISTING_WRITE)")
    public Listing createListing(@GraphQLArgument(name = "input") CreateListingInput input) {
        return listingService.create(input);
    }

    @GraphQLMutation(name = "publishListing")
    @PreAuthorize("@wsSec.hasPermission(#input.workspaceId, T(com.github.dimitryivaniuta.dealflow.domain.security.PermissionCode).LISTING_WRITE)")
    public Listing publishListing(@GraphQLArgument(name = "input") PublishListingInput input) {
        // IMPORTANT:
        // If PublishListingInput is a record -> use input.workspaceId() / input.listingId()
        // If it's a Lombok class -> use getters
        UUID workspaceId = input.workspaceId();
        UUID listingId = input.listingId();

        return listingService.publish(workspaceId, listingId);
    }

    @GraphQLMutation(name = "updateListing")
    @PreAuthorize("@wsSec.hasPermission(#input.workspaceId, T(com.github.dimitryivaniuta.dealflow.domain.security.PermissionCode).LISTING_WRITE)")
    public Listing updateListing(@GraphQLArgument(name = "input") UpdateListingInput input) {
        return listingService.update(input);
    }

    @GraphQLMutation(name = "deleteListing")
    @PreAuthorize("@wsSec.hasPermission(#input.workspaceId, T(com.github.dimitryivaniuta.dealflow.domain.security.PermissionCode).LISTING_WRITE)")
    public Listing deleteListing(@GraphQLArgument(name = "input") DeleteListingInput input) {
        return listingService.delete(input);
    }
}
