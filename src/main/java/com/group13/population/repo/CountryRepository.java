package com.group13.population.repo;

import java.sql.SQLException;
import java.util.List;

/**
 * Abstraction for country reporting queries (R01–R06).
 */
public interface CountryRepository {

    List<CountryReport> countriesWorld() throws SQLException;

    List<CountryReport> countriesWorldByPopulation() throws SQLException;

    List<CountryReport> countriesByContinent(String continent)
        throws SQLException;

    List<CountryReport> countriesByContinentByPopulation(String continent)
        throws SQLException;

    List<CountryReport> countriesByRegion(String region)
        throws SQLException;

    List<CountryReport> countriesByRegionByPopulation(String region)
        throws SQLException;

    List<CountryReport> topCountriesWorld(int n)
        throws SQLException;

    List<CountryReport> topCountriesByContinent(String continent, int n)
        throws SQLException;

    List<CountryReport> topCountriesByRegion(String region, int n)
        throws SQLException;
}
