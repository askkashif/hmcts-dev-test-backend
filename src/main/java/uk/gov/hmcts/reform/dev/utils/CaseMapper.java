package uk.gov.hmcts.reform.dev.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.dev.dto.CaseRequest;
import uk.gov.hmcts.reform.dev.dto.CaseResponse;
import uk.gov.hmcts.reform.dev.models.Case;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
public class CaseMapper {

    private CaseMapper() {
        // Private constructor to prevent instantiation
    }

    public static Case toEntity(CaseRequest request) {
        Objects.requireNonNull(request, "CaseRequest cannot be null");

        log.debug("Converting CaseRequest to Entity: {}", request.getCaseNumber());
        Case caseEntity = new Case();
        caseEntity.setCaseNumber(request.getCaseNumber());
        caseEntity.setTitle(request.getTitle());
        caseEntity.setDescription(request.getDescription());
        caseEntity.setStatus(request.getStatus());
        caseEntity.setCreatedDate(LocalDateTime.now());
        return caseEntity;
    }

    public static CaseResponse toResponse(Case caseEntity) {
        Objects.requireNonNull(caseEntity, "Case entity cannot be null");

        log.debug("Converting Case Entity to Response: {}", caseEntity.getCaseNumber());
        return CaseResponse.builder()
            .id(caseEntity.getId())
            .caseNumber(caseEntity.getCaseNumber())
            .title(caseEntity.getTitle())
            .description(caseEntity.getDescription())
            .status(caseEntity.getStatus())
            .createdDate(caseEntity.getCreatedDate())
            .build();
    }

    public static void updateEntityFromRequest(Case existingCase, CaseRequest request) {
        Objects.requireNonNull(existingCase, "Existing case cannot be null");
        Objects.requireNonNull(request, "CaseRequest cannot be null");

        log.debug("Updating Case Entity from Request: {}", request.getCaseNumber());
        existingCase.setCaseNumber(request.getCaseNumber());
        existingCase.setTitle(request.getTitle());
        existingCase.setDescription(request.getDescription());
        existingCase.setStatus(request.getStatus());
    }
}
