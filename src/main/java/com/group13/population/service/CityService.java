package com.group13.population.service;

import com.group13.population.model.CityRow;
import com.group13.population.repo.CityRepo;

import java.util.List;
import java.util.Objects;

/**
 * Business logic for city reports (R07–R16).
 */
public class CityService {

    private final CityRepo repo;

    /**
     * Creates a new city service.
     *
     * @param repo repository used to load city data.
     */
    public CityService(CityRepo repo) {
        this.repo = Objects.requireNonNull(repo, "repo must not be null");
    }

    // ---------------------------------------------------------------------
    // World (R07, R11)
    // ---------------------------------------------------------------------

    /**
     * All cities in the world ordered by population (largest first).
     *
     * @return list of cities.
     */
    public List<CityRow> getCitiesInWorldByPopulationDesc() {
        return repo.findCitiesInWorldByPopulationDesc();
    }

    /**
     * Top N cities in the world ordered by population (largest first).
     *
     * @param limit number of cities to return.
     * @return list of cities.
     */
    public List<CityRow> getTopCitiesInWorldByPopulationDesc(int limit) {
        validateLimit(limit);
        return repo.findTopCitiesInWorldByPopulationDesc(limit);
    }

    // ---------------------------------------------------------------------
    // Continent (R08, R12)
    // ---------------------------------------------------------------------

    /**
     * All cities in a continent ordered by population (largest first).
     *
     * @param continent continent name.
     * @return list of cities.
     */
    public List<CityRow> getCitiesInContinentByPopulationDesc(String continent) {
        validateName(continent, "continent");
        return repo.findCitiesInContinentByPopulationDesc(continent);
    }

    /**
     * Top N cities in a continent ordered by population (largest first).
     *
     * @param continent continent name.
     * @param limit     number of cities.
     * @return list of cities.
     */
    public List<CityRow> getTopCitiesInContinentByPopulationDesc(String continent,
                                                                 int limit) {
        validateName(continent, "continent");
        validateLimit(limit);
        return repo.findTopCitiesInContinentByPopulationDesc(continent, limit);
    }

    // ---------------------------------------------------------------------
    // Region (R09, R13)
    // ---------------------------------------------------------------------

    /**
     * All cities in a region ordered by population (largest first).
     *
     * @param region region name.
     * @return list of cities.
     */
    public List<CityRow> getCitiesInRegionByPopulationDesc(String region) {
        validateName(region, "region");
        return repo.findCitiesInRegionByPopulationDesc(region);
    }

    /**
     * Top N cities in a region ordered by population (largest first).
     *
     * @param region region name.
     * @param limit  number of cities.
     * @return list of cities.
     */
    public List<CityRow> getTopCitiesInRegionByPopulationDesc(String region,
                                                              int limit) {
        validateName(region, "region");
        validateLimit(limit);
        return repo.findTopCitiesInRegionByPopulationDesc(region, limit);
    }

    // ---------------------------------------------------------------------
    // Country (R10, R14)
    // ---------------------------------------------------------------------

    /**
     * All cities in a country ordered by population (largest first).
     *
     * @param country country name.
     * @return list of cities.
     */
    public List<CityRow> getCitiesInCountryByPopulationDesc(String country) {
        validateName(country, "country");
        return repo.findCitiesInCountryByPopulationDesc(country);
    }

    /**
     * Top N cities in a country ordered by population (largest first).
     *
     * @param country country name.
     * @param limit   number of cities.
     * @return list of cities.
     */
    public List<CityRow> getTopCitiesInCountryByPopulationDesc(String country,
                                                               int limit) {
        validateName(country, "country");
        validateLimit(limit);
        return repo.findTopCitiesInCountryByPopulationDesc(country, limit);
    }

    // ---------------------------------------------------------------------
    // District (R15, R16)
    // ---------------------------------------------------------------------

    /**
     * All cities in a district ordered by population (largest first).
     *
     * @param district district name.
     * @return list of cities.
     */
    public List<CityRow> getCitiesInDistrictByPopulationDesc(String district) {
        validateName(district, "district");
        return repo.findCitiesInDistrictByPopulationDesc(district);
    }

    /**
     * Top N cities in a district ordered by population (largest first).
     *
     * @param district district name.
     * @param limit    number of cities.
     * @return list of cities.
     */
    public List<CityRow> getTopCitiesInDistrictByPopulationDesc(String district,
                                                                int limit) {
        validateName(district, "district");
        validateLimit(limit);
        return repo.findTopCitiesInDistrictByPopulationDesc(district, limit);
    }

    // ---------------------------------------------------------------------
    // Validation helpers
    // ---------------------------------------------------------------------

    private void validateLimit(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be a positive integer.");
        }
    }

    private void validateName(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be empty.");
        }
    }
}
