package com.github.dimitryivaniuta.dealflow.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dimitryivaniuta.dealflow.domain.security.RoleKey;
import com.github.dimitryivaniuta.dealflow.domain.workspace.MemberRole;
import com.github.dimitryivaniuta.dealflow.domain.workspace.MemberStatus;
import com.github.dimitryivaniuta.dealflow.domain.workspace.Workspace;
import com.github.dimitryivaniuta.dealflow.domain.workspace.WorkspaceMember;
import com.github.dimitryivaniuta.dealflow.infra.BasePostgresIT;
import com.github.dimitryivaniuta.dealflow.repo.security.RoleRepository;
import com.github.dimitryivaniuta.dealflow.repo.workspace.MemberRoleRepository;
import com.github.dimitryivaniuta.dealflow.repo.workspace.WorkspaceMemberRepository;
import com.github.dimitryivaniuta.dealflow.repo.workspace.WorkspaceRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

class CustomerTimelineGraphQlApiIT extends BasePostgresIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @Autowired WorkspaceRepository workspaceRepository;
    @Autowired WorkspaceMemberRepository memberRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired MemberRoleRepository memberRoleRepository;

    @Test
    void admin_can_append_event_and_query_with_filters() throws Exception {
        UUID wsId = demoWorkspaceId();

        // 1) Create customer (admin user-1 is seeded by Flyway)
        String createCustomer = """
            mutation Create($input: CreateCustomerInputInput!) {
              createCustomer(input: $input) { id displayName status }
            }
        """;

        Map<String, Object> vars1 = Map.of(
                "input", Map.of(
                        "workspaceId", wsId.toString(),
                        "displayName", "Timeline Corp",
                        "email", "timeline@corp.test"
                )
        );

        String resp1 = executeGraphql("user-1", createCustomer, vars1);
        assertGraphQlNoErrors(resp1);

        String customerId = om.readTree(resp1).at("/data/createCustomer/id").asText();
        assertThat(customerId).isNotBlank();

        // 2) Add note (timeline write)
        String addNote = """
            mutation Add($ws: UUID!, $cid: UUID!, $note: String!) {
              addCustomerNote(workspaceId: $ws, customerId: $cid, note: $note) { id eventType category summary }
            }
        """;
        Map<String, Object> vars2 = Map.of(
                "ws", wsId.toString(),
                "cid", customerId,
                "note", "Called customer, sent follow-up"
        );

        String resp2 = executeGraphql("user-1", addNote, vars2);
        assertGraphQlNoErrors(resp2);
        assertThat(resp2).contains("NOTE_ADDED");

        // 3) Query timeline with type filter
        String query = """
            query Timeline($ws: UUID!, $cid: UUID!, $filter: CustomerTimelineFilterInputInput, $page: Int!, $size: Int!) {
              customerTimeline(workspaceId: $ws, customerId: $cid, filter: $filter, page: $page, size: $size) {
                items { eventType summary }
                pageInfo { totalElements }
              }
            }
        """;

        Map<String, Object> filter = Map.of(
                "types", List.of("NOTE_ADDED"),
                "text", "follow-up"
        );

        Map<String, Object> vars3 = Map.of(
                "ws", wsId.toString(),
                "cid", customerId,
                "filter", filter,
                "page", 0,
                "size", 10
        );

        String resp3 = executeGraphql("user-1", query, vars3);
        assertGraphQlNoErrors(resp3);
        assertThat(resp3).contains("NOTE_ADDED");
        assertThat(resp3).contains("Note added");
    }

    @Test
    void viewer_can_read_timeline_but_cannot_write() throws Exception {
        UUID wsId = demoWorkspaceId();
        ensureViewerMember(wsId, "user-viewer");

        // create customer as admin
        String createCustomer = """
            mutation Create($input: CreateCustomerInputInput!) {
              createCustomer(input: $input) { id }
            }
        """;

        Map<String, Object> vars1 = Map.of(
                "input", Map.of(
                        "workspaceId", wsId.toString(),
                        "displayName", "Viewer Timeline Corp"
                )
        );

        String resp1 = executeGraphql("user-1", createCustomer, vars1);
        assertGraphQlNoErrors(resp1);
        String customerId = om.readTree(resp1).at("/data/createCustomer/id").asText();

        // viewer tries to write
        String addNote = """
            mutation Add($ws: UUID!, $cid: UUID!, $note: String!) {
              addCustomerNote(workspaceId: $ws, customerId: $cid, note: $note) { id }
            }
        """;
        Map<String, Object> vars2 = Map.of(
                "ws", wsId.toString(),
                "cid", customerId,
                "note", "Should fail"
        );

        String resp2 = executeGraphql("user-viewer", addNote, vars2);
        assertGraphQlHasError(resp2, "Access");

        // viewer can read
        String query = """
            query Timeline($ws: UUID!, $cid: UUID!) {
              customerTimeline(workspaceId: $ws, customerId: $cid, filter: null, page: 0, size: 10) {
                items { id }
                pageInfo { totalElements }
              }
            }
        """;

        String resp3 = executeGraphql("user-viewer", query, Map.of("ws", wsId.toString(), "cid", customerId));
        assertGraphQlNoErrors(resp3);
    }

    private UUID demoWorkspaceId() {
        Workspace ws = workspaceRepository.findBySlug("demo").orElseThrow();
        return ws.getId();
    }

    @SneakyThrows
    private String graphqlBody(String query, Map<String, Object> variables) {
        return om.writeValueAsString(Map.of("query", query, "variables", variables));
    }

    private String executeGraphql(String subject, String query, Map<String, Object> variables) throws Exception {
        MvcResult r = mvc.perform(post("/graphql")
                        .with(jwt().jwt(j -> j.subject(subject)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(graphqlBody(query, variables)))
                .andExpect(status().isOk())
                .andReturn();

        if (r.getRequest().isAsyncStarted()) {
            r = mvc.perform(asyncDispatch(r))
                    .andExpect(status().isOk())
                    .andReturn();
        }

        return r.getResponse().getContentAsString();
    }

    @SneakyThrows
    private void assertGraphQlNoErrors(String respJson) {
        var root = om.readTree(respJson);
        assertThat(root.get("errors")).isNull();
    }

    @SneakyThrows
    private void assertGraphQlHasError(String respJson, String expectedMessagePart) {
        var root = om.readTree(respJson);
        var errors = root.get("errors");
        assertThat(errors).isNotNull();
        String msg = errors.get(0).get("message").asText();
        assertThat(msg).containsIgnoringCase(expectedMessagePart);
    }

    private void ensureViewerMember(UUID wsId, String subject) {
        runAsSubject(subject, () -> {
            Workspace ws = workspaceRepository.findById(wsId).orElseThrow();

            WorkspaceMember member = memberRepository.findByWorkspaceAndSubject(wsId, subject).orElse(null);
            if (member == null) {
                member = new WorkspaceMember();
                member.setWorkspace(ws);
                member.setSubject(subject);
                member.setEmail(subject + "@example.com");
                member.setDisplayName("Viewer User");
                member.setStatus(MemberStatus.ACTIVE);
                member = memberRepository.save(member);
            }

            var viewerRole = roleRepository.findByRoleKey(RoleKey.VIEWER).orElseThrow();
            if (!memberRoleRepository.existsByMember_IdAndRole_Id(member.getId(), viewerRole.getId())) {
                MemberRole mr = new MemberRole();
                mr.setMember(member);
                mr.setRole(viewerRole);
                memberRoleRepository.save(mr);
            }
        });
    }

    private void runAsSubject(String subject, Runnable action) {
        var ctx = SecurityContextHolder.createEmptyContext();
        Jwt jwt = Jwt.withTokenValue("it-token")
                .header("alg", "none")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claims(c -> c.putAll(Map.of("sub", subject)))
                .build();

        ctx.setAuthentication(new JwtAuthenticationToken(jwt, List.of()));
        SecurityContextHolder.setContext(ctx);
        try {
            action.run();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
