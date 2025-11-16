package com.group13.population.repo;

import com.group13.population.db.Db;
import com.group13.population.model.CapitalCity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link CapitalRepo} backed by the classic world DB.
 *
 * Supports the capital city reports R17–R22:
 *  - R17: all capitals in the world
 *  - R18: all capitals in a continent
 *  - R19: all capitals in a region
 *  - R20: top-N capitals in the world
 *  - R21: top-N capitals in a continent
 *  - R22: top-N capitals in a region
 */
public class CapitalRepoJdbc implements CapitalRepo {

    /**
     * Base SELECT shared by all capital reports.
     * Joins COUNTRY and CITY where CITY.ID = COUNTRY.Capital.
     */
    private static final String BASE_SELECT =
        "SELECT ci.Name AS name, "
            + "co.Name AS country, "
            + "ci.Population AS population, "
            + "co.Continent AS continent, "
            + "co.Region AS region "
            + "FROM city ci "
            + "JOIN country co ON co.Capital = ci.ID ";

    /** Ordering required by the coursework – population DESC, name ASC. */
    private static final String ORDER_BY =
        " ORDER BY ci.Population DESC, ci.Name ASC";

    private final Connection connection;

    /**
     * Main constructor used by integration tests, where a ready {@link Connection}
     * is passed in (e.g. from {@link Db#connect(String, int, String, String, String)}).
     */
    public CapitalRepoJdbc(Connection connection) {
        this.connection = connection;
    }

    /**
     * Convenience constructor used by the real application and Javalin tests.
     * <p>
     * Reads DB connection details from the standard environment variables
     * ({@code DB_HOST}, {@code DB_PORT}, {@code DB_NAME}, {@code DB_USER},
     * {@code DB_PASS}) and falls back to the coursework defaults:
     * {@code localhost:43306 / world / app / app}.
     * </p>
     */
    public CapitalRepoJdbc() {
        this(createConnectionFromEnv());
    }

    // ---------------------------------------------------------------------
    // Helpers for connection and env handling
    // ---------------------------------------------------------------------

    private static Connection createConnectionFromEnv() {
        String host = envOr("DB_HOST", "localhost");
        int port = Integer.parseInt(envOr("DB_PORT", "43306"));
        String dbName = envOr("DB_NAME", "world");
        String user = envOr("DB_USER", "app");
        String pass = envOr("DB_PASS", "app");

        try {
            return Db.connect(host, port, dbName, user, pass);
        } catch (SQLException ex) {
            throw new IllegalStateException(
                "Failed to connect to database using DB_* environment variables",
                ex
            );
        }
    }

    private static String envOr(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    // ---------------------------------------------------------------------
    // Public API – R17–R22
    // ---------------------------------------------------------------------

    @Override
    public List<CapitalCity> allCapitalsWorld() {
        String sql = BASE_SELECT + ORDER_BY;
        return queryCapitals(sql, ps -> {
            // no parameters
        });
    }

    @Override
    public List<CapitalCity> allCapitalsContinent(String continent) {
        String sql = BASE_SELECT
            + "WHERE co.Continent = ?"
            + ORDER_BY;
        return queryCapitals(sql, ps -> ps.setString(1, continent));
    }

    @Override
    public List<CapitalCity> allCapitalsRegion(String region) {
        String sql = BASE_SELECT
            + "WHERE co.Region = ?"
            + ORDER_BY;
        return queryCapitals(sql, ps -> ps.setString(1, region));
    }

    @Override
    public List<CapitalCity> topCapitalsWorld(int n) {
        String sql = BASE_SELECT
            + ORDER_BY
            + " LIMIT ?";
        return queryCapitals(sql, ps -> ps.setInt(1, n));
    }

    @Override
    public List<CapitalCity> topCapitalsContinent(String continent, int n) {
        String sql = BASE_SELECT
            + "WHERE co.Continent = ?"
            + ORDER_BY
            + " LIMIT ?";
        return queryCapitals(sql, ps -> {
            ps.setString(1, continent);
            ps.setInt(2, n);
        });
    }

    @Override
    public List<CapitalCity> topCapitalsRegion(String region, int n) {
        String sql = BASE_SELECT
            + "WHERE co.Region = ?"
            + ORDER_BY
            + " LIMIT ?";
        return queryCapitals(sql, ps -> {
            ps.setString(1, region);
            ps.setInt(2, n);
        });
    }

    // ---------------------------------------------------------------------
    // Internal query helper
    // ---------------------------------------------------------------------

    @FunctionalInterface
    private interface StatementConfigurer {
        void configure(PreparedStatement ps) throws SQLException;
    }

    private List<CapitalCity> queryCapitals(String sql, StatementConfigurer configurer) {
        List<CapitalCity> results = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            configurer.configure(ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    String country = rs.getString("country");
                    long population = rs.getLong("population");

                    // CapitalCity only needs Name, Country, Population
                    results.add(new CapitalCity(name, country, population));
                }
            }
        } catch (SQLException ex) {
            // For this coursework we wrap checked SQLExceptions so that callers
            // (service / routes) do not need to handle JDBC details.
            throw new IllegalStateException("Failed to execute capital city report query", ex);
        }

        return results;
    }
}
