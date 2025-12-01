package com.group13.population.model;

/**
 * Row model for population reports (R24, R25).
 *
 * Name = continent / region / country depending on the query.
 */
public class PopulationRow {

    private final String name;
    private final long totalPopulation;
    private final long cityPopulation;
    private final long nonCityPopulation;
    private final double cityPopulationPercent;
    private final double nonCityPopulationPercent;

    public PopulationRow(String name,
                         long totalPopulation,
                         long cityPopulation,
                         long nonCityPopulation,
                         double cityPopulationPercent,
                         double nonCityPopulationPercent) {
        this.name = name;
        this.totalPopulation = totalPopulation;
        this.cityPopulation = cityPopulation;
        this.nonCityPopulation = nonCityPopulation;
        this.cityPopulationPercent = cityPopulationPercent;
        this.nonCityPopulationPercent = nonCityPopulationPercent;
    }

    /**
     * Factory method that calculates non-city population and percentages.
     */
    public static PopulationRow fromTotals(String name, long totalPopulation, long cityPopulation) {
        if (totalPopulation < 0) {
            totalPopulation = 0;
        }
        if (cityPopulation < 0) {
            cityPopulation = 0;
        }

        long nonCity = Math.max(0, totalPopulation - cityPopulation);

        double cityPct = 0.0;
        double nonCityPct = 0.0;
        if (totalPopulation > 0) {
            cityPct = (cityPopulation * 100.0) / totalPopulation;
            nonCityPct = (nonCity * 100.0) / totalPopulation;
        }

        return new PopulationRow(
            name,
            totalPopulation,
            cityPopulation,
            nonCity,
            cityPct,
            nonCityPct
        );
    }

    public String getName() {
        return name;
    }

    public long getTotalPopulation() {
        return totalPopulation;
    }

    public long getCityPopulation() {
        return cityPopulation;
    }

    public long getNonCityPopulation() {
        return nonCityPopulation;
    }

    public double getCityPopulationPercent() {
        return cityPopulationPercent;
    }

    public double getNonCityPopulationPercent() {
        return nonCityPopulationPercent;
    }

    // -------------------------------------------------------------------------
    // Compatibility getters used by repo/tests (living / not living in cities)
    // -------------------------------------------------------------------------

    /** Alias for city population – people living in cities. */
    public long getLivingInCities() {
        return cityPopulation;
    }

    /** Alias for non-city population – people not living in cities. */
    public long getNotLivingInCities() {
        return nonCityPopulation;
    }
}
