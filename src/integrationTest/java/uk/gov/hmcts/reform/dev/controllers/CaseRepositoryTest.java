package uk.gov.hmcts.reform.dev.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import uk.gov.hmcts.reform.dev.enums.CaseStatus;
import uk.gov.hmcts.reform.dev.models.Case;
import uk.gov.hmcts.reform.dev.repositories.CaseRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
class CaseRepositoryTest {

    @Autowired
    private CaseRepository caseRepository;

    @Test
    void shouldSaveCase() {
        Case testCase = new Case();
        testCase.setCaseNumber("TEST123");
        testCase.setTitle("Test Case");
        testCase.setDescription("Test Description");
        testCase.setStatus(CaseStatus.NEW);
        testCase.setCreatedDate(LocalDateTime.now());

        Case savedCase = caseRepository.save(testCase);

        assertNotNull(savedCase.getId());
        assertEquals("TEST123", savedCase.getCaseNumber());
    }

    @Test
    void shouldCheckCaseNumberExists() {
        // Given
        Case testCase = new Case();
        testCase.setCaseNumber("UNIQUE123");
        testCase.setTitle("Test Case");
        testCase.setStatus(CaseStatus.NEW);
        testCase.setCreatedDate(LocalDateTime.now());
        caseRepository.save(testCase);

        // When & Then
        assertTrue(caseRepository.existsByCaseNumber("UNIQUE123"));
        assertFalse(caseRepository.existsByCaseNumber("NONEXISTENT"));
    }

    @Test
    void shouldFindAllCases() {
        // Given
        Case case1 = createTestCase("CASE1", CaseStatus.NEW);
        Case case2 = createTestCase("CASE2", CaseStatus.IN_PROGRESS);
        caseRepository.saveAll(List.of(case1, case2));

        // When
        List<Case> cases = caseRepository.findAll();

        // Then
        assertEquals(2, cases.size());
    }

    @Test
    void shouldFindCasesByStatus() {
        // Given
        Case case1 = createTestCase("CASE1", CaseStatus.NEW);
        Case case2 = createTestCase("CASE2", CaseStatus.IN_PROGRESS);
        Case case3 = createTestCase("CASE3", CaseStatus.NEW);
        caseRepository.saveAll(List.of(case1, case2, case3));

        // When
        List<Case> newCases = caseRepository.findByStatus(CaseStatus.NEW);

        // Then
        assertEquals(2, newCases.size());
        newCases.forEach(c -> assertEquals(CaseStatus.NEW, c.getStatus()));
    }

    @Test
    void shouldDeleteCase() {
        // Given
        Case testCase = createTestCase("DELETE123", CaseStatus.NEW);
        Case savedCase = caseRepository.save(testCase);

        // When
        caseRepository.deleteById(savedCase.getId());

        // Then
        Optional<Case> deletedCase = caseRepository.findById(savedCase.getId());
        assertTrue(deletedCase.isEmpty());
    }

    private Case createTestCase(String caseNumber, CaseStatus status) {
        Case testCase = new Case();
        testCase.setCaseNumber(caseNumber);
        testCase.setTitle("Test Case");
        testCase.setStatus(status);
        testCase.setCreatedDate(LocalDateTime.now());
        return testCase;
    }
}
