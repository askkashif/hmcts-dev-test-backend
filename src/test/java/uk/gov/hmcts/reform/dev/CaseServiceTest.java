package uk.gov.hmcts.reform.dev;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dev.dto.CaseRequest;
import uk.gov.hmcts.reform.dev.dto.CaseResponse;
import uk.gov.hmcts.reform.dev.enums.CaseStatus;
import uk.gov.hmcts.reform.dev.exceptions.CaseNotFoundException;
import uk.gov.hmcts.reform.dev.exceptions.DuplicateCaseException;
import uk.gov.hmcts.reform.dev.models.Case;
import uk.gov.hmcts.reform.dev.repositories.CaseRepository;
import uk.gov.hmcts.reform.dev.services.CaseService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {

    @Mock
    private CaseRepository caseRepository;

    @InjectMocks
    private CaseService caseService;

    private Case testCase;
    private CaseRequest testRequest;

    @BeforeEach
    void setUp() {
        testCase = new Case();
        testCase.setId(1L);
        testCase.setCaseNumber("TEST123");
        testCase.setTitle("Test Case");
        testCase.setDescription("Test Description");
        testCase.setStatus(CaseStatus.NEW);
        testCase.setCreatedDate(LocalDateTime.now());

        testRequest = new CaseRequest();
        testRequest.setCaseNumber("TEST123");
        testRequest.setTitle("Test Case");
        testRequest.setDescription("Test Description");
        testRequest.setStatus(CaseStatus.NEW);
    }

    @Test
    void shouldCreateCase() {
        when(caseRepository.existsByCaseNumber(anyString())).thenReturn(false);
        when(caseRepository.save(any(Case.class))).thenReturn(testCase);

        CaseResponse response = caseService.createCase(testRequest);

        assertNotNull(response);
        assertEquals(testCase.getCaseNumber(), response.getCaseNumber());
        assertEquals(testCase.getStatus(), response.getStatus());
        verify(caseRepository).save(any(Case.class));
    }

    @Test
    void shouldGetAllCases() {
        when(caseRepository.findAll()).thenReturn(List.of(testCase));

        List<CaseResponse> responses = caseService.getAllCases();

        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals(testCase.getCaseNumber(), responses.get(0).getCaseNumber());
        assertEquals(testCase.getStatus(), responses.get(0).getStatus());
    }

    @Test
    void shouldThrowDuplicateCaseExceptionWhenCaseNumberExists() {
        when(caseRepository.existsByCaseNumber(anyString())).thenReturn(true);

        assertThrows(DuplicateCaseException.class, () ->
            caseService.createCase(testRequest));
    }

    @Test
    void shouldThrowCaseNotFoundExceptionWhenCaseDoesNotExist() {
        when(caseRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CaseNotFoundException.class, () ->
            caseService.getCaseById(1L));
    }

    @Test
    void shouldUpdateCaseSuccessfully() {
        when(caseRepository.findById(1L)).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(Case.class))).thenReturn(testCase);

        CaseResponse response = caseService.updateCase(1L, testRequest);

        assertNotNull(response);
        assertEquals(testCase.getCaseNumber(), response.getCaseNumber());
        assertEquals(testCase.getStatus(), response.getStatus());
        verify(caseRepository).save(any(Case.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithDuplicateCaseNumber() {
        Case existingCase = new Case();
        existingCase.setId(1L);
        existingCase.setCaseNumber("OLD123");
        existingCase.setStatus(CaseStatus.NEW);

        CaseRequest updateRequest = new CaseRequest();
        updateRequest.setCaseNumber("NEW123");
        updateRequest.setTitle("Test Case");
        updateRequest.setStatus(CaseStatus.IN_PROGRESS);

        when(caseRepository.findById(1L)).thenReturn(Optional.of(existingCase));
        when(caseRepository.existsByCaseNumber("NEW123")).thenReturn(true);

        assertThrows(DuplicateCaseException.class, () ->
            caseService.updateCase(1L, updateRequest));
    }

    @Test
    void shouldHandleNullStatus() {
        testRequest.setStatus(null);

        assertThrows(IllegalArgumentException.class, () ->
            caseService.createCase(testRequest));
    }

    @Test
    void shouldUpdateCaseWithDifferentStatus() {
        Case existingCase = new Case();
        existingCase.setId(1L);
        existingCase.setCaseNumber("TEST123");
        existingCase.setStatus(CaseStatus.NEW);

        CaseRequest updateRequest = new CaseRequest();
        updateRequest.setCaseNumber("TEST123");
        updateRequest.setTitle("Updated Case");
        updateRequest.setStatus(CaseStatus.IN_PROGRESS);

        Case updatedCase = new Case();
        updatedCase.setId(1L);
        updatedCase.setCaseNumber("TEST123");
        updatedCase.setStatus(CaseStatus.IN_PROGRESS);

        when(caseRepository.findById(1L)).thenReturn(Optional.of(existingCase));
        when(caseRepository.save(any(Case.class))).thenReturn(updatedCase);

        CaseResponse response = caseService.updateCase(1L, updateRequest);

        assertEquals(CaseStatus.IN_PROGRESS, response.getStatus());
        verify(caseRepository).save(any(Case.class));
    }

    @Test
    void shouldHandleAllStatusTransitions() {
        testCase.setStatus(CaseStatus.NEW);
        when(caseRepository.findById(1L)).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(Case.class))).thenReturn(testCase);

        // Test all possible status transitions
        for (CaseStatus status : CaseStatus.values()) {
            testRequest.setStatus(status);
            CaseResponse response = caseService.updateCase(1L, testRequest);
            assertEquals(status, response.getStatus());
        }
    }
}
