package uk.gov.hmcts.reform.dev.dto;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.dev.enums.CaseStatus;
import java.time.LocalDateTime;

@Data
@Builder
public class CaseResponse {
    private Long id;
    private String caseNumber;
    private String title;
    private String description;
    private CaseStatus status;
    private LocalDateTime createdDate;
}
