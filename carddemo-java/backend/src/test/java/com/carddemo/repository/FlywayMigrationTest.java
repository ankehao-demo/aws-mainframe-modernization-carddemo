package com.carddemo.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FlywayMigrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void allMigrationsRunCleanly() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            List<String> tableNames = new ArrayList<>();
            while (tables.next()) {
                tableNames.add(tables.getString("TABLE_NAME").toUpperCase());
            }

            assertThat(tableNames).contains("USERS");
            assertThat(tableNames).contains("ACCOUNTS");
            assertThat(tableNames).contains("CUSTOMERS");
            assertThat(tableNames).contains("CARDS");
            assertThat(tableNames).contains("CARD_XREF");
            assertThat(tableNames).contains("TRANSACTIONS");
            assertThat(tableNames).contains("DAILY_TRANSACTIONS");
            assertThat(tableNames).contains("TRAN_CAT_BALANCES");
            assertThat(tableNames).contains("DISCLOSURE_GROUPS");
            assertThat(tableNames).contains("TRAN_TYPES");
            assertThat(tableNames).contains("TRAN_CATEGORIES");
        }
    }

    @Test
    void seedDataLoaded() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            var stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            rs.next();
            assertThat(rs.getInt(1)).isGreaterThanOrEqualTo(3);

            rs = stmt.executeQuery("SELECT COUNT(*) FROM tran_types");
            rs.next();
            assertThat(rs.getInt(1)).isGreaterThanOrEqualTo(5);

            rs = stmt.executeQuery("SELECT COUNT(*) FROM tran_categories");
            rs.next();
            assertThat(rs.getInt(1)).isGreaterThanOrEqualTo(5);
        }
    }

    @Test
    void flywayVersionTableExists() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            List<String> tableNames = new ArrayList<>();
            while (tables.next()) {
                tableNames.add(tables.getString("TABLE_NAME").toUpperCase());
            }

            assertThat(tableNames).contains("FLYWAY_SCHEMA_HISTORY");
        }
    }
}
