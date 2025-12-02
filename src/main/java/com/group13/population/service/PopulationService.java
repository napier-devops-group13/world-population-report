package com.group13.population.service;

import com.group13.population.model.LanguagePopulationRow;
import com.group13.population.model.PopulationLookupRow;
import com.group13.population.model.PopulationRow;
import com.group13.population.repo.PopulationRepo;

import java.util.List;
import java.util.Objects;

/**
 * Service wrapper for population reports R24–R32.
 *
 * R24–R25: population in/out of cities (region / country).
 * R26:     population of the world.
 * R27:     population of a continent.
 * R28:     population of a region.
 * R29:     population of a country.
 * R30:     population of a district.
 * R31:     population of a city.
 * R32:     language populations (Chinese, English, Hindi, Spanish, Arabic)
 *          including % of world population.
 */
public class PopulationService {

    private final PopulationRepo populationRepo;

    public PopulationService(PopulationRepo populationRepo) {
        this.populationRepo = Objects.requireNonNull(populationRepo, "populationRepo");
    }

    // ---------------------------------------------------------------------
    // R24–R26
    // ---------------------------------------------------------------------

    public List<PopulationRow> getRegionPopulationInOutCities() {
        return populationRepo.findPopulationByRegionInOutCities();
    }

    public List<PopulationRow> getCountryPopulationInOutCities() {
        return populationRepo.findPopulationByCountryInOutCities();
    }

    public long getWorldPopulation() {
        return populationRepo.findWorldPopulation();
    }

    // ---------------------------------------------------------------------
    // R27 – The population of a continent
    // ---------------------------------------------------------------------

    /**
     * Returns the population of a continent.
     *
     * @param continent Continent name as stored in the database
     *                  (e.g. "Asia", "Europe").
     */
    public PopulationLookupRow getContinentPopulation(String continent) {
        return populationRepo.findContinentPopulation(continent);
    }

    // ---------------------------------------------------------------------
    // R28 – The population of a region
    // ---------------------------------------------------------------------

    /**
     * Returns the population of a region.
     *
     * @param region Region name as stored in the database
     *               (e.g. "Southeast Asia").
     */
    public PopulationLookupRow getRegionPopulation(String region) {
        return populationRepo.findRegionPopulation(region);
    }

    // ---------------------------------------------------------------------
    // R29 – The population of a country
    // ---------------------------------------------------------------------

    /**
     * Returns the population of a country (lookup by country name).
     */
    public PopulationLookupRow getCountryPopulation(String countryName) {
        return populationRepo.findCountryPopulation(countryName);
    }

    // ---------------------------------------------------------------------
    // R30 – The population of a district
    // ---------------------------------------------------------------------

    /**
     * Returns the population of a district
     * (sum of all cities in that district).
     */
    public PopulationLookupRow getDistrictPopulation(String district) {
        return populationRepo.findDistrictPopulation(district);
    }

    // ---------------------------------------------------------------------
    // R31 – The population of a city
    // ---------------------------------------------------------------------

    /**
     * Returns the population of a city.
     *
     * If multiple cities share the same name in different countries,
     * the populations are summed.
     */
    public PopulationLookupRow getCityPopulation(String cityName) {
        return populationRepo.findCityPopulation(cityName);
    }

    // ---------------------------------------------------------------------
    // R32 – Language populations and % of world
    // ---------------------------------------------------------------------

    /**
     * Returns language population data for:
     * Chinese, English, Hindi, Spanish, and Arabic,
     * ordered from greatest to smallest, with % of world population.
     */
    public List<LanguagePopulationRow> getLanguagePopulations() {
        return populationRepo.findLanguagePopulations();
    }
}
