package com.group13.population.service;

import com.group13.population.model.CityRow;
import com.group13.population.repo.CapitalRepo;

import java.util.List;
import java.util.Objects;

/**
 * Business logic for capital city reports (R17–R22).
 */
public class CapitalService {

    private final CapitalRepo repo;

    /**
     * Creates a new capital service.
     *
     * @param repo repository used to load capital city data.
     */
    public CapitalService(CapitalRepo repo) {
        this.repo = Objects.requireNonNull(repo, "repo must not be null");
    }

    // World

    public List<CityRow> getCapitalCitiesInWorldByPopulationDesc() {
        return repo.findCapitalCitiesInWorldByPopulationDesc();
    }

    public List<CityRow> getTopCapitalCitiesInWorldByPopulationDesc(int limit) {
        validateLimit(limit);
        return repo.findTopCapitalCitiesInWorldByPopulationDesc(limit);
    }

    // Continent

    public List<CityRow> getCapitalCitiesInContinentByPopulationDesc(String continent) {
        validateName(continent, "continent");
        return repo.findCapitalCitiesInContinentByPopulationDesc(continent);
    }

    public List<CityRow> getTopCapitalCitiesInContinentByPopulationDesc(String continent,
                                                                        int limit) {
        validateName(continent, "continent");
        validateLimit(limit);
        return repo.findTopCapitalCitiesInContinentByPopulationDesc(continent, limit);
    }

    // Region

    public List<CityRow> getCapitalCitiesInRegionByPopulationDesc(String region) {
        validateName(region, "region");
        return repo.findCapitalCitiesInRegionByPopulationDesc(region);
    }

    public List<CityRow> getTopCapitalCitiesInRegionByPopulationDesc(String region,
                                                                     int limit) {
        validateName(region, "region");
        validateLimit(limit);
        return repo.findTopCapitalCitiesInRegionByPopulationDesc(region, limit);
    }

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
