package com.isaactai.cloudnativeweb.user;

import com.isaactai.cloudnativeweb.support.BaseApiTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author tisaac
 */
public class UserControllerTest extends BaseApiTest {

    static Stream<Arguments> validUsers() {
        return Stream.of(
                Arguments.of("wen", "lin", "wenlin"),
                Arguments.of("chalot", "wu", "chalot"),
                Arguments.of("Adom", "chin", "adomchin")
        );
    }

    @ParameterizedTest(name = "[{index}] {0} {1} -> {2}+ts@example.com")
    @MethodSource("validUsers")
    void createUser_success_variousInput(String first, String last, String usernamePrefix) {
        String username = usernamePrefix + System.currentTimeMillis() + "+ts@example.com";
        String userJson = """
            {
              "first_name": "%s",
              "last_name": "%s",
              "username": "%s",
              "password": "%s"
            }
            """.formatted(first, last, username, "StrongPwd$");

        Response res = given()
                .contentType("application/json")
                .body(userJson)
                .when()
                .post("/v1/user")
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .body("id", notNullValue())
                .body("username", matchesRegex("^[a-zA-Z0-9+_.-]+@(.+)$"))
                .body("first_name", equalTo(first))
                .body("last_name", equalTo(last))
                .body("account_created", notNullValue())
                .body("account_updated", notNullValue())
                .body("$", not(hasKey("password")))
                .extract()
                .response();

        // check create_time is equal to update_time
        String created = res.jsonPath().getString("account_created");
        String updated = res.jsonPath().getString("account_updated");
        assertThat("account_created should equal account_updated on first creation",
                updated, equalTo(created));
    }
}
