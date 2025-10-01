package com.isaactai.cloudnativeweb.health;

import com.isaactai.cloudnativeweb.support.BaseApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * @author tisaac
 */
public class HealthControllerTest extends BaseApiTest {

    @Test
    @DisplayName("Get /healthz -> 200 with empty body, no headers, no query")
    void healthz_ok_noBody_noQuery_noAuth() {
        var resp = given()
                .when()
                    .get("/healthz")
                .then()
                    .statusCode(200)
                    .body(is(emptyString()));
    }
}
