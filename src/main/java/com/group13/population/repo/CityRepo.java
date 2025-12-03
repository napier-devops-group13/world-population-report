package com.group13.population.repo;

import com.group13.population.db.Db;
import com.group13.population.model.CityRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Repository for city reports (R07–R16).
 *
 * Provides queries for all cities and top N cities in the world,
 * a continent, region, country, or district ordered by population
 * from largest to smallest.
 */
public class CityRepo {

    /**
     * Shared SELECT clause for all city queries.
     */
    private static final String SELECT_BASE =
        "SELECT city.Name AS city_name, "
            + "country.Name AS country_name, "
            + "city.District AS district, "
            + "city.Population AS population "
            + "FROM city "
            + "JOIN country ON city.CountryCode = country.Code ";

    /**
     * Database wrapper. In production this is non-null; in some
     * test stubs a protected no-args constructor is used and the
     * methods are overridden so the field is not accessed.
     */
    private final Db db;

    /**
     * Protected no-args constructor used only by stub subclasses
     * in tests (for example, {@code CityServiceTest.StubCityRepo}).
     * <p>
     * The {@link #db} field is set to {@code null}; any method that
     * attempts to execute a real query will throw an exception.
     */
    protected CityRepo() {
        this.db = null;
    }

    /**
     * Creates a new city repository.
     *
     * @param db database wrapper, must not be {@code null}.
     */
    public CityRepo(Db db) {
        this.db = Objects.requireNonNull(db, "db must not be null");
    }

    // -------------------------------------------------------------------------
    // World (R07, R11)
    // -------------------------------------------------------------------------

    /**
     * All cities in the world ordered by population (largest first).
     *
     * @return list of cities.
     */
    public List<CityRow> findCitiesInWorldByPopulationDesc() {
        return queryCities(null, null, null);
    }

    /**
     * Top N cities in the world ordered by population (largest first).
     *
     * @param limit number of cities to return.
     * @return list of cities.
     */
    public List<CityRow> findTopCitiesInWorldByPopulationDesc(int limit) {
        validateLimit(limit);
        return queryCities(null, null, limit);
    }

    // -------------------------------------------------------------------------
    // Continent (R08, R12)
    // -------------------------------------------------------------------------

    /**
     * All cities in a continent ordered by population (largest first).
     *
     * @param continent continent name.
     * @return list of cities.
     */
    public List<CityRow> findCitiesInContinentByPopulationDesc(String continent) {
        validateName(continent, "continent");
        return queryCities("country.Continent = ?", continent, null);
    }

    /**
     * Top N cities in a continent ordered by population (largest first).
     *
     * @param continent continent name.
     * @param limit     number of cities.
     * @return list of cities.
     */
    public List<CityRow> findTopCitiesInContinentByPopulationDesc(String continent,
                                                                  int limit) {
        validateName(continent, "continent");
        validateLimit(limit);
        return queryCities("country.Continent = ?", continent, limit);
    }

    // -------------------------------------------------------------------------
    // Region (R09, R13)
    // -------------------------------------------------------------------------

    /**
     * All cities in a region ordered by population (largest first).
     *
     * @param region region name.
     * @return list of cities.
     */
    public List<CityRow> findCitiesInRegionByPopulationDesc(String region) {
        validateName(region, "region");
        return queryCities("country.Region = ?", region, null);
    }

    /**
     * Top N cities in a region ordered by population (largest first).
     *
     * @param region region name.
     * @param limit  number of cities.
     * @return list of cities.
     */
    public List<CityRow> findTopCitiesInRegionByPopulationDesc(String region,
                                                               int limit) {
        validateName(region, "region");
        validateLimit(limit);
        return queryCities("country.Region = ?", region, limit);
    }

    // -------------------------------------------------------------------------
    // Country (R10, R14)
    // -------------------------------------------------------------------------

    /**
     * All cities in a country ordered by population (largest first).
     *
     * @param country country name.
     * @return list of cities.
     */
    public List<CityRow> findCitiesInCountryByPopulationDesc(String country) {
        validateName(country, "country");
        return queryCities("country.Name = ?", country, null);
    }

    /**
     * Top N cities in a country ordered by population (largest first).
     *
     * @param country country name.
     * @param limit   number of cities.
     * @return list of cities.
     */
    public List<CityRow> findTopCitiesInCountryByPopulationDesc(String country,
                                                                int limit) {
        validateName(country, "country");
        validateLimit(limit);
        return queryCities("country.Name = ?", country, limit);
    }

    // -------------------------------------------------------------------------
    // District (R15, R16)
    // -------------------------------------------------------------------------

    /**
     * All cities in a district ordered by population (largest first).
     *
     * @param district district name.
     * @return list of cities.
     */
    public List<CityRow> findCitiesInDistrictByPopulationDesc(String district) {
        validateName(district, "district");
        return queryCities("city.District = ?", district, null);
    }

    /**
     * Top N cities in a district ordered by population (largest first).
     *
     * @param district district name.
     * @param limit    number of cities.
     * @return list of cities.
     */
    public List<CityRow> findTopCitiesInDistrictByPopulationDesc(String district,
                                                                 int limit) {
        validateName(district, "district");
        validateLimit(limit);
        return queryCities("city.District = ?", district, limit);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Runs a city query using the shared SELECT clause with an optional
     * {@code WHERE} condition and optional {@code LIMIT}.
     *
     * @param condition SQL condition after {@code WHERE}, or {@code null} for none.
     * @param value     value to bind for the condition, or {@code null}.
     * @param limit     maximum number of rows, or {@code null} for all rows.
     * @return list of matching city rows.
     */
    private List<CityRow> queryCities(String condition, String value, Integer limit) {
        if (db == null) {
            throw new IllegalStateException(
                "CityRepo was created without a Db; this constructor "
                    + "is only intended for test stubs.");
        }

        StringBuilder sql = new StringBuilder(SELECT_BASE);
        if (condition != null) {
            sql.append("WHERE ").append(condition).append(" ");
        }
        sql.append("ORDER BY city.Population DESC");

        boolean hasLimit = limit != null && limit > 0;
        if (hasLimit) {
            sql.append(" LIMIT ?");
        }

        try {
            Connection connection = db.getConnection();
            try (PreparedStatement stmt =
                     connection.prepareStatement(sql.toString())) {

                int index = 1;
                if (condition != null && value != null) {
                    stmt.setString(index++, value);
                }
                if (hasLimit) {
                    stmt.setInt(index, limit);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    List<CityRow> rows = new ArrayList<>();
                    while (rs.next()) {
                        rows.add(mapRow(rs));
                    }
                    return rows;
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to query cities.", ex);
        }
    }

    /**
     * Maps the current row of the result set to a {@link CityRow}.
     *
     * @param rs result set positioned on a row.
     * @return mapped city row.
     * @throws SQLException if a column cannot be read.
     */
    private CityRow mapRow(ResultSet rs) throws SQLException {
        String cityName = rs.getString("city_name");
        String countryName = rs.getString("country_name");
        String district = rs.getString("district");
        int population = rs.getInt("population");
        return new CityRow(cityName, countryName, district, population);
    }

    private void validateLimit(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be a positive integer.");
        }
    }

    private void validateName(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null.");
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
