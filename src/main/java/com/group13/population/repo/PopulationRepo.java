package com.group13.population.repo;

import com.group13.population.db.Db;
import com.group13.population.model.PopulationRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Repository for population reports.
 *
 * <p>Supports:</p>
 * <ul>
 *   <li>R24 – population in/out of cities for each region.</li>
 *   <li>R25 – population in/out of cities for each country.</li>
 *   <li>R26 – population of the world.</li>
 * </ul>
 */
public class PopulationRepo {

    /** Database helper; expected to be connected before use. */
    private final Db db;

    /**
     * Default constructor used in production wiring.
     */
    public PopulationRepo() {
        this(new Db());
    }

    /**
     * Constructor for injecting a pre-configured Db (used in tests).
     */
    public PopulationRepo(final Db db) {
        this.db = Objects.requireNonNull(db, "db");
    }

    // ---------------------------------------------------------------------
    // R24 – Population of people, in cities and not in cities, for each region
    // ---------------------------------------------------------------------

    public List<PopulationRow> findPopulationByRegionInOutCities() {
        final String sql = """
            SELECT
                c.Region AS Name,
                SUM(c.Population)       AS TotalPopulation,
                SUM(ci.Population)      AS CityPopulation
            FROM country c
            LEFT JOIN city ci ON ci.CountryCode = c.Code
            GROUP BY c.Region
            ORDER BY TotalPopulation DESC
            """;

        return runPopulationQuery(sql);
    }

    // ---------------------------------------------------------------------
    // R25 – Population of people, in cities and not in cities, for each country
    // ---------------------------------------------------------------------

    public List<PopulationRow> findPopulationByCountryInOutCities() {
        final String sql = """
            SELECT
                c.Name               AS Name,
                c.Population         AS TotalPopulation,
                SUM(ci.Population)   AS CityPopulation
            FROM country c
            LEFT JOIN city ci ON ci.CountryCode = c.Code
            GROUP BY c.Code, c.Name, c.Population
            ORDER BY TotalPopulation DESC
            """;

        return runPopulationQuery(sql);
    }

    // ---------------------------------------------------------------------
    // R26 – The population of the world
    // ---------------------------------------------------------------------

    public long findWorldPopulation() {
        final String sql = "SELECT SUM(Population) AS WorldPopulation FROM country";

        final Connection conn;

        try {
            conn = db.getConnection();
        } catch (SQLException ex) {
            System.err.println("PopulationRepo getConnection failed (R26): " + ex.getMessage());
            return 0L;
        }

        if (conn == null) {
            return 0L;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("WorldPopulation");
            }
        } catch (SQLException ex) {
            System.err.println("PopulationRepo query failed (R26): " + ex.getMessage());
        }

        return 0L;
    }

    // ---------------------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------------------

    /**
     * Execute a read-only SELECT and map to PopulationRow list.
     * Follows the same error-handling style as WorldRepo.
     */
    private List<PopulationRow> runPopulationQuery(final String sql, final Object... params) {
        final Connection conn;

        try {
            conn = db.getConnection();
        } catch (SQLException ex) {
            System.err.println("PopulationRepo getConnection failed: " + ex.getMessage());
            return Collections.emptyList();
        }

        if (conn == null) {
            return Collections.emptyList();
        }

        final List<PopulationRow> rows = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // bind parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapPopulationRow(rs));
                }
            }
        } catch (SQLException ex) {
            System.err.println("PopulationRepo query failed: " + ex.getMessage());
        }

        return rows;
    }

    /**
     * Map the current row from the ResultSet into a PopulationRow.
     * Expects columns: Name, TotalPopulation, CityPopulation.
     */
    private PopulationRow mapPopulationRow(final ResultSet rs) throws SQLException {
        final String name = rs.getString("Name");
        final long total = rs.getLong("TotalPopulation");
        final long city = rs.getLong("CityPopulation");
        return PopulationRow.fromTotals(name, total, city);
    }
}
