package com.group13.population.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Immutable projection of a single country row used by R01–R06 reports.
 *
 * <p>This class deliberately keeps only the fields required by the coursework
 * and is used as a low-level DTO between the repository and service layers.</p>
 */
public final class CountryRow {

    private final String code;
    private final String name;
    private final String continent;
    private final String region;
    private final long population;
    private final String capital;

    /**
     * Create a new {@code CountryRow}.
     *
     * @param code       ISO country code (required, non-blank)
     * @param name       Country name (required, non-blank)
     * @param continent  Continent name (required, non-blank)
     * @param region     Region name (required, non-blank)
     * @param population Population (must be zero or positive)
     * @param capital    Capital city name (optional, may be {@code null} or blank)
     */
    public CountryRow(
        String code,
        String name,
        String continent,
        String region,
        long population,
        String capital) {

        this.code = normaliseRequired(code, "code");
        this.name = normaliseRequired(name, "name");
        this.continent = normaliseRequired(continent, "continent");
        this.region = normaliseRequired(region, "region");

        if (population < 0) {
            throw new IllegalArgumentException("population must not be negative");
        }
        this.population = population;

        this.capital = normaliseOptional(capital);
    }

    private static String normaliseRequired(String value, String field) {
        if (value == null) {
            throw new NullPointerException(field + " must not be null");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return trimmed;
    }

    private static String normaliseOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getContinent() {
        return continent;
    }

    public String getRegion() {
        return region;
    }

    public long getPopulation() {
        return population;
    }

    public String getCapital() {
        return capital;
    }

    /**
     * Map the current row of a {@link ResultSet} into a {@code CountryRow}.
     *
     * <p>Column names follow the world database used in the coursework:
     * Code, Name, Continent, Region, Population and the joined
     * capital city name as {@code CapitalName}.</p>
     */
    public static CountryRow fromResultSet(ResultSet rs) throws SQLException {
        String code = rs.getString("Code");
        String name = rs.getString("Name");
        String continent = rs.getString("Continent");
        String region = rs.getString("Region");
        long population = rs.getLong("Population");
        String capital = rs.getString("CapitalName");
        return new CountryRow(code, name, continent, region, population, capital);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CountryRow)) {
            return false;
        }
        CountryRow that = (CountryRow) other;
        return population == that.population
            && code.equals(that.code)
            && name.equals(that.name)
            && continent.equals(that.continent)
            && region.equals(that.region)
            && Objects.equals(capital, that.capital);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name, continent, region, population, capital);
    }

    @Override
    public String toString() {
        return "CountryRow{"
            + "code='" + code + '\''
            + ", name='" + name + '\''
            + ", continent='" + continent + '\''
            + ", region='" + region + '\''
            + ", population=" + population
            + ", capital='" + capital + '\''
            + '}';
    }
}
