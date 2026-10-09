package ru.yandex.practicum;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import ru.yandex.practicum.config.WebConfiguration;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WebConfiguration.class)
@WebAppConfiguration
@TestPropertySource("classpath:test-application.properties")
@Sql("classpath:cleanup.sql")
public abstract class AbstractIntegrationTest {
}
