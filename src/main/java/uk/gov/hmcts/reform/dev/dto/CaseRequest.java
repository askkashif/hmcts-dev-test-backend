package uk.gov.hmcts.reform.dev.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import uk.gov.hmcts.reform.dev.enums.CaseStatus;

@Data
@Schema(description = "Request object for creating or updating a case")
public class CaseRequest {

    @NotBlank(message = "Case number is required")
    @Pattern(regexp = "^[A-Z0-9]{2,20}$", message = "Case number must be 2-20 characters"
        + " long and contain only uppercase letters and numbers")
    @Schema(description = "Unique identifier for the case", example = "ABC123")
    private String caseNumber;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    @Schema(description = "Title of the case", example = "Property Dispute Case")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Detailed description of the case", example = "Property dispute between tenant and landlord")
    private String description;

    @NotNull(message = "Status is required")
    @Schema(description = "Current status of the case", example = "NEW")
    private CaseStatus status;
}
