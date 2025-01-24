package uk.gov.hmcts.reform.dev.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.dev.dto.CaseRequest;
import uk.gov.hmcts.reform.dev.dto.CaseResponse;
import uk.gov.hmcts.reform.dev.services.CaseService;

import java.util.List;

@RestController
@RequestMapping("/cases")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Case Management", description = "APIs for managing legal cases")
@Validated
public class CaseController {

    private final CaseService caseService;

    @GetMapping("/{id}")
    @Operation(summary = "Get case by ID", description = "Retrieves a specific case by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Case found"),
        @ApiResponse(responseCode = "404", description = "Case not found"),
        @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<CaseResponse> getCaseById(
        @Parameter(description = "ID of the case", required = true) @PathVariable Long id) {
        log.debug("Received request to get case with id: {}", id);
        return ResponseEntity.ok(caseService.getCaseById(id));
    }

    @GetMapping
    @Operation(summary = "Get all cases", description = "Retrieves all cases")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<CaseResponse>> getAllCases() {
        log.debug("Received request to get all cases");
        return ResponseEntity.ok(caseService.getAllCases());
    }

    @PostMapping
    @Operation(summary = "Create new case", description = "Creates a new case")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Case created successfully"),
        @ApiResponse(responseCode = "409", description = "Case number already exists"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CaseResponse> createCase(@Valid @RequestBody CaseRequest caseRequest) {
        log.debug("Received request to create case with number: {}", caseRequest.getCaseNumber());
        return ResponseEntity.ok(caseService.createCase(caseRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete case", description = "Deletes an existing case")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Case deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Case not found"),
        @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCase(
        @Parameter(description = "ID of the case to delete", required = true) @PathVariable Long id) {
        log.debug("Received request to delete case with id: {}", id);
        caseService.deleteCase(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update case", description = "Updates an existing case")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Case updated successfully"),
        @ApiResponse(responseCode = "404", description = "Case not found"),
        @ApiResponse(responseCode = "409", description = "Case number already exists"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CaseResponse> updateCase(
        @Parameter(description = "ID of the case to update", required = true) @PathVariable Long id,
        @Valid @RequestBody CaseRequest caseRequest) {
        log.debug("Received request to update case with id: {}", id);
        return ResponseEntity.ok(caseService.updateCase(id, caseRequest));
    }
}
