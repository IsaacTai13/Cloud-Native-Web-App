package com.isaactai.cloudnativeweb;

import com.isaactai.cloudnativeweb.support.BaseApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("ci")
class CloudNativeWebApplicationTests extends BaseApiTest {

    @Test
    void contextLoads() {
    }

}
