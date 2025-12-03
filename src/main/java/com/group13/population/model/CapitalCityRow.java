package com.group13.population.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Immutable projection of a single capital city row used by R17–R22 reports.
 *
 * <p>This class keeps only the fields required by the coursework
 * Capital City Report specification and is used as a low-level DTO between
 * the repository and service layers.</p>
 *
 * <p>Capital city report columns (spec): Name, Country, Population.</p>
 */
public final class CapitalCityRow {

    private final String name;
    private final String country;
    private final long population;

    /**
     * Create a new {@code CapitalCityRow}.
     *
     * @param name        Capital city name (required, non-blank)
     * @param country     Country name (required, non-blank)
     * @param population  Population of the capital city (must be zero or positive)
     */
    public CapitalCityRow(
        String name,
        String country,
        long population) {

        this.name = normaliseRequired(name, "name");
        this.country = normaliseRequired(country, "country");

        if (population < 0) {
            throw new IllegalArgumentException("population must not be negative");
        }
        this.population = population;
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

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public long getPopulation() {
        return population;
    }

    /**
     * Map the current row of a {@link ResultSet} into a {@code CapitalCityRow}.
     *
     * <p>Column names follow the aliases used in the capital city queries:
     * {@code Name}, {@code Country}, {@code Population}.</p>
     */
    public static CapitalCityRow fromResultSet(ResultSet rs) throws SQLException {
        String name = rs.getString("Name");
        String country = rs.getString("Country");
        long population = rs.getLong("Population");
        return new CapitalCityRow(name, country, population);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CapitalCityRow)) {
            return false;
        }
        CapitalCityRow that = (CapitalCityRow) other;
        return population == that.population
            && name.equals(that.name)
            && country.equals(that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, country, population);
    }

    @Override
    public String toString() {
        return "CapitalCityRow{"
            + "name='" + name + '\''
            + ", country='" + country + '\''
            + ", population=" + population
            + '}';
    }
}
