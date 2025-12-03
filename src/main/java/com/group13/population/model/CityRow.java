package com.group13.population.model;

/**
 * Represents one row in a city report.
 */
public class CityRow {

    private final String name;
    private final String country;
    private final String district;
    private final int population;

    /**
     * Creates a new city row.
     *
     * @param name       city name.
     * @param country    country name.
     * @param district   district name.
     * @param population population of the city.
     */
    public CityRow(String name, String country, String district, int population) {
        this.name = name;
        this.country = country;
        this.district = district;
        this.population = population;
    }

    /**
     * @return city name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return country name.
     */
    public String getCountry() {
        return country;
    }

    /**
     * @return district name.
     */
    public String getDistrict() {
        return district;
    }

    /**
     * @return population of the city.
     */
    public int getPopulation() {
        return population;
    }
}
