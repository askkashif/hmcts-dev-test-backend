package uk.gov.hmcts.reform.dev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.reform.dev.models.Case;
import uk.gov.hmcts.reform.dev.enums.CaseStatus;
import java.util.List;

public interface CaseRepository extends JpaRepository<Case, Long> {

    boolean existsByCaseNumber(String caseNumber);

    List<Case> findByStatus(CaseStatus status);

}
