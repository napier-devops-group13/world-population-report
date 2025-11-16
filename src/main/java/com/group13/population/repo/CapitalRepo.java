package com.group13.population.repo;

import com.group13.population.model.CapitalCity;

import java.util.List;

/**
 * Repository abstraction for capital city reports (R17–R22).
 */
public interface CapitalRepo {

    // R17 – all capital cities in the world (population DESC)
    List<CapitalCity> allCapitalsWorld();

    // R18 – all capital cities in a continent (population DESC)
    List<CapitalCity> allCapitalsContinent(String continent);

    // R19 – all capital cities in a region (population DESC)
    List<CapitalCity> allCapitalsRegion(String region);

    // R20 – top-N capital cities in the world (population DESC)
    List<CapitalCity> topCapitalsWorld(int n);

    // R21 – top-N capital cities in a continent (population DESC)
    List<CapitalCity> topCapitalsContinent(String continent, int n);

    // R22 – top-N capital cities in a region (population DESC)
    List<CapitalCity> topCapitalsRegion(String region, int n);
}
