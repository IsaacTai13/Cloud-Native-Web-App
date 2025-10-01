package com.isaactai.cloudnativeweb.product;

import com.isaactai.cloudnativeweb.support.BaseApiTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * @author tisaac
 */
public class ProductControllerTest extends BaseApiTest {

    String username;
    String pwd;

    @BeforeEach
    void setUpUser() {
        long ts = System.currentTimeMillis();
        username = "first" + ts + "@example.com";
        pwd = "StrongPwd$";

        String userJson = """
            {
              "first_name": "%s",
              "last_name": "%s",
              "username": "%s",
              "password": "%s"
            }
            """.formatted("first", "last", username, pwd);

        given()
                .contentType("application/json").accept("application/json")
                .body(userJson)
                .when()
                .post("/v1/user")
                .then()
                .statusCode(201)
                .body("username", equalTo(username));
    }

    @Test
    void createProduct_success() {
        String productJson = """
            {
              "name": "%s",
              "description": "%s",
              "sku": "%s",
              "manufacturer": "Apple",
              "quantity": 5
            }
            """.formatted("MacBook Pro", "16-inch, M2 Pro", "sku-" + System.currentTimeMillis());

        given()
                .auth().basic(username, pwd)
                .contentType("application/json")
                .body(productJson)
                .when()
                .post("/v1/product")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("MacBook Pro"))
                .body("sku", startsWith("sku-"))
                .body("manufacturer", equalTo("Apple"))
                .body("quantity", equalTo(5))
                .body("owner_user_id", notNullValue());

    }
}
