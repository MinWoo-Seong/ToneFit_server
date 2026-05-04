package com.example.tonefitserver.global.config;

import com.example.tonefitserver.global.annotation.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

@IntegrationTest
class TestcontainerConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void 연결정보_확인() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            System.out.println("===== Testcontainers 연결 정보 =====");
            System.out.println("JDBC URL: " + metaData.getURL());
            System.out.println("Username: " + metaData.getUserName());
            System.out.println("Product: " + metaData.getDatabaseProductName());
            System.out.println("Version: " + metaData.getDatabaseProductVersion());
        }
    }
}
