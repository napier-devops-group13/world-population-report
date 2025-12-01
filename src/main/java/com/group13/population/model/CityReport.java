package com.group13.population.model;

/**
 * DTO used by the web/API layer for city reports (R07–R16).
 *
 * Fields match the coursework spec:
 *   Name, Country, District, Population.
 */
public final class CityReport {

    private final String name;
    private final String country;
    private final String district;
    private final long population;

    public CityReport(String name,
                      String country,
                      String district,
                      long population) {
        this.name = name;
        this.country = country;
        this.district = district;
        this.population = population;
    }

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
}
