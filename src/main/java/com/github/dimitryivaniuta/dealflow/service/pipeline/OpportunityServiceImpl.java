package com.github.dimitryivaniuta.dealflow.service.pipeline;

import com.github.dimitryivaniuta.dealflow.domain.customer.Customer;
import com.github.dimitryivaniuta.dealflow.domain.pipeline.Opportunity;
import com.github.dimitryivaniuta.dealflow.domain.pipeline.OpportunityStage;
import com.github.dimitryivaniuta.dealflow.domain.workspace.Workspace;
import com.github.dimitryivaniuta.dealflow.domain.workspace.WorkspaceMember;
import com.github.dimitryivaniuta.dealflow.graphql.input.CreateOpportunityInput;
import com.github.dimitryivaniuta.dealflow.graphql.input.DeleteOpportunityInput;
import com.github.dimitryivaniuta.dealflow.graphql.input.OpportunityFilterInput;
import com.github.dimitryivaniuta.dealflow.graphql.input.UpdateOpportunityInput;
import com.github.dimitryivaniuta.dealflow.repo.customer.CustomerRepository;
import com.github.dimitryivaniuta.dealflow.repo.pipeline.OpportunityRepository;
import com.github.dimitryivaniuta.dealflow.repo.workspace.WorkspaceMemberRepository;
import com.github.dimitryivaniuta.dealflow.repo.workspace.WorkspaceRepository;
import com.github.dimitryivaniuta.dealflow.service.exception.BadRequestException;
import com.github.dimitryivaniuta.dealflow.service.exception.NotFoundException;
import com.github.dimitryivaniuta.dealflow.service.spec.OpportunitySpecifications;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OpportunityServiceImpl implements OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final WorkspaceRepository workspaceRepository;
    private final CustomerRepository customerRepository;
    private final WorkspaceMemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Opportunity> search(UUID workspaceId, OpportunityFilterInput filter, Pageable pageable) {
        Specification<Opportunity> spec = OpportunitySpecifications.byFilter(workspaceId, filter);
        return opportunityRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Opportunity get(UUID workspaceId, UUID opportunityId) {
        Opportunity opp = opportunityRepository.findById(opportunityId)
            .orElseThrow(() -> new NotFoundException("opportunity not found"));
        if (!opp.getWorkspace().getId().equals(workspaceId)) {
            throw new NotFoundException("opportunity not found");
        }
        return opp;
    }

    @Override
    @Transactional
    public Opportunity create(CreateOpportunityInput input) {
        if (input == null) {
            throw new BadRequestException("input is required");
        }
        Workspace workspace = workspaceRepository.findById(input.getWorkspaceId())
            .orElseThrow(() -> new NotFoundException("workspace not found"));

        Opportunity opp = new Opportunity();
        opp.setWorkspace(workspace);
        opp.setTitle(requireNonBlank(input.getTitle(), "title"));
        opp.setAmount(input.getAmount());
        opp.setExpectedCloseDate(input.getExpectedCloseDate());

        if (input.getCustomerId() != null) {
            Customer customer = customerRepository.findById(input.getCustomerId())
                .orElseThrow(() -> new NotFoundException("customer not found"));
            opp.setCustomer(customer);
        }

        if (input.getOwnerMemberId() != null) {
            WorkspaceMember owner = memberRepository.findById(input.getOwnerMemberId())
                .orElseThrow(() -> new NotFoundException("owner member not found"));
            opp.setOwner(owner);
        }

        return opportunityRepository.save(opp);
    }

    @Override
    @Transactional
    public Opportunity update(UpdateOpportunityInput input) {
        if (input == null) {
            throw new BadRequestException("input is required");
        }
        Opportunity opp = get(input.workspaceId(), input.opportunityId());

        if (input.title() != null) {
            opp.setTitle(requireNonBlank(input.title(), "title"));
        }
        if (input.amount() != null) {
            opp.setAmount(input.amount());
        }
        if (input.expectedCloseDate() != null) {
            opp.setExpectedCloseDate(input.expectedCloseDate());
        }
        if (input.stage() != null) {
            opp.setStage(input.stage());
        }

        boolean clearCustomer = Boolean.TRUE.equals(input.clearCustomer());
        if (clearCustomer) {
            opp.setCustomer(null);
        } else if (input.customerId() != null) {
            Customer customer = customerRepository.findById(input.customerId())
                .orElseThrow(() -> new NotFoundException("customer not found"));
            opp.setCustomer(customer);
        }

        boolean clearOwner = Boolean.TRUE.equals(input.clearOwner());
        if (clearOwner) {
            opp.setOwner(null);
        } else if (input.ownerMemberId() != null) {
            WorkspaceMember owner = memberRepository.findById(input.ownerMemberId())
                .orElseThrow(() -> new NotFoundException("owner member not found"));
            opp.setOwner(owner);
        }

        return opportunityRepository.save(opp);
    }

    @Override
    @Transactional
    public Opportunity delete(DeleteOpportunityInput input) {
        if (input == null) {
            throw new BadRequestException("input is required");
        }
        Opportunity opp = get(input.workspaceId(), input.opportunityId());
        opp.setStage(OpportunityStage.ARCHIVED);
        return opportunityRepository.save(opp);
    }

    @Override
    @Transactional
    public Opportunity moveStage(UUID workspaceId, UUID opportunityId, OpportunityStage stage) {
        Opportunity opp = get(workspaceId, opportunityId);
        opp.setStage(stage);
        return opportunityRepository.save(opp);
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(field + " is required");
        }
        return value.trim();
    }
}
