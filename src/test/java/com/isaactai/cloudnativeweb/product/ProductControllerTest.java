package com.isaactai.cloudnativeweb.product;

import com.isaactai.cloudnativeweb.support.BaseApiTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * @author tisaac
 */
public class ProductControllerTest extends BaseApiTest {

    String username;
    String pwd;
    Long userId;
    Long productId;

    @BeforeEach
    void setUpUser() {
        // create user
        long ts = System.currentTimeMillis();
        username = "first" + ts + "@example.com";
        pwd = "StrongPwd$";

        String bodyJson = userJson("first", "last", username, pwd);

        Response res = given()
                .contentType("application/json").accept("application/json")
                .body(bodyJson)
                .when()
                .post("/v1/user")
                .then()
                .statusCode(201)
                .body("username", equalTo(username))
                .extract().response();

        userId = res.jsonPath().getLong("id");

        // create product
        bodyJson = productJson("MacBook Pro 14", "14-inch, M3 Pro",
                "sku", "Apple", 2);

        res = given()
                .auth().basic(username, pwd)
                .contentType("application/json")
                .body(bodyJson)
                .when()
                .post("/v1/product")
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .body("id", notNullValue())
                .body("owner_user_id", equalTo(userId.intValue()))
                .extract().response();

        productId = res.jsonPath().getLong("id");
    }

    @Nested
    class CreateProduct {
        @Test
        void createProduct_success() {
            String bodyJson = productJson("MacBook Pro", "16-inch, M2 Pro",
                    "sku", "Apple", 5);

            given()
                    .auth().basic(username, pwd)
                    .contentType("application/json")
                    .body(bodyJson)
                    .when()
                    .post("/v1/product")
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("name", equalTo("MacBook Pro"))
                    .body("sku", startsWith("sku-"))
                    .body("manufacturer", equalTo("Apple"))
                    .body("quantity", equalTo(5))
                    .body("owner_user_id", equalTo(userId.intValue()));
        }
    }

    @Nested
    class RetrieveProduct {
        @Test
        void getProductById_success() {
            given()
                    .when()
                    .accept("application/json")
                    .get("/v1/product/{id}", productId)
                    .then()
                    .log().ifValidationFails()
                    .statusCode(200)
                    .body("id", equalTo(productId.intValue()))
                    .body("name", equalTo("MacBook Pro 14"));
        }
    }

    @Nested
    class UpdateProduct {
        @Test
        void updateProduct_fullPut_success() {
            String prodName = "Iphone 17 pro";
            String newProductJson = productJson(prodName, "ios 26",
                    "sku", "Apple", 10);

            given()
                    .auth().basic(username, pwd)
                    .contentType("application/json")
                    .body(newProductJson)
                    .when()
                    .put("/v1/product/{id}", productId)
                    .then()
                    .log().ifValidationFails()
                    .statusCode(204);

            given()
                    .accept("application/json")
                    .when()
                    .get("/v1/product/{id}", productId)
                    .then()
                    .log().ifValidationFails()
                    .statusCode(200)
                    .body("id", equalTo(productId.intValue()))
                    .body("name", equalTo(prodName));
        }

        @Test
        void updateProduct_partialPatch_quantity_only_success() {
            int quantity = 17;
            String patchJson = """
                    {
                        "quantity": %s
                    }""".formatted(quantity);

            given()
                    .auth().basic(username, pwd)
                    .contentType("application/json")
                    .body(patchJson)
                    .when()
                    .patch("/v1/product/{id}", productId)
                    .then()
                    .log().ifValidationFails()
                    .statusCode(204);

            given()
                    .accept("application/json")
                    .when()
                    .get("/v1/product/{id}", productId)
                    .then()
                    .log().ifValidationFails()
                    .statusCode(200)
                    .body("id", equalTo(productId.intValue()))
                    .body("quantity", equalTo(quantity));
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

    private static String productJson(String name, String desc, String sku, String manufa, int quantity) {
        return """
                {
                  "name": "%s",
                  "description": "%s",
                  "sku": "%s",
                  "manufacturer": "%s",
                  "quantity": %s
                }
                """.formatted(name, desc, sku + "-" + System.currentTimeMillis(), manufa, quantity);
    }
}
