package uk.gov.hmcts.reform.dev;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.dev.dto.CaseRequest;
import uk.gov.hmcts.reform.dev.enums.CaseStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class CasesSmokeTest {
    protected static final String CONTENT_TYPE_VALUE = "application/json";

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 4000;
    }

    @Test
    void shouldGetAllCases() {
        given()
            .contentType(ContentType.JSON)
            .when()
            .get("/cases")
            .then()
            .statusCode(200);
    }

    @Test
    void shouldCreateNewCase() {
        CaseRequest request = new CaseRequest();
        request.setCaseNumber("SMOKE123");
        request.setTitle("Smoke Test Case");
        request.setDescription("Smoke Test Description");
        request.setStatus(CaseStatus.NEW);

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/cases")
            .then()
            .statusCode(200)
            .body("id", notNullValue());
    }

    @Test
    void shouldHandleInvalidStatus() {
        String requestBody = """
            {
                "caseNumber": "SMOKE123",
                "title": "Smoke Test Case",
                "description": "Smoke Test Description",
                "status": "INVALID_STATUS"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/cases")
            .then()
            .statusCode(400);
    }
}
