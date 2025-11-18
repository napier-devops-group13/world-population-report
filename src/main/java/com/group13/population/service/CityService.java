package com.group13.population.service;

import com.group13.population.model.City;
import com.group13.population.repo.CityRepo;

import java.util.List;
import java.util.Objects;

/**
 * Application-level service for city reports (R07–R16).
 * Thin wrapper around {@link CityRepo} that:
 *  - normalises null / whitespace parameters
 *  - guards against non-positive N values
 *  - delegates all work to the repository
 */
public final class CityService {

    private final CityRepo repo;

    public CityService(CityRepo repo) {
        this.repo = Objects.requireNonNull(repo, "repo");
    }

    // -------------------------------------------------------------------------
    // World (R07, R08)
    // -------------------------------------------------------------------------

    /** All cities in the world, ordered by population DESC then name ASC. */
    public List<City> worldAll() {
        return repo.worldAll();
    }

    /** Top-N cities in the world (N is forced to be at least 1). */
    public List<City> worldTopN(int n) {
        return repo.worldTopN(positive(n));
    }

    // -------------------------------------------------------------------------
    // Continent (R09, R10)
    // -------------------------------------------------------------------------

    /** All cities in a continent; null becomes empty string, value is trimmed. */
    public List<City> continentAll(String continent) {
        return repo.continentAll(trimToEmpty(continent));
    }

    /** Top-N cities in a continent (trimmed name, N forced positive). */
    public List<City> continentTopN(String continent, int n) {
        return repo.continentTopN(trimToEmpty(continent), positive(n));
    }

    // -------------------------------------------------------------------------
    // Region (R11, R12)
    // -------------------------------------------------------------------------

    public List<City> regionAll(String region) {
        return repo.regionAll(trimToEmpty(region));
    }

    public List<City> regionTopN(String region, int n) {
        return repo.regionTopN(trimToEmpty(region), positive(n));
    }

    // -------------------------------------------------------------------------
    // Country (R13, R14)
    // -------------------------------------------------------------------------

    public List<City> countryAll(String country) {
        return repo.countryAll(trimToEmpty(country));
    }

    public List<City> countryTopN(String country, int n) {
        return repo.countryTopN(trimToEmpty(country), positive(n));
    }

    // -------------------------------------------------------------------------
    // District (R15, R16)
    // -------------------------------------------------------------------------

    public List<City> districtAll(String district) {
        return repo.districtAll(trimToEmpty(district));
    }

    public List<City> districtTopN(String district, int n) {
        return repo.districtTopN(trimToEmpty(district), positive(n));
    }

    // -------------------------------------------------------------------------
    // Helpers (package-private so tests in same package can see behaviour)
    // -------------------------------------------------------------------------

    /** Treats null as empty string and trims whitespace. */
    String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    /** Guarantees a strictly positive value (1 if n <= 0). */
    int positive(int n) {
        return (n <= 0) ? 1 : n;
    }
}
