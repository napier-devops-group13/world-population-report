package com.group13.population.model;

import java.util.Objects;

/**
 * Domain model for a capital city report row (R17–R22).
 *
 * Columns required by the coursework:
 *   - Name
 *   - Country
 *   - Population
 */
public final class CapitalCity {

    private final String name;
    private final String country;
    private final long population;

    public CapitalCity(String name, String country, long population) {
        this.name = Objects.requireNonNull(name, "name");
        this.country = Objects.requireNonNull(country, "country");
        this.population = population;
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

    // ----- value semantics for easy testing -----

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CapitalCity)) {
            return false;
        }
        CapitalCity that = (CapitalCity) o;
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
        return "CapitalCity{"
            + "name='" + name + '\''
            + ", country='" + country + '\''
            + ", population=" + population
            + '}';
    }
}
