package com.group13.population.model;

import java.util.Objects;

/**
 * Simple model for capital city reports (R17–R22).
 * Fields and JSON properties: name, country, population.
 */
public class CapitalCity {

    private String name;
    private String country;
    private long population;

    /** No-args constructor required by Jackson for JSON deserialization. */
    public CapitalCity() {
        // Jackson uses this, then calls the setters.
    }

    /** All-args constructor used in tests and in the application code. */
    public CapitalCity(String name, String country, long population) {
        this.name = name;
        this.country = country;
        this.population = population;
    }

    // Getters

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public long getPopulation() {
        return population;
    }

    // Setters (needed so Jackson can populate the object)

    public void setName(String name) {
        this.name = name;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setPopulation(long population) {
        this.population = population;
    }

    // Equality and hash code based on all fields

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;        // <-- braces satisfy Checkstyle
        }
        if (!(o instanceof CapitalCity)) {
            return false;       // <-- braces satisfy Checkstyle
        }
        CapitalCity that = (CapitalCity) o;
        return population == that.population
            && Objects.equals(name, that.name)
            && Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, country, population);
    }

    // toString should contain all fields (for your tests)

    @Override
    public String toString() {
        return "CapitalCity{" +
            "name='" + name + '\'' +
            ", country='" + country + '\'' +
            ", population=" + population +
            '}';
    }
}
