package com.github.dimitryivaniuta.dealflow.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

class GraphQlRbacIT extends BasePostgresIT {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    @Autowired
    WorkspaceRepository workspaceRepository;
    @Autowired
    WorkspaceMemberRepository memberRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    MemberRoleRepository memberRoleRepository;

    @Test
    void admin_can_create_customer_and_query_with_spec_filter() throws Exception {
        UUID wsId = demoWorkspaceId();

        // create customer
        String mutation = """
                    mutation Create($input: CreateCustomerInputInput!) {
                      createCustomer(input: $input) { id displayName status }
                    }
                """;

        Map<String, Object> variables = Map.of(
                "input", Map.of(
                        "workspaceId", wsId.toString(),
                        "displayName", "Acme Holdings",
                        "email", "hello@acme.test"
                )
        );

        String createResp = executeGraphql("user-1", mutation, variables);
                /*mvc.perform(post("/graphql")
                        .with(jwt().jwt(j -> j.subject("user-1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(graphqlBody(mutation, variables)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();*/

        assertGraphQlNoErrors(createResp);

        String query = """
                    query Q($ws: UUID!, $filter: CustomerFilterInputInput, $page: Int!, $size: Int!) {
                      customers(workspaceId: $ws, filter: $filter, page: $page, size: $size) {
                        items { displayName status }
                        pageInfo { totalElements }
                      }
                    }
                """;

        Map<String, Object> vars2 = Map.of(
                "ws", wsId.toString(),
                "filter", Map.of("text", "acme"),
                "page", 0,
                "size", 10
        );

        String resp = executeGraphql("user-1", query, vars2);
        /*mvc.perform(post("/graphql")
                        .with(jwt().jwt(j -> j.subject("user-1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(graphqlBody(query, vars2)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();*/

        assertGraphQlNoErrors(resp);
        assertThat(resp).contains("Acme Holdings");
    }

    @Test
    void viewer_cannot_create_customer() throws Exception {
        UUID wsId = demoWorkspaceId();

        ensureViewerMember(wsId, "user-viewer");

        String mutation = """
                    mutation Create($input: CreateCustomerInputInput!) {
                      createCustomer(input: $input) { id }
                    }
                """;

        Map<String, Object> variables = Map.of(
                "input", Map.of(
                        "workspaceId", wsId.toString(),
                        "displayName", "Forbidden Corp"
                )
        );

        String resp = executeGraphql("user-viewer", mutation, variables);
        /*mvc.perform(post("/graphql")
                        .with(jwt().jwt(j -> j.subject("user-viewer")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(graphqlBody(mutation, variables)))
                .andExpect(status().isOk()) // GraphQL returns 200 and puts failures into "errors"
                .andReturn().getResponse().getContentAsString();*/

        assertGraphQlHasError(resp, "Access");
    }

    private UUID demoWorkspaceId() {
        Workspace ws = workspaceRepository.findBySlug("demo").orElseThrow();
        return ws.getId();
    }

    @SneakyThrows
    private String graphqlBody(String query, Map<String, Object> variables) {
        return om.writeValueAsString(Map.of("query", query, "variables", variables));
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

    @SneakyThrows
    private void assertGraphQlNoErrors(String respJson) {
        var root = om.readTree(respJson);
        assertThat(root.get("errors")).isNull();
    }

    @SneakyThrows
    private void assertGraphQlHasError(String respJson, String expectedMessagePart) {
        assertThat(respJson).as("GraphQL response body").isNotBlank();

        var root = om.readTree(respJson);
        var errors = root.get("errors");
        assertThat(errors).isNotNull();
        String msg = errors.get(0).get("message").asText();
        assertThat(msg).containsIgnoringCase(expectedMessagePart);
    }

    private String executeGraphql(String subject, String query, Map<String, Object> variables) throws Exception {
        MvcResult r = mvc.perform(post("/graphql")
                        .with(jwt().jwt(j -> j.subject(subject)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(graphqlBody(query, variables)))
                .andExpect(status().isOk())
                .andReturn();

        // SPQR controller is async -> must dispatch to get response body
        if (r.getRequest().isAsyncStarted()) {
            r = mvc.perform(asyncDispatch(r))
                    .andExpect(status().isOk())
                    .andReturn();
        }

        return r.getResponse().getContentAsString();
    }
}
