package com.group13.population.repo;

/** Simple POJO for country reporting rows. */
public class CountryReport {

    private String code;
    private String name;
    private String continent;
    private String region;
    private long population;
    private String capital;

    public CountryReport() { }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(final String continent) {
        this.continent = continent;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    public long getPopulation() {
        return population;
    }

    public void setPopulation(final long population) {
        this.population = population;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(final String capital) {
        this.capital = capital;
    }
}
