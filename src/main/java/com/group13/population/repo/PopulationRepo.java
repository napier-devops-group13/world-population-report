package com.group13.population.repo;

import com.group13.population.db.Db;
import com.group13.population.model.LanguagePopulationRow;
import com.group13.population.model.PopulationLookupRow;
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
 *   <li>R27 – population of a continent.</li>
 *   <li>R28 – population of a region.</li>
 *   <li>R29 – population of a country.</li>
 *   <li>R30 – population of a district.</li>
 *   <li>R31 – population of a city.</li>
 *   <li>R32 – language populations + % of world (Chinese, English, Hindi, Spanish, Arabic).</li>
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
    // R23 – Population of people, in cities and not in cities, for each continent
    // ---------------------------------------------------------------------

    public List<PopulationRow> findPopulationByContinentInOutCities() {
        final String sql = """
            SELECT
                c.Continent        AS Name,
                SUM(c.Population)  AS TotalPopulation,
                SUM(ci.Population) AS CityPopulation
            FROM country c
            LEFT JOIN city ci ON ci.CountryCode = c.Code
            GROUP BY c.Continent
            ORDER BY TotalPopulation DESC
            """;

        // mapPopulationRow(...) expects columns: Name, TotalPopulation, CityPopulation
        // Non-city + percentages are calculated inside PopulationRow.fromTotals(...)
        return runPopulationQuery(sql);
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
    // R26 – The population of the world (numeric only)
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
    // R27 – The population of a continent
    // ---------------------------------------------------------------------

    /**
     * Return the population of a single continent as a lookup row.
     *
     * @param continent Continent name exactly as stored in the database.
     */
    public PopulationLookupRow findContinentPopulation(final String continent) {
        if (continent == null || continent.isBlank()) {
            return PopulationLookupRow.of("unknown continent", 0L);
        }

        final String sql = """
            SELECT SUM(Population) AS Population
            FROM country
            WHERE Continent = ?
            """;

        return runSingleLookup(continent, sql, continent);
    }

    // ---------------------------------------------------------------------
    // R28 – The population of a region
    // ---------------------------------------------------------------------

    /**
     * Return the population of a single region as a lookup row.
     *
     * @param region Region name exactly as stored in the database.
     */
    public PopulationLookupRow findRegionPopulation(final String region) {
        if (region == null || region.isBlank()) {
            return PopulationLookupRow.of("unknown region", 0L);
        }

        final String sql = """
            SELECT SUM(Population) AS Population
            FROM country
            WHERE Region = ?
            """;

        return runSingleLookup(region, sql, region);
    }

    // ---------------------------------------------------------------------
    // R29 – The population of a country
    // ---------------------------------------------------------------------

    /**
     * Return the population of a single country as a lookup row.
     *
     * <p>Lookup is by country name (e.g. "Myanmar").</p>
     */
    public PopulationLookupRow findCountryPopulation(final String countryName) {
        if (countryName == null || countryName.isBlank()) {
            return PopulationLookupRow.of("unknown country", 0L);
        }

        final String sql = """
            SELECT Population AS Population
            FROM country
            WHERE Name = ?
            """;

        return runSingleLookup(countryName, sql, countryName);
    }

// ---------------------------------------------------------------------
// R30 – The population of a district
// ---------------------------------------------------------------------

    /**
     * Return the total population of a district (sum of all cities in that district).
     *
     * Uses a prefix match so "Rangoon" will match "Rangoon [Yangon]" in the world DB.
     */
    public PopulationLookupRow findDistrictPopulation(final String district) {
        if (district == null || district.isBlank()) {
            return PopulationLookupRow.of("unknown district", 0L);
        }

        final String sql = """
        SELECT SUM(Population) AS Population
        FROM city
        WHERE District LIKE ?
        """;

        // "Rangoon" -> "Rangoon%" so it matches "Rangoon [Yangon]"
        String pattern = district + "%";

        return runSingleLookup(district, sql, pattern);
    }

// ---------------------------------------------------------------------
// R31 – The population of a city
// ---------------------------------------------------------------------

    /**
     * Return the population of a city.
     *
     * Uses a substring match so "Yangon" will match "Rangoon (Yangon)".
     */
    public PopulationLookupRow findCityPopulation(final String cityName) {
        if (cityName == null || cityName.isBlank()) {
            return PopulationLookupRow.of("unknown city", 0L);
        }

        final String sql = """
        SELECT SUM(Population) AS Population
        FROM city
        WHERE Name LIKE ?
        """;

        // "Yangon" -> "%Yangon%" so it matches "Rangoon (Yangon)"
        String pattern = "%" + cityName + "%";

        return runSingleLookup(cityName, sql, pattern);
    }


    // ---------------------------------------------------------------------
    // R32 – Language populations and % of world
    // ---------------------------------------------------------------------

    /**
     * Returns language population statistics for:
     * Chinese, English, Hindi, Spanish, and Arabic.
     *
     * Speakers are calculated using the classic world database:
     *   SUM(country.Population * countrylanguage.Percentage / 100)
     *
     * The percentage of world population is then calculated based on R26.
     */
    public List<LanguagePopulationRow> findLanguagePopulations() {
        final long worldPopulation = findWorldPopulation();

        final String sql = """
            SELECT
                cl.Language AS Language,
                SUM(c.Population * cl.Percentage / 100) AS Speakers
            FROM countrylanguage cl
            JOIN country c ON c.Code = cl.CountryCode
            WHERE cl.Language IN ('Chinese', 'English', 'Hindi', 'Spanish', 'Arabic')
            GROUP BY cl.Language
            ORDER BY Speakers DESC
            """;

        final Connection conn;
        try {
            conn = db.getConnection();
        } catch (SQLException ex) {
            System.err.println("PopulationRepo getConnection failed (R32): " + ex.getMessage());
            return Collections.emptyList();
        }

        if (conn == null) {
            return Collections.emptyList();
        }

        final List<LanguagePopulationRow> rows = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                final String language = rs.getString("Language");
                final long speakers = rs.getLong("Speakers");

                rows.add(LanguagePopulationRow.fromWorldTotal(
                    language,
                    speakers,
                    worldPopulation
                ));
            }
        } catch (SQLException ex) {
            System.err.println("PopulationRepo query failed (R32): " + ex.getMessage());
        }

        return rows;
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
     * Execute a lookup query that returns a single row with a column
     * aliased as "Population", then wrap it in a PopulationLookupRow.
     */
    private PopulationLookupRow runSingleLookup(final String logicalName,
                                                final String sql,
                                                final Object... params) {
        final Connection conn;

        try {
            conn = db.getConnection();
        } catch (SQLException ex) {
            System.err.println("PopulationRepo getConnection failed (lookup): " + ex.getMessage());
            return PopulationLookupRow.of(logicalName, 0L);
        }

        if (conn == null) {
            return PopulationLookupRow.of(logicalName, 0L);
        }

        long population = 0L;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    population = rs.getLong("Population");
                }
            }
        } catch (SQLException ex) {
            System.err.println("PopulationRepo lookup query failed: " + ex.getMessage());
        }

        return PopulationLookupRow.of(logicalName, population);
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
