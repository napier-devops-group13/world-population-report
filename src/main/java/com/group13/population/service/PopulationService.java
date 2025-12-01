package com.group13.population.service;

import com.group13.population.model.PopulationRow;
import com.group13.population.repo.PopulationRepo;

import java.util.List;
import java.util.Objects;

/**
 * Service wrapper for population reports R24–R26.
 */
public class PopulationService {

    private final PopulationRepo populationRepo;

    public PopulationService(PopulationRepo populationRepo) {
        this.populationRepo = Objects.requireNonNull(populationRepo, "populationRepo");
    }

    public List<PopulationRow> getRegionPopulationInOutCities() {
        return populationRepo.findPopulationByRegionInOutCities();
    }

    public List<PopulationRow> getCountryPopulationInOutCities() {
        return populationRepo.findPopulationByCountryInOutCities();
    }

    public long getWorldPopulation() {
        return populationRepo.findWorldPopulation();
    }
}
