package uk.gov.hmcts.reform.dev.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.dev.dto.CaseRequest;
import uk.gov.hmcts.reform.dev.dto.CaseResponse;
import uk.gov.hmcts.reform.dev.exceptions.CaseNotFoundException;
import uk.gov.hmcts.reform.dev.exceptions.CaseUpdateException;
import uk.gov.hmcts.reform.dev.exceptions.DuplicateCaseException;
import uk.gov.hmcts.reform.dev.models.Case;
import uk.gov.hmcts.reform.dev.repositories.CaseRepository;
import uk.gov.hmcts.reform.dev.utils.CaseMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaseService {

    private final CaseRepository caseRepository;

    public List<CaseResponse> getAllCases() {
        log.info("Retrieving all cases");
        try {
            List<CaseResponse> cases = caseRepository.findAll().stream()
                .map(CaseMapper::toResponse)
                .collect(Collectors.toList());
            log.info("Retrieved {} cases", cases.size());
            return cases;
        } catch (Exception e) {
            log.error("Error retrieving all cases: {}", e.getMessage());
            throw new CaseUpdateException("Failed to retrieve cases", e);
        }
    }

    public CaseResponse getCaseById(Long id) {
        log.info("Retrieving case with id: {}", id);
        try {
            return caseRepository.findById(id)
                .map(CaseMapper::toResponse)
                .orElseThrow(() -> {
                    log.error("Case not found with id: {}", id);
                    return new CaseNotFoundException(id);
                });
        } catch (CaseNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving case with id {}: {}", id, e.getMessage());
            throw new CaseUpdateException("Failed to retrieve case", e);
        }
    }

    @Transactional
    public CaseResponse createCase(CaseRequest caseRequest) {
        log.info("Creating new case with case number: {}", caseRequest.getCaseNumber());
        try {

            validateCaseRequest(caseRequest);

            if (caseRepository.existsByCaseNumber(caseRequest.getCaseNumber())) {
                log.error("Case number already exists: {}", caseRequest.getCaseNumber());
                throw new DuplicateCaseException("Case number already exists: " + caseRequest.getCaseNumber());
            }

            Case newCase = CaseMapper.toEntity(caseRequest);
            Case savedCase = caseRepository.save(newCase);
            log.info("Successfully created case with id: {}", savedCase.getId());
            return CaseMapper.toResponse(savedCase);
        } catch (DuplicateCaseException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating case: {}", e.getMessage());
            throw new CaseUpdateException("Failed to create case", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public CaseResponse updateCase(Long id, CaseRequest caseRequest) {
        log.info("Updating case with id: {}", id);
        try {
            validateCaseRequest(caseRequest);

            Case existingCase = caseRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Case not found with id: {}", id);
                    return new CaseNotFoundException(id);
                });

            // Check if the new case number conflicts with another case
            if (!existingCase.getCaseNumber().equals(caseRequest.getCaseNumber())
                && caseRepository.existsByCaseNumber(caseRequest.getCaseNumber())) {
                log.error("Cannot update case. Case number already exists: {}", caseRequest.getCaseNumber());
                throw new DuplicateCaseException("Case number already exists: " + caseRequest.getCaseNumber());
            }

            CaseMapper.updateEntityFromRequest(existingCase, caseRequest); // Use new mapper method
            Case updatedCase = caseRepository.save(existingCase);
            log.info("Successfully updated case with id: {}", id);
            return CaseMapper.toResponse(updatedCase);
        } catch (CaseNotFoundException | DuplicateCaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating case with id {}: {}", id, e.getMessage());
            throw new CaseUpdateException("Failed to update case", e);
        }
    }

    // Add validation method
    private void validateCaseRequest(CaseRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Case request cannot be null");
        }
        if (request.getCaseNumber() == null || request.getCaseNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Case number is required");
        }
        // Add case number format validation
        if (!request.getCaseNumber().matches("^[A-Z0-9]{2,20}$")) {
            throw new IllegalArgumentException("Case number must be 2-20 characters long and "
                                                   + "contain only uppercase letters and numbers");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Case title is required");
        }
        // Add title length validation
        if (request.getTitle().length() < 3 || request.getTitle().length() > 100) {
            throw new IllegalArgumentException("Title must be between 3 and 100 characters");
        }
        if (request.getStatus() == null) {
            throw new IllegalArgumentException("Case status is required");
        }
        // Add description length validation if description is present
        if (request.getDescription() != null && request.getDescription().length() > 500) {
            throw new IllegalArgumentException("Description cannot exceed 500 characters");
        }
    }

    @Transactional
    public void deleteCase(Long id) {
        log.info("Deleting case with id: {}", id);
        try {
            if (!caseRepository.existsById(id)) {
                log.error("Case not found with id: {}", id);
                throw new CaseNotFoundException(id);
            }
            caseRepository.deleteById(id);
            log.info("Successfully deleted case with id: {}", id);
        } catch (CaseNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting case with id {}: {}", id, e.getMessage());
            throw new CaseUpdateException("Failed to delete case", e);
        }
    }
}
