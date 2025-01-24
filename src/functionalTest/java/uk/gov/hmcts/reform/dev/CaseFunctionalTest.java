package uk.gov.hmcts.reform.dev;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.dev.dto.AuthRequest;
import uk.gov.hmcts.reform.dev.dto.CaseRequest;
import uk.gov.hmcts.reform.dev.enums.CaseStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CaseFunctionalTest {

    private String authToken;
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 4000;
        authToken = getAuthToken();
    }

    private String getAuthToken() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(ADMIN_USERNAME);
        authRequest.setPassword(ADMIN_PASSWORD);

        return given()
            .contentType(ContentType.JSON)
            .body(authRequest)
            .when()
            .post("/auth/login")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getString("token");
    }

    @Test
    @Order(1)
    void shouldPerformFullCaseLifecycle() {
        // Create case
        String caseId = createCase("FUNC123", "Initial Case");

        // Update case
        updateCase(caseId, "FUNC123", "Updated Case", CaseStatus.IN_PROGRESS);

        // Verify update
        verifyCase(caseId, "Updated Case", CaseStatus.IN_PROGRESS);

        // Delete case
        deleteCase(caseId);

        // Verify deletion
        verifyCaseNotFound(caseId);
    }

    @Test
    @Order(2)
    void shouldHandleDuplicateCaseNumber() {
        // Create first case
        createCase("DUPLICATE123", "First Case");

        // Attempt to create case with same number
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(createCaseRequest("DUPLICATE123", "Second Case", CaseStatus.NEW))
            .when()
            .post("/cases")
            .then()
            .statusCode(409);
    }

    @Test
    @Order(3)
    void shouldHandleInvalidStatus() {
        CaseRequest request = new CaseRequest();
        request.setCaseNumber("STATUS123");
        request.setTitle("Invalid Status Case");

        // This will now be handled by validation before even trying to convert to enum
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(request) // Status is null, should trigger validation
            .when()
            .post("/cases")
            .then()
            .statusCode(400);
    }

    @Test
    @Order(4)
    void shouldHandleStatusTransitions() {
        String caseId = createCase("TRANS123", "Status Transition Case");

        // NEW -> IN_PROGRESS
        updateCase(caseId, "TRANS123", "In Progress Case", CaseStatus.IN_PROGRESS);
        verifyCase(caseId, "In Progress Case", CaseStatus.IN_PROGRESS);

        // IN_PROGRESS -> ON_HOLD
        updateCase(caseId, "TRANS123", "On Hold Case", CaseStatus.ON_HOLD);
        verifyCase(caseId, "On Hold Case", CaseStatus.ON_HOLD);

        // ON_HOLD -> RESOLVED
        updateCase(caseId, "TRANS123", "Resolved Case", CaseStatus.RESOLVED);
        verifyCase(caseId, "Resolved Case", CaseStatus.RESOLVED);

        // RESOLVED -> CLOSED
        updateCase(caseId, "TRANS123", "Closed Case", CaseStatus.CLOSED);
        verifyCase(caseId, "Closed Case", CaseStatus.CLOSED);
    }

    private String createCase(String caseNumber, String title) {
        return given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(createCaseRequest(caseNumber, title, CaseStatus.NEW))
            .when()
            .post("/cases")
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .extract()
            .jsonPath()
            .getString("id");
    }

    private void updateCase(String caseId, String caseNumber, String title, CaseStatus status) {
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(createCaseRequest(caseNumber, title, status))
            .when()
            .put("/cases/" + caseId)
            .then()
            .statusCode(200)
            .body("title", equalTo(title))
            .body("status", equalTo(status.name()));
    }

    private void verifyCase(String caseId, String expectedTitle, CaseStatus expectedStatus) {
        Response response = given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .when()
            .get("/cases/" + caseId)
            .then()
            .statusCode(200)
            .extract()
            .response();

        assertEquals(expectedTitle, response.jsonPath().getString("title"));
        assertEquals(expectedStatus.name(), response.jsonPath().getString("status"));
    }

    private void deleteCase(String caseId) {
        given()
            .header("Authorization", "Bearer " + authToken)
            .when()
            .delete("/cases/" + caseId)
            .then()
            .statusCode(204);
    }

    private void verifyCaseNotFound(String caseId) {
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .when()
            .get("/cases/" + caseId)
            .then()
            .statusCode(404);
    }

    private CaseRequest createCaseRequest(String caseNumber, String title, CaseStatus status) {
        CaseRequest request = new CaseRequest();
        request.setCaseNumber(caseNumber);
        request.setTitle(title);
        request.setDescription("Test Description");
        request.setStatus(status);
        return request;
    }
}
