package com.github.dimitryivaniuta.dealflow.graphql.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dimitryivaniuta.dealflow.domain.timeline.CustomerEvent;
import com.github.dimitryivaniuta.dealflow.domain.timeline.CustomerEventCategory;
import com.github.dimitryivaniuta.dealflow.domain.timeline.CustomerEventSource;
import com.github.dimitryivaniuta.dealflow.domain.timeline.CustomerEventType;
import com.github.dimitryivaniuta.dealflow.graphql.api.output.CustomerEventConnection;
import com.github.dimitryivaniuta.dealflow.graphql.input.timeline.AppendCustomerEventInput;
import com.github.dimitryivaniuta.dealflow.graphql.input.timeline.CustomerTimelineFilterInput;
import com.github.dimitryivaniuta.dealflow.service.timeline.CustomerTimelineService;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@GraphQLApi
@Component
@RequiredArgsConstructor
public class CustomerTimelineGraphQlApi {

    private final CustomerTimelineService timeline;
    private final ObjectMapper om;

    @GraphQLQuery(name = "customerTimeline")
    @PreAuthorize("@wsSec.hasPermission(#workspaceId, T(com.github.dimitryivaniuta.dealflow.domain.security.PermissionCode).CUSTOMER_TIMELINE_READ)")
    public CustomerEventConnection customerTimeline(
            @GraphQLArgument(name = "workspaceId") UUID workspaceId,
            @GraphQLArgument(name = "customerId") UUID customerId,
            @GraphQLArgument(name = "filter") CustomerTimelineFilterInput filter,
            @GraphQLArgument(name = "page") int page,
            @GraphQLArgument(name = "size") int size
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 200);

        var pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "occurredAt"));
        var result = timeline.timeline(workspaceId, customerId, filter, pageable);
        return CustomerEventConnection.from(result);
    }

    /**
     * Generic mutation for integrations or advanced UI forms.
     */
    @GraphQLMutation(name = "appendCustomerEvent")
    @PreAuthorize("@wsSec.hasPermission(#input.workspaceId, T(com.github.dimitryivaniuta.dealflow.domain.security.PermissionCode).CUSTOMER_TIMELINE_WRITE)")
    public CustomerEvent appendCustomerEvent(
            @GraphQLArgument(name = "input") AppendCustomerEventInput input,
            Authentication auth
    ) {
        return timeline.append(input, subject(auth));
    }

    /**
     * Convenience mutation: adds a NOTE_ADDED event with minimal structured payload.
     */
    @GraphQLMutation(name = "addCustomerNote")
    @PreAuthorize("@wsSec.hasPermission(#workspaceId, T(com.github.dimitryivaniuta.dealflow.domain.security.PermissionCode).CUSTOMER_TIMELINE_WRITE)")
    public CustomerEvent addCustomerNote(
            @GraphQLArgument(name = "workspaceId") UUID workspaceId,
            @GraphQLArgument(name = "customerId") UUID customerId,
            @GraphQLArgument(name = "note") String note,
            Authentication auth
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("note", note);

        return timeline.append(
                new AppendCustomerEventInput(
                        workspaceId,
                        customerId,
                        CustomerEventType.NOTE_ADDED,
                        CustomerEventCategory.RELATIONSHIP,
                        CustomerEventSource.MANUAL,
                        Instant.now(),
                        false,
                        "Note added",
                        om.valueToTree(payload),
                        null,
                        null,
                        null
                ),
                subject(auth)
        );
    }

    /**
     * Convenience mutation: record email sent (unified timeline item for frontend).
     */
    @GraphQLMutation(name = "recordEmailSent")
    @PreAuthorize("@wsSec.hasPermission(#workspaceId, T(com.github.dimitryivaniuta.dealflow.domain.security.PermissionCode).CUSTOMER_TIMELINE_WRITE)")
    public CustomerEvent recordEmailSent(
            @GraphQLArgument(name = "workspaceId") UUID workspaceId,
            @GraphQLArgument(name = "customerId") UUID customerId,
            @GraphQLArgument(name = "subject") String subject,
            @GraphQLArgument(name = "to") String to,
            @GraphQLArgument(name = "templateKey") String templateKey,
            Authentication auth
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfNotBlank(payload, "to", to);
        putIfNotBlank(payload, "subject", subject);
        putIfNotBlank(payload, "templateKey", templateKey);

        return timeline.append(
                new AppendCustomerEventInput(
                        workspaceId,
                        customerId,
                        CustomerEventType.EMAIL_SENT,
                        CustomerEventCategory.COMMUNICATION,
                        CustomerEventSource.SYSTEM,
                        Instant.now(),
                        false,
                        "Email sent",
                        om.valueToTree(payload),
                        null,
                        null,
                        null
                ),
                subject(auth)
        );
    }

    @GraphQLMutation(name = "recordContactAdded")
    @PreAuthorize("@wsSec.hasPermission(#workspaceId, T(com.github.dimitryivaniuta.dealflow.domain.security.PermissionCode).CUSTOMER_TIMELINE_WRITE)")
    public CustomerEvent recordContactAdded(
            @GraphQLArgument(name = "workspaceId") UUID workspaceId,
            @GraphQLArgument(name = "customerId") UUID customerId,
            @GraphQLArgument(name = "contactName") String contactName,
            @GraphQLArgument(name = "contactEmail") String contactEmail,
            @GraphQLArgument(name = "contactPhone") String contactPhone,
            Authentication auth
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfNotBlank(payload, "name", contactName);
        putIfNotBlank(payload, "email", contactEmail);
        putIfNotBlank(payload, "phone", contactPhone);

        return timeline.append(
                new AppendCustomerEventInput(
                        workspaceId,
                        customerId,
                        CustomerEventType.CONTACT_ADDED,
                        CustomerEventCategory.RELATIONSHIP,
                        CustomerEventSource.MANUAL,
                        Instant.now(),
                        false,
                        "Contact added",
                        om.valueToTree(payload),
                        null,
                        null,
                        null
                ),
                subject(auth)
        );
    }

    @GraphQLMutation(name = "recordTaskCompleted")
    @PreAuthorize("@wsSec.hasPermission(#workspaceId, T(com.github.dimitryivaniuta.dealflow.domain.security.PermissionCode).CUSTOMER_TIMELINE_WRITE)")
    public CustomerEvent recordTaskCompleted(
            @GraphQLArgument(name = "workspaceId") UUID workspaceId,
            @GraphQLArgument(name = "customerId") UUID customerId,
            @GraphQLArgument(name = "taskTitle") String taskTitle,
            @GraphQLArgument(name = "taskId") String taskId,
            Authentication auth
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfNotBlank(payload, "taskId", taskId);
        putIfNotBlank(payload, "title", taskTitle);

        String summary = (taskTitle == null || taskTitle.isBlank()) ? "Task completed" : ("Task completed: " + taskTitle);

        return timeline.append(
                new AppendCustomerEventInput(
                        workspaceId,
                        customerId,
                        CustomerEventType.TASK_COMPLETED,
                        CustomerEventCategory.TASKS,
                        CustomerEventSource.SYSTEM,
                        Instant.now(),
                        false,
                        summary,
                        om.valueToTree(payload),
                        null,
                        null,
                        null
                ),
                subject(auth)
        );
    }

    @GraphQLMutation(name = "recordTransactionAccepted")
    @PreAuthorize("@wsSec.hasPermission(#workspaceId, T(com.github.dimitryivaniuta.dealflow.domain.security.PermissionCode).CUSTOMER_TIMELINE_WRITE)")
    public CustomerEvent recordTransactionAccepted(
            @GraphQLArgument(name = "workspaceId") UUID workspaceId,
            @GraphQLArgument(name = "customerId") UUID customerId,
            @GraphQLArgument(name = "transactionId") UUID transactionId,
            @GraphQLArgument(name = "amount") String amount,
            @GraphQLArgument(name = "currency") String currency,
            Authentication auth
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        if (transactionId != null) payload.put("transactionId", transactionId.toString());
        putIfNotBlank(payload, "amount", amount);
        putIfNotBlank(payload, "currency", currency);

        return timeline.append(
                new AppendCustomerEventInput(
                        workspaceId,
                        customerId,
                        CustomerEventType.TRANSACTION_ACCEPTED,
                        CustomerEventCategory.DEAL,
                        CustomerEventSource.SYSTEM,
                        Instant.now(),
                        false,
                        "Transaction accepted",
                        om.valueToTree(payload),
                        null,
                        null,
                        transactionId
                ),
                subject(auth)
        );
    }

    private static void putIfNotBlank(Map<String, Object> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, value);
        }
    }

    private static String subject(Authentication auth) {
        return (auth == null || auth.getName() == null || auth.getName().isBlank()) ? "system" : auth.getName();
    }
}
