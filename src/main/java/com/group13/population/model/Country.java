package com.group13.population.model;

import java.util.Objects;

/**
 * Country row used in reports (R01–R06).
 * Fields: code, name, continent, region, population, capital.
 */
public class Country {
    private final String code;
    private final String name;
    private final String continent;
    private final String region;
    private final long population;
    private final String capital;

    public Country(
        final String code,
        final String name,
        final String continent,
        final String region,
        final long population,
        final String capital
    ) {
        this.code = code;
        this.name = name;
        this.continent = continent;
        this.region = region;
        this.population = population;
        this.capital = capital;
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Country)) {
            return false;
        }
        Country that = (Country) o;
        return population == that.population
            && Objects.equals(code, that.code)
            && Objects.equals(name, that.name)
            && Objects.equals(continent, that.continent)
            && Objects.equals(region, that.region)
            && Objects.equals(capital, that.capital);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name, continent, region, population, capital);
    }

    @Override
    public String toString() {
        return "Country{"
            + "code='" + code + '\''
            + ", name='" + name + '\''
            + ", continent='" + continent + '\''
            + ", region='" + region + '\''
            + ", population=" + population
            + ", capital='" + capital + '\''
            + '}';
    }
}
