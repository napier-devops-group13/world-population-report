package com.group13.population.repo;

import com.group13.population.model.City;

import java.util.List;

/**
 * Repository abstraction for city reports (R07–R16).
 * Columns: Name, Country, District, Population.
 */
public interface CityRepo {

    // R07/R08 – cities in the world
    List<City> worldAll();

    List<City> worldTopN(int limit);

    // R09/R10 – cities in a continent
    List<City> continentAll(String continent);

    List<City> continentTopN(String continent, int limit);

    // R11/R12 – cities in a region
    List<City> regionAll(String region);

    List<City> regionTopN(String region, int limit);

    // R13/R14 – cities in a country
    List<City> countryAll(String country);

    List<City> countryTopN(String country, int limit);

    // R15/R16 – cities in a district
    List<City> districtAll(String district);

    List<City> districtTopN(String district, int limit);
}
