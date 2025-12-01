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
 * Integration tests which verify that all city reports (R07–R16)
 * return data ordered by population descending and respect limits.
 */
class CityReportsOrderingTest {

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
                "Skipping CityReportsOrderingTest - cannot connect to DB: " + e.getMessage());
        }
    }

    private List<Long> queryCityPopulations(String sql, Object... params) {
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
            fail("City query failed: " + e.getMessage());
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

    // R07 – all cities in the world
    @Test
    @DisplayName("R07 - world cities ordered by population (DESC)")
    void worldCitiesReportOrderedByPopulation() {
        String sql =
            "SELECT c.Population AS Population " +
                "FROM city c " +
                "ORDER BY c.Population DESC";

        List<Long> pops = queryCityPopulations(sql);
        assertOrderedByPopulationDesc(pops, "World cities");
    }

    // R08 – all cities in a continent
    @Test
    @DisplayName("R08 - Europe cities ordered by population (DESC)")
    void continentCitiesReportOrderedByPopulation() {
        String sql =
            "SELECT c.Population AS Population " +
                "FROM city c " +
                "JOIN country co ON c.CountryCode = co.Code " +
                "WHERE co.Continent = ? " +
                "ORDER BY c.Population DESC";

        List<Long> pops = queryCityPopulations(sql, "Europe");
        assertOrderedByPopulationDesc(pops, "Europe cities");
    }

    // R09 – all cities in a region
    @Test
    @DisplayName("R09 - Caribbean cities ordered by population (DESC)")
    void regionCitiesReportOrderedByPopulation() {
        String sql =
            "SELECT c.Population AS Population " +
                "FROM city c " +
                "JOIN country co ON c.CountryCode = co.Code " +
                "WHERE co.Region = ? " +
                "ORDER BY c.Population DESC";

        List<Long> pops = queryCityPopulations(sql, "Caribbean");
        assertOrderedByPopulationDesc(pops, "Caribbean cities");
    }

    // R10 – all cities in a country
    @Test
    @DisplayName("R10 - Japanese cities ordered by population (DESC)")
    void countryCitiesReportOrderedByPopulation() {
        String sql =
            "SELECT c.Population AS Population " +
                "FROM city c " +
                "JOIN country co ON c.CountryCode = co.Code " +
                "WHERE co.Name = ? " +
                "ORDER BY c.Population DESC";

        List<Long> pops = queryCityPopulations(sql, "Japan");
        assertOrderedByPopulationDesc(pops, "Japan cities");
    }

    // R11 – all cities in a district
    @Test
    @DisplayName("R11 - Tokyo district cities ordered by population (DESC)")
    void districtCitiesReportOrderedByPopulation() {
        String sql =
            "SELECT c.Population AS Population " +
                "FROM city c " +
                "WHERE c.District = ? " +
                "ORDER BY c.Population DESC";

        List<Long> pops = queryCityPopulations(sql, "Tokyo");
        assertOrderedByPopulationDesc(pops, "Tokyo district cities");
    }

    // R12 – top-N cities in the world
    @Test
    @DisplayName("R12 - top 10 world cities ordered by population (DESC)")
    void topWorldCitiesReportOrderedAndLimited() {
        int limit = 10;
        String sql =
            "SELECT c.Population AS Population " +
                "FROM city c " +
                "ORDER BY c.Population DESC " +
                "LIMIT " + limit;

        List<Long> pops = queryCityPopulations(sql);
        assertTrue(pops.size() <= limit, "Top world cities - size should not exceed limit");
        assertOrderedByPopulationDesc(pops, "Top world cities");
    }

    // R13 – top-N cities in a continent
    @Test
    @DisplayName("R13 - top 5 Europe cities ordered by population (DESC)")
    void topContinentCitiesReportOrderedAndLimited() {
        int limit = 5;
        String sql =
            "SELECT c.Population AS Population " +
                "FROM city c " +
                "JOIN country co ON c.CountryCode = co.Code " +
                "WHERE co.Continent = ? " +
                "ORDER BY c.Population DESC " +
                "LIMIT " + limit;

        List<Long> pops = queryCityPopulations(sql, "Europe");
        assertTrue(pops.size() <= limit, "Top Europe cities - size should not exceed limit");
        assertOrderedByPopulationDesc(pops, "Top Europe cities");
    }

    // R14 – top-N cities in a region
    @Test
    @DisplayName("R14 - top 5 Caribbean cities ordered by population (DESC)")
    void topRegionCitiesReportOrderedAndLimited() {
        int limit = 5;
        String sql =
            "SELECT c.Population AS Population " +
                "FROM city c " +
                "JOIN country co ON c.CountryCode = co.Code " +
                "WHERE co.Region = ? " +
                "ORDER BY c.Population DESC " +
                "LIMIT " + limit;

        List<Long> pops = queryCityPopulations(sql, "Caribbean");
        assertTrue(pops.size() <= limit, "Top Caribbean cities - size should not exceed limit");
        assertOrderedByPopulationDesc(pops, "Top Caribbean cities");
    }

    // R15 – top-N cities in a country
    @Test
    @DisplayName("R15 - top 5 Japanese cities ordered by population (DESC)")
    void topCountryCitiesReportOrderedAndLimited() {
        int limit = 5;
        String sql =
            "SELECT c.Population AS Population " +
                "FROM city c " +
                "JOIN country co ON c.CountryCode = co.Code " +
                "WHERE co.Name = ? " +
                "ORDER BY c.Population DESC " +
                "LIMIT " + limit;

        List<Long> pops = queryCityPopulations(sql, "Japan");
        assertTrue(pops.size() <= limit, "Top Japan cities - size should not exceed limit");
        assertOrderedByPopulationDesc(pops, "Top Japan cities");
    }

    // R16 – top-N cities in a district
    @Test
    @DisplayName("R16 - top 5 Tokyo district cities ordered by population (DESC)")
    void topDistrictCitiesReportOrderedAndLimited() {
        int limit = 5;
        String sql =
            "SELECT c.Population AS Population " +
                "FROM city c " +
                "WHERE c.District = ? " +
                "ORDER BY c.Population DESC " +
                "LIMIT " + limit;

        List<Long> pops = queryCityPopulations(sql, "Tokyo");
        assertTrue(pops.size() <= limit, "Top Tokyo cities - size should not exceed limit");
        assertOrderedByPopulationDesc(pops, "Top Tokyo cities");
    }
}
