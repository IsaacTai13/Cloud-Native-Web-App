package com.isaactai.cloudnativeweb.user;

import com.isaactai.cloudnativeweb.support.BaseApiTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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

    String username;
    String first;
    String last;
    String pwd;
    Long userId;
    String createdAt;

    @BeforeEach
    void createUserFixed() {
        username = "people" + System.currentTimeMillis() + "+ts@example.com";
        pwd = "StrongPwd$";
        String userJson = userJson("people", "p", username, pwd);

        Response res = given()
                .contentType("application/json")
                .body(userJson)
                .when()
                .post("/v1/user")
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .extract().response();

        userId = res.jsonPath().getLong("id");
        first = res.jsonPath().getString("first_name");
        last = res.jsonPath().getString("last_name");
        createdAt = res.jsonPath().getString("account_created");
    }

    @Nested
    class CreateUser {
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
            String userJson = userJson(first, last, username, "StrongPwd$");

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

    @Nested
    class RetrieveUser {

        @Test
        void getUserById_success() {
            given()
                    .auth().preemptive().basic(username, pwd)
                    .accept("application/json")
                    .when()
                    .get("/v1/user/{id}", userId)
                    .then()
                    .log().ifValidationFails()
                    .statusCode(200)
                    .body("id", equalTo(userId.intValue()))
                    .body("username", equalTo(username))
                    .body("first_name", notNullValue())
                    .body("last_name", notNullValue())
                    .body("$", not(hasKey("password")))
                    .body("account_created", notNullValue())
                    .body("account_updated", notNullValue());
        }
    }

    @Nested
    class UpdateUser {
        @Test
        void updateUser_fullPut_success() {
            String userJson = """
                {
                  "first_name": "%s",
                  "last_name": "%s",
                  "password": "%s"
                }
                """.formatted("newFirst", "newLast", "newStrongPwd$");

            given()
                    .contentType("application/json")
                    .auth().preemptive().basic(username, pwd)
                    .body(userJson)
                    .when()
                    .put("/v1/user/{id}", userId)
                    .then()
                    .log().ifValidationFails()
                    .statusCode(204);
        }
    }

    private static String userJson(String first, String last, String email, String pwd) {
        return """
        {
          "first_name": "%s",
          "last_name": "%s",
          "username": "%s",
          "password": "%s"
        }
        """.formatted(first, last, email, pwd);
    }
}
