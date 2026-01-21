package com.github.dimitryivaniuta.dealflow.domain.pipeline;

public enum OpportunityStage {
    INTAKE,
    DISCOVERY,
    PROPOSAL,
    NEGOTIATION,
    WON,
    LOST,

    /**
     * Soft-deleted opportunity. Preserves history and avoids FK issues.
     * Excluded from search results unless explicitly filtered.
     */
    ARCHIVED
}
