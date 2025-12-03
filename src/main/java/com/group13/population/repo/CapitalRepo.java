package com.group13.population.repo;

import com.group13.population.db.Db;
import com.group13.population.model.CityRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Repository for capital city reports (R17–R22).
 *
 * <p>This class is defensive: if the database is not connected or a query
 * fails, the public methods return an empty (but non-null) list instead of
 * bubbling up SQL exceptions. This matches WorldRepo/CityRepo so that the
 * HTTP routes can still respond with 200 even when MySQL is not running
 * during unit tests.</p>
 */
public class CapitalRepo {

    private static final String SELECT_BASE =
        "SELECT city.Name AS city_name, "
            + "country.Name AS country_name, "
            + "city.District AS district, "
            + "city.Population AS population "
            + "FROM city "
            + "JOIN country ON city.ID = country.Capital ";

    private final Db db;

    /**
     * Creates a new capital repository.
     *
     * @param db database helper (must not be null)
     */
    public CapitalRepo(Db db) {
        this.db = Objects.requireNonNull(db, "db must not be null");
    }

    // ---------------------------------------------------------------------
    // World (R17, R20)
    // ---------------------------------------------------------------------

    /** R17 – all capital cities in the world, largest population first. */
    public List<CityRow> findCapitalCitiesInWorldByPopulationDesc() {
        String sql = SELECT_BASE + "ORDER BY city.Population DESC";
        return queryCapitalsAsCityRows(sql);
    }

    /** Alias used by CapitalRepoIT (legacy naming). */
    public List<CityRow> findCapitalsInWorldByPopulationDesc() {
        return findCapitalCitiesInWorldByPopulationDesc();
    }

    /** R20 – top N capital cities in the world, largest population first. */
    public List<CityRow> findTopCapitalCitiesInWorldByPopulationDesc(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        String sql = SELECT_BASE + "ORDER BY city.Population DESC LIMIT ?";
        return queryCapitalsAsCityRows(sql, limit);
    }

    /** Alias used by CapitalRepoIT (legacy naming). */
    public List<CityRow> findTopCapitalsInWorldByPopulationDesc(int limit) {
        return findTopCapitalCitiesInWorldByPopulationDesc(limit);
    }

    // ---------------------------------------------------------------------
    // Continent (R18, R21)
    // ---------------------------------------------------------------------

    /** R18 – all capital cities in a continent, largest population first. */
    public List<CityRow> findCapitalCitiesInContinentByPopulationDesc(String continent) {
        if (continent == null || continent.isBlank()) {
            return List.of();
        }
        String sql = SELECT_BASE
            + "WHERE country.Continent = ? "
            + "ORDER BY city.Population DESC";
        return queryCapitalsAsCityRows(sql, continent);
    }

    /** Alias used by CapitalRepoIT (legacy naming). */
    public List<CityRow> findCapitalsInContinentByPopulationDesc(String continent) {
        return findCapitalCitiesInContinentByPopulationDesc(continent);
    }

    /** R21 – top N capital cities in a continent, largest population first. */
    public List<CityRow> findTopCapitalCitiesInContinentByPopulationDesc(String continent,
                                                                         int limit) {
        if (continent == null || continent.isBlank() || limit <= 0) {
            return List.of();
        }
        String sql = SELECT_BASE
            + "WHERE country.Continent = ? "
            + "ORDER BY city.Population DESC LIMIT ?";
        return queryCapitalsAsCityRows(sql, continent, limit);
    }

    /** Alias used by CapitalRepoIT (legacy naming). */
    public List<CityRow> findTopCapitalsInContinentByPopulationDesc(String continent,
                                                                    int limit) {
        return findTopCapitalCitiesInContinentByPopulationDesc(continent, limit);
    }

    // ---------------------------------------------------------------------
    // Region (R19, R22)
    // ---------------------------------------------------------------------

    /** R19 – all capital cities in a region, largest population first. */
    public List<CityRow> findCapitalCitiesInRegionByPopulationDesc(String region) {
        if (region == null || region.isBlank()) {
            return List.of();
        }
        String sql = SELECT_BASE
            + "WHERE country.Region = ? "
            + "ORDER BY city.Population DESC";
        return queryCapitalsAsCityRows(sql, region);
    }

    /** Alias used by CapitalRepoIT (legacy naming). */
    public List<CityRow> findCapitalsInRegionByPopulationDesc(String region) {
        return findCapitalCitiesInRegionByPopulationDesc(region);
    }

    /** R22 – top N capital cities in a region, largest population first. */
    public List<CityRow> findTopCapitalCitiesInRegionByPopulationDesc(String region,
                                                                      int limit) {
        if (region == null || region.isBlank() || limit <= 0) {
            return List.of();
        }
        String sql = SELECT_BASE
            + "WHERE country.Region = ? "
            + "ORDER BY city.Population DESC LIMIT ?";
        return queryCapitalsAsCityRows(sql, region, limit);
    }

    /** Alias used by CapitalRepoIT (legacy naming). */
    public List<CityRow> findTopCapitalsInRegionByPopulationDesc(String region,
                                                                 int limit) {
        return findTopCapitalCitiesInRegionByPopulationDesc(region, limit);
    }

    // ---------------------------------------------------------------------
    // Shared query helper
    // ---------------------------------------------------------------------

    /**
     * Run the given SQL and map results into {@link CityRow} objects.
     * Any {@link SQLException} is logged and results in an empty list.
     */
    private List<CityRow> queryCapitalsAsCityRows(String sql, Object... params) {
        Objects.requireNonNull(sql, "sql must not be null");

        List<CityRow> rows = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                Object value = params[i];
                if (value instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) value);
                } else {
                    stmt.setString(i + 1, String.valueOf(value));
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String cityName = rs.getString("city_name");
                    String countryName = rs.getString("country_name");
                    String district = rs.getString("district");
                    int population = rs.getInt("population");

                    rows.add(new CityRow(cityName, countryName, district, population));
                }
            }
        } catch (SQLException ex) {
            // Important for tests: do NOT throw, just log + return empty list.
            System.err.println("WARNING: CapitalRepo query failed: " + ex.getMessage());
            rows.clear();
        }

        return Collections.unmodifiableList(rows);
    }
}
