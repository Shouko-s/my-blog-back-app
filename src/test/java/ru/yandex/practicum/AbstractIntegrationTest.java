package ru.yandex.practicum;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@TestPropertySource("classpath:test-application.properties")
@Sql("classpath:cleanup.sql")
public abstract class AbstractIntegrationTest {
}
