package com.eazybytes.accounts;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
public class AccountsIntegrationTest_rest_assured {

    static {
        // Base URI for REST Assured
        RestAssured.baseURI = "http://localhost:8080/api";
    }

    @Test
    void testCreateAccount_Successs() {
        String requestBody = """
            {
                "name": "John Doe",
                "mobileNumber": "1234567889",
                "email": "johndoe@example.com"
            }
        """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/create")
        .then()
            .statusCode(201)
            .body("statusCode", equalTo("201"))
            .body("statusMsg", equalTo("Account created successfully"));
    }
    @Test
    void testCreateAccount_Failure_InvalidInput() {
        String requestBody = """
            {
                "name": "",
                "mobileNumber": "12345",  // Invalid mobile number
                "email": "invalidemail"
            }
        """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/create")
        .then()
            .statusCode(400) // Validation failure
            .body("Mobile number must be 10 digits", hasKey("mobileNumber"))
            .body("errors", hasKey("email"));
    }
    
    @Test
    void testCreateAccount_Failure_InvalidInputt() {
        // Define invalid parameters
        String invalidName = "";
        String invalidMobileNumber = "12345"; // Invalid mobile number (less than 10 digits)
        String invalidEmail = "invalidemail"; // Malformed email

        // Use JsonPath.param(...) to dynamically insert parameters
        String requestBody = """
            {
                "name": "%s",
                "mobileNumber": "%s",
                "email": "%s"
            }
        """.formatted(invalidName, invalidMobileNumber, invalidEmail);

        given()
            .contentType(ContentType.JSON)
            .body(requestBody) // Pass the dynamically created request body
        .when()
            .post("/create")
        .then()
            .statusCode(400) // Expecting a validation error
            .body("errors.mobileNumber", notNullValue()) // Validate the presence of an error key for mobileNumber
            .body("errors.email", notNullValue()); // Validate the presence of an error key for email
    }


    @Test
    void testFetchAccountDetails_Success() {
        given()
            .queryParam("mobileNumber", "1234567890")
        .when()
            .get("/fetch")
        .then()
            .statusCode(200)
            .body("name", equalTo("John Doe"))
            .body("mobileNumber", equalTo("1234567890"));
    }

    @Test
    void testFetchAccountDetails_Failure_InvalidMobileNumber() {
        given()
            .queryParam("mobileNumber", "12345")
        .when()
            .get("/fetch")
        .then()
            .statusCode(400) // Validation error
            .body("errors", hasKey("mobileNumber"));
    }

    @Test
    void testUpdateAccountDetails_Success() {
        String requestBody = """
            {
                "name": "John Smith",
                "mobileNumber": "1234567890",
                "email": "johnsmith@example.com"
            }
        """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .put("/update")
        .then()
            .statusCode(200)
            .body("status", equalTo("200"))
            .body("message", equalTo("Account successfully updated."));
    }

    @Test
    void testUpdateAccountDetails_Failure_ExpectationFailed() {
        String requestBody = """
            {
                "name": "Nonexistent User",
                "mobileNumber": "0000000000",
                "email": "nonexistent@example.com"
            }
        """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .put("/update")
        .then()
            .statusCode(417) // Expectation failed
            .body("status", equalTo("417"))
            .body("message", equalTo("Account update failed."));
    }

    @Test
    void testDeleteAccountDetails_Success() {
        given()
            .queryParam("mobileNumber", "1234567890")
        .when()
            .delete("/delete")
        .then()
            .statusCode(200)
            .body("status", equalTo("200"))
            .body("message", equalTo("Account successfully deleted."));
    }

    @Test
    void testDeleteAccountDetails_Failure_ExpectationFailed() {
        given()
            .queryParam("mobileNumber", "0000000000")
        .when()
            .delete("/delete")
        .then()
            .statusCode(417) // Expectation failed
            .body("status", equalTo("417"))
            .body("message", equalTo("Account deletion failed."));
    }
}
