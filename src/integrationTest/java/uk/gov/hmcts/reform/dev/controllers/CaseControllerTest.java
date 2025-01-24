package uk.gov.hmcts.reform.dev.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.dev.dto.CaseRequest;
import uk.gov.hmcts.reform.dev.dto.CaseResponse;
import uk.gov.hmcts.reform.dev.enums.CaseStatus;
import uk.gov.hmcts.reform.dev.exceptions.CaseNotFoundException;
import uk.gov.hmcts.reform.dev.exceptions.DuplicateCaseException;
import uk.gov.hmcts.reform.dev.security.JwtUtil;
import uk.gov.hmcts.reform.dev.services.CaseService;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CaseController.class)
@Import({CaseControllerTestConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class CaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CaseService caseService;

    @Autowired
    private JwtUtil jwtUtil;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldGetAllCases() throws Exception {
        CaseResponse mockCase = CaseResponse.builder()
            .id(1L)
            .caseNumber("TEST123")
            .title("Test Case")
            .status(CaseStatus.NEW) // Added status
            .createdDate(LocalDateTime.now())
            .build();

        when(caseService.getAllCases()).thenReturn(Collections.singletonList(mockCase));

        mockMvc.perform(get("/cases")
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].caseNumber").value("TEST123"))
            .andExpect(jsonPath("$[0].status").value(CaseStatus.NEW.name()));
    }

    @Test
    void shouldGetCaseById() throws Exception {
        CaseResponse mockCase = CaseResponse.builder()
            .id(1L)
            .caseNumber("TEST123")
            .title("Test Case")
            .status(CaseStatus.NEW) // Added status
            .createdDate(LocalDateTime.now())
            .build();

        when(caseService.getCaseById(eq(1L))).thenReturn(mockCase);

        mockMvc.perform(get("/cases/1")
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.caseNumber").value("TEST123"))
            .andExpect(jsonPath("$.status").value(CaseStatus.NEW.name()));
    }

    @Test
    void shouldCreateCase() throws Exception {
        CaseRequest request = new CaseRequest();
        request.setCaseNumber("TEST123");
        request.setTitle("Test Case");
        request.setStatus(CaseStatus.NEW);  // Added status

        CaseResponse mockCase = CaseResponse.builder()
            .id(1L)
            .caseNumber("TEST123")
            .title("Test Case")
            .status(CaseStatus.NEW)
            .createdDate(LocalDateTime.now())
            .build();

        when(caseService.createCase(any(CaseRequest.class))).thenReturn(mockCase);

        mockMvc.perform(post("/cases")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.caseNumber").value("TEST123"))
            .andExpect(jsonPath("$.status").value(CaseStatus.NEW.name()));
    }

    @Test
    void shouldUpdateCase() throws Exception {
        CaseRequest request = new CaseRequest();
        request.setCaseNumber("TEST123");
        request.setTitle("Updated Test Case");
        request.setDescription("Updated Description");
        request.setStatus(CaseStatus.IN_PROGRESS);

        CaseResponse mockCase = CaseResponse.builder()
            .id(1L)
            .caseNumber("TEST123")
            .title("Updated Test Case")
            .description("Updated Description")
            .status(CaseStatus.IN_PROGRESS)
            .createdDate(LocalDateTime.now())
            .build();

        when(caseService.updateCase(eq(1L), any(CaseRequest.class))).thenReturn(mockCase);

        mockMvc.perform(put("/cases/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Updated Test Case"))
            .andExpect(jsonPath("$.status").value(CaseStatus.IN_PROGRESS.name()));
    }

    @Test
    void shouldReturnBadRequestForInvalidStatus() throws Exception {
        // Create a JSON string directly to test invalid enum value
        String requestJson = """
            {
                "caseNumber": "TEST123",
                "title": "Test Case",
                "status": "INVALID_STATUS"
            }
            """;

        mockMvc.perform(post("/cases")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundForNonExistentCase() throws Exception {
        when(caseService.getCaseById(999L))
            .thenThrow(new CaseNotFoundException(999L));

        mockMvc.perform(get("/cases/999")
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnConflictForDuplicateCaseNumber() throws Exception {
        CaseRequest request = new CaseRequest();
        request.setCaseNumber("DUPLICATE123");
        request.setTitle("Test Case");
        request.setStatus(CaseStatus.NEW);

        when(caseService.createCase(any(CaseRequest.class)))
            .thenThrow(new DuplicateCaseException("Case number already exists"));

        mockMvc.perform(post("/cases")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnBadRequestForMissingStatus() throws Exception {
        CaseRequest request = new CaseRequest();
        request.setCaseNumber("TEST123");
        request.setTitle("Test Case");
        // status is deliberately not set

        mockMvc.perform(post("/cases")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
