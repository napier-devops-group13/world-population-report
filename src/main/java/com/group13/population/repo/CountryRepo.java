package com.group13.population.repo;

import com.group13.population.model.Country;
import java.util.List;

/**
 * Repository contract for country reports (R01–R06).
 */
public interface CountryRepo {
    List<Country> worldAll();

    List<Country> continentAll(String continent);

    List<Country> regionAll(String region);

    List<Country> worldTopN(int n);

    List<Country> continentTopN(String continent, int n);

    List<Country> regionTopN(String region, int n);
}
