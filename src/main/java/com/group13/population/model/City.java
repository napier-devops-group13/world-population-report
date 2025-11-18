package com.group13.population.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Simple immutable DTO for city reports (R07–R16).
 *
 * Fields map directly to the coursework spec:
 *  - name
 *  - country
 *  - district
 *  - population
 *
 * The {@link JsonCreator} / {@link JsonProperty} annotations make this
 * class deserialisable by Jackson in CityReportsOrderingTest.
 */
public final class City {

    private final String name;
    private final String country;
    private final String district;
    private final long population;

    /**
     * Main constructor used by both application code and Jackson.
     */
    @JsonCreator
    public City(
        @JsonProperty("name") String name,
        @JsonProperty("country") String country,
        @JsonProperty("district") String district,
        @JsonProperty("population") long population
    ) {
        this.name = name == null ? "" : name.trim();
        this.country = country == null ? "" : country.trim();
        this.district = district == null ? "" : district.trim();
        this.population = Math.max(0L, population);
    }

    /**
     * Convenient factory, used in CityTest and consistent with other models.
     */
    public static City of(String name, String country, String district, long population) {
        return new City(name, country, district, population);
    }

    // ================= getters =================

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getDistrict() {
        return district;
    }

    public long getPopulation() {
        return population;
    }

    // ======== value-object helpers (used by CityTest) ========

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof City)) {
            return false;
        }
        City other = (City) o;
        return population == other.population
            && Objects.equals(name, other.name)
            && Objects.equals(country, other.country)
            && Objects.equals(district, other.district);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, country, district, population);
    }

    @Override
    public String toString() {
        return "City{" +
            "name='" + name + '\'' +
            ", country='" + country + '\'' +
            ", district='" + district + '\'' +
            ", population=" + population +
            '}';
    }
}
