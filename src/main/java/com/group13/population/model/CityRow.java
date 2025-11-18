package com.group13.population.model;

/**
 * DTO for city reports (R07–R16).
 * Columns: Name, Country, District, Population.
 */
public class CityRow {

    private final String name;
    private final String country;
    private final String district;
    private final long population;

    /**
     * Creates a new immutable city row.
     *
     * @param name       city name
     * @param country    country name
     * @param district   district name
     * @param population population of the city
     */
    public CityRow(String name, String country, String district, long population) {
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
