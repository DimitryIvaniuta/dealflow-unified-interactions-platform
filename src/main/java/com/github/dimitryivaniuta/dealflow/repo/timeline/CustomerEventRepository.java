package com.github.dimitryivaniuta.dealflow.repo.timeline;

import com.github.dimitryivaniuta.dealflow.domain.timeline.CustomerEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CustomerEventRepository extends JpaRepository<CustomerEvent, UUID>, JpaSpecificationExecutor<CustomerEvent> {
}
