package com.group13.population.repo;

import com.group13.population.db.Db;
import com.group13.population.model.City;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link CityRepo} using the classic world database.
 *
 * Produces rows with:
 *   Name, Country, District, Population
 */
public final class CityWorldRepo implements CityRepo {

    private static final String BASE_SELECT =
        "SELECT ci.Name       AS Name, "
            + "       co.Name       AS Country, "
            + "       ci.District   AS District, "
            + "       ci.Population AS Population "
            + "FROM city ci "
            + "JOIN country co ON ci.CountryCode = co.Code ";

    private static final String ORDER_BY_POP_DESC_NAME_ASC =
        " ORDER BY ci.Population DESC, ci.Name ASC";

    /** Shared DB connection (managed outside in tests, or created in default ctor). */
    private final Connection connection;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Default constructor used by the application (routes).
     * Opens a connection using environment variables, with sensible defaults.
     */
    public CityWorldRepo() {
        String host = envOr("DB_HOST", "localhost");
        int port = Integer.parseInt(envOr("DB_PORT", "43306"));
        String dbName = envOr("DB_NAME", "world");
        String user = envOr("DB_USER", "app");
        String pass = envOr("DB_PASS", "app");

        try {
            this.connection = Db.connect(host, port, dbName, user, pass);
        } catch (SQLException e) {
            // Wrap in unchecked so the app can start/fail cleanly
            throw new IllegalStateException("Failed to connect to database", e);
        }
    }


    /**
     * Constructor used by CityWorldRepoIT – reuses the test-managed connection.
     */
    public CityWorldRepo(Connection connection) {
        this.connection = connection;
    }

    // -------------------------------------------------------------------------
    // World (R07/R08)
    // -------------------------------------------------------------------------

    @Override
    public List<City> worldAll() {
        String sql = BASE_SELECT + ORDER_BY_POP_DESC_NAME_ASC;
        return query(sql, ps -> { });
    }

    @Override
    public List<City> worldTopN(int limit) {
        String sql = BASE_SELECT + ORDER_BY_POP_DESC_NAME_ASC + " LIMIT ?";
        return query(sql, ps -> ps.setInt(1, limit));
    }

    // -------------------------------------------------------------------------
    // Continent (R09/R10)
    // -------------------------------------------------------------------------

    @Override
    public List<City> continentAll(String continent) {
        String sql = BASE_SELECT
            + "WHERE co.Continent = ?"
            + ORDER_BY_POP_DESC_NAME_ASC;
        return query(sql, ps -> ps.setString(1, continent));
    }

    @Override
    public List<City> continentTopN(String continent, int limit) {
        String sql = BASE_SELECT
            + "WHERE co.Continent = ?"
            + ORDER_BY_POP_DESC_NAME_ASC
            + " LIMIT ?";
        return query(sql, ps -> {
            ps.setString(1, continent);
            ps.setInt(2, limit);
        });
    }

    // -------------------------------------------------------------------------
    // Region (R11/R12)
    // -------------------------------------------------------------------------

    @Override
    public List<City> regionAll(String region) {
        String sql = BASE_SELECT
            + "WHERE co.Region = ?"
            + ORDER_BY_POP_DESC_NAME_ASC;
        return query(sql, ps -> ps.setString(1, region));
    }

    @Override
    public List<City> regionTopN(String region, int limit) {
        String sql = BASE_SELECT
            + "WHERE co.Region = ?"
            + ORDER_BY_POP_DESC_NAME_ASC
            + " LIMIT ?";
        return query(sql, ps -> {
            ps.setString(1, region);
            ps.setInt(2, limit);
        });
    }

    // -------------------------------------------------------------------------
    // Country (R13/R14)
    // -------------------------------------------------------------------------

    @Override
    public List<City> countryAll(String country) {
        String sql = BASE_SELECT
            + "WHERE co.Name = ?"
            + ORDER_BY_POP_DESC_NAME_ASC;
        return query(sql, ps -> ps.setString(1, country));
    }

    @Override
    public List<City> countryTopN(String country, int limit) {
        String sql = BASE_SELECT
            + "WHERE co.Name = ?"
            + ORDER_BY_POP_DESC_NAME_ASC
            + " LIMIT ?";
        return query(sql, ps -> {
            ps.setString(1, country);
            ps.setInt(2, limit);
        });
    }

    // -------------------------------------------------------------------------
    // District (R15/R16)
    // -------------------------------------------------------------------------

    @Override
    public List<City> districtAll(String district) {
        String sql = BASE_SELECT
            + "WHERE ci.District = ?"
            + ORDER_BY_POP_DESC_NAME_ASC;
        return query(sql, ps -> ps.setString(1, district));
    }

    @Override
    public List<City> districtTopN(String district, int limit) {
        String sql = BASE_SELECT
            + "WHERE ci.District = ?"
            + ORDER_BY_POP_DESC_NAME_ASC
            + " LIMIT ?";
        return query(sql, ps -> {
            ps.setString(1, district);
            ps.setInt(2, limit);
        });
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    @FunctionalInterface
    private interface StatementConfigurer {
        void configure(PreparedStatement ps) throws SQLException;
    }

    private List<City> query(String sql, StatementConfigurer configurer) {
        List<City> results = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            configurer.configure(ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("Name");
                    String country = rs.getString("Country");
                    String district = rs.getString("District");
                    long population = rs.getLong("Population");

                    City city = new City(name, country, district, population);
                    results.add(city);
                }
            }
        } catch (SQLException e) {
            // Lab style: on error, fail safe and return empty list.
            return List.of();
        }

        return results;
    }

    private static String envOr(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
