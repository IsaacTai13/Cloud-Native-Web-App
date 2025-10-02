package com.isaactai.cloudnativeweb.support;

import io.restassured.RestAssured;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author tisaac
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("ci")
public abstract class BaseApiTest {

    @LocalServerPort
    int port;

    @BeforeAll
    public void setupRestAssured() {
        RestAssured.baseURI = System.getProperty("api.base", "http://localhost");
        RestAssured.port = port;
        RestAssured.basePath = "";
    }
}
