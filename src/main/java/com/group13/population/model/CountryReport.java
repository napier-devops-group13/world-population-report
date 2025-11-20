package com.group13.population.model;

/**
 * DTO used by the web/API layer for country reports (R01–R06).
 *
 * Fields match the coursework spec:
 *   Code, Name, Continent, Region, Population, Capital.
 */
public final class CountryReport {

    private final String code;
    private final String name;
    private final String continent;
    private final String region;
    private final long population;
    private final String capital;

    public CountryReport(String code,
                         String name,
                         String continent,
                         String region,
                         long population,
                         String capital) {
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
}
