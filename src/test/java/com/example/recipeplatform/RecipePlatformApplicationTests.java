package com.example.recipeplatform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true")
class RecipePlatformApplicationTests {

    @Test
    void contextLoads() {
        // Spring fails this test during context bootstrap if the application cannot start.
    }

}
