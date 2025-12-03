package com.group13.population.model;

/**
 * DTO used by the web/API layer for capital city reports (R17–R22).
 *
 * Fields match the coursework spec:
 *   Name, Country, Population.
 */
public final class CapitalCityReport {

    private final String name;
    private final String country;
    private final long population;

    public CapitalCityReport(String name,
                             String country,
                             long population) {
        this.name = name;
        this.country = country;
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
}
