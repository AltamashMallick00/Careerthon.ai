package com.careerthon;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates that when DATABASE_URL points to an unreachable or expired cloud database host
 * (like dpg-da9voihsrm7s73dg33gg-a on Render), the application gracefully falls back to
 * embedded H2 database without crashing.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "DATABASE_URL=postgres://careerthon_user:secret@dpg-da9voihsrm7s73dg33gg-a:5432/careerthon"
})
class DatabaseFailoverTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testFallbackToH2WhenPostgresUnreachable() throws SQLException {
        assertNotNull(dataSource, "DataSource should not be null even when Cloud PostgreSQL is unreachable");
        try (Connection conn = dataSource.getConnection()) {
            assertNotNull(conn, "Connection must be acquired from fallback database");
            String dbProduct = conn.getMetaData().getDatabaseProductName();
            System.out.println("Verified active fallback DB product: " + dbProduct);
            assertTrue(dbProduct.equalsIgnoreCase("H2"), "Should fail over to H2 database");
        }
    }
}
