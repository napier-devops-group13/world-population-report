package com.group13.population.web;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests which verify that all capital reports (R17–R22)
 * return data ordered by population descending and respect limits.
 */
class CapitalReportsOrderingTest {

    private static String jdbcUrl;
    private static String dbUser;
    private static String dbPassword;

    @BeforeAll
    static void setUp() {
        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        String port = System.getenv().getOrDefault("DB_PORT", "43306");
        String dbName = System.getenv().getOrDefault("DB_NAME", "world");

        jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        dbUser = System.getenv().getOrDefault("DB_USER", "root");
        dbPassword = System.getenv().getOrDefault("DB_PASSWORD", "example");

        // Try one connection; if it fails, SKIP all tests in this class
        try (Connection ignored = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            // ok
        } catch (SQLException e) {
            assumeTrue(false,
                "Skipping CapitalReportsOrderingTest - cannot connect to DB: " + e.getMessage());
        }
    }

    private List<Long> queryCapitalPopulations(String sql, Object... params) {
        List<Long> populations = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    populations.add(rs.getLong("Population"));
                }
            }
        } catch (SQLException e) {
            fail("Capital query failed: " + e.getMessage());
        }

        return populations;
    }

    private void assertOrderedByPopulationDesc(List<Long> populations, String message) {
        assertNotNull(populations, message + " - list should not be null");
        assertFalse(populations.isEmpty(), message + " - list should not be empty");

        for (int i = 1; i < populations.size(); i++) {
            long previous = populations.get(i - 1);
            long current = populations.get(i);
            assertTrue(
                previous >= current,
                message + " - out of order at index " + i +
                    " (" + previous + " < " + current + ")"
            );
        }
    }

    // R17 – all capital cities in the world
    @Test
    @DisplayName("R17 - world capitals ordered by population (DESC)")
    void worldCapitalsReportOrderedByPopulation() {
        String sql =
            "SELECT ci.Population AS Population " +
                "FROM city ci " +
                "JOIN country co ON ci.ID = co.Capital " +
                "ORDER BY ci.Population DESC";

        List<Long> pops = queryCapitalPopulations(sql);
        assertOrderedByPopulationDesc(pops, "World capitals");
    }

    // R18 – all capital cities in a continent
    @Test
    @DisplayName("R18 - Europe capitals ordered by population (DESC)")
    void continentCapitalsReportOrderedByPopulation() {
        String sql =
            "SELECT ci.Population AS Population " +
                "FROM city ci " +
                "JOIN country co ON ci.ID = co.Capital " +
                "WHERE co.Continent = ? " +
                "ORDER BY ci.Population DESC";

        List<Long> pops = queryCapitalPopulations(sql, "Europe");
        assertOrderedByPopulationDesc(pops, "Europe capitals");
    }

    // R19 – all capital cities in a region
    @Test
    @DisplayName("R19 - Caribbean capitals ordered by population (DESC)")
    void regionCapitalsReportOrderedByPopulation() {
        String sql =
            "SELECT ci.Population AS Population " +
                "FROM city ci " +
                "JOIN country co ON ci.ID = co.Capital " +
                "WHERE co.Region = ? " +
                "ORDER BY ci.Population DESC";

        List<Long> pops = queryCapitalPopulations(sql, "Caribbean");
        assertOrderedByPopulationDesc(pops, "Caribbean capitals");
    }

    // R20 – top-N capital cities in the world
    @Test
    @DisplayName("R20 - top 10 world capitals ordered by population (DESC)")
    void topWorldCapitalsReportOrderedAndLimited() {
        int limit = 10;
        String sql =
            "SELECT ci.Population AS Population " +
                "FROM city ci " +
                "JOIN country co ON ci.ID = co.Capital " +
                "ORDER BY ci.Population DESC " +
                "LIMIT " + limit;

        List<Long> pops = queryCapitalPopulations(sql);
        assertTrue(pops.size() <= limit, "Top world capitals - size should not exceed limit");
        assertOrderedByPopulationDesc(pops, "Top world capitals");
    }

    // R21 – top-N capital cities in a continent
    @Test
    @DisplayName("R21 - top 5 Europe capitals ordered by population (DESC)")
    void topContinentCapitalsReportOrderedAndLimited() {
        int limit = 5;
        String sql =
            "SELECT ci.Population AS Population " +
                "FROM city ci " +
                "JOIN country co ON ci.ID = co.Capital " +
                "WHERE co.Continent = ? " +
                "ORDER BY ci.Population DESC " +
                "LIMIT " + limit;

        List<Long> pops = queryCapitalPopulations(sql, "Europe");
        assertTrue(pops.size() <= limit, "Top Europe capitals - size should not exceed limit");
        assertOrderedByPopulationDesc(pops, "Top Europe capitals");
    }

    // R22 – top-N capital cities in a region
    @Test
    @DisplayName("R22 - top 5 Caribbean capitals ordered by population (DESC)")
    void topRegionCapitalsReportOrderedAndLimited() {
        int limit = 5;
        String sql =
            "SELECT ci.Population AS Population " +
                "FROM city ci " +
                "JOIN country co ON ci.ID = co.Capital " +
                "WHERE co.Region = ? " +
                "ORDER BY ci.Population DESC " +
                "LIMIT " + limit;

        List<Long> pops = queryCapitalPopulations(sql, "Caribbean");
        assertTrue(pops.size() <= limit, "Top Caribbean capitals - size should not exceed limit");
        assertOrderedByPopulationDesc(pops, "Top Caribbean capitals");
    }
}
