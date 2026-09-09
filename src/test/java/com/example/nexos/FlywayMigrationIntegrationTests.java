package com.example.nexos;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class FlywayMigrationIntegrationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateInitialSchemaWithFlyway() {
        Integer clientTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'TB_CLIENTES'", Integer.class);
        Integer serviceOrderTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'TB_ORDENS_SERVICO'", Integer.class);
        Integer statusHistoryTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'TB_HISTORICO_STATUS_ORDEM_SERVICO'",
                Integer.class);

        assertThat(clientTableCount).isEqualTo(1);
        assertThat(serviceOrderTableCount).isEqualTo(1);
        assertThat(statusHistoryTableCount).isEqualTo(1);
    }

}
