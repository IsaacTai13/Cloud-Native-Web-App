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
                null, "Apple", 2);

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
                    null, "Apple", 5);

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

        @Test
        void createProduct_invalidQuantity_returns400() {
            String json = productJson("BadProduct", "Invalid Quantity", null, "Microsoft", 111);

            given()
                    .auth().basic(username, pwd)
                    .contentType("application/json")
                    .body(json)
                    .when()
                    .post("/v1/product")
                    .then()
                    .statusCode(400);
        }

        @Test
        void createProduct_duplicateSku_returns400() {
            String dupSku = "sku-" + System.currentTimeMillis();
            String body1 = productJson("BadProduct", "Invalid Quantity", dupSku, "Microsoft", 11);
            given().auth().basic(username, pwd)
                    .contentType("application/json").body(body1)
                    .when().post("/v1/product")
                    .then().statusCode(201);

            String body2 = productJson("iPhone2", "Another", dupSku, "Apple", 2);
            given().auth().basic(username, pwd)
                    .contentType("application/json").body(body2)
                    .when().post("/v1/product")
                    .then().statusCode(400);
        }
    }

    @Nested
    class RetrieveProduct {
        @Test
        void getProductById_success() {
            given()
                    .accept("application/json")
                    .when()
                    .get("/v1/product/{id}", productId)
                    .then()
                    .log().ifValidationFails()
                    .statusCode(200)
                    .body("id", equalTo(productId.intValue()))
                    .body("name", equalTo("MacBook Pro 14"));
        }

        @Test
        void getProductById_withInvalidAuth_returns401() {
            given()
                    .auth().preemptive().basic(username, "wrongPwd")
                    .accept("application/json")
                    .when()
                    .get("/v1/product/{id}", productId)
                    .then()
                    .statusCode(401);
        }

        @Test
        void getProductById_withValidAuth_returns403() {
            given()
                    .auth().preemptive().basic(username, pwd)
                    .accept("application/json")
                    .when()
                    .get("/v1/product/{id}", productId)
                    .then()
                    .statusCode(403);
        }
    }

    @Nested
    class UpdateProduct {
        @Test
        void updateProduct_fullPut_success() {
            String prodName = "Iphone 17 pro";
            String newProductJson = productJson(prodName, "ios 26",
                    null, "Apple", 10);

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

        @Test
        void updateProduct_withoutAuth_returns401() {
            String body = productJson("NoAuth", "No Token", "", "Apple", 1);

            given()
                    .contentType("application/json")
                    .body(body)
                    .when()
                    .put("/v1/product/{id}", productId)
                    .then()
                    .log().ifValidationFails()
                    .statusCode(401);
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
        String finalSku = (sku == null || sku.isEmpty())
                ? "sku-" + System.currentTimeMillis()
                : sku;

        return """
                {
                  "name": "%s",
                  "description": "%s",
                  "sku": "%s",
                  "manufacturer": "%s",
                  "quantity": %s
                }
                """.formatted(name, desc, finalSku, manufa, quantity);
    }
}
