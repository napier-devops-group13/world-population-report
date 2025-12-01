package com.group13.population.service;

import com.group13.population.model.CountryRow;
import com.group13.population.repo.WorldRepo;

import java.util.List;
import java.util.Objects;

/**
 * Service layer for country reports R01–R06.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Validate and normalise input (continent / region / limit).</li>
 *   <li>Apply the coursework rule that all “top-N” calls
 *       are clamped to a maximum of {@link #MAX_LIMIT} rows.</li>
 *   <li>Delegate to {@link WorldRepo} for database access.</li>
 * </ul>
 *
 * <p>The public API has two groups of methods:</p>
 * <ul>
 *   <li><strong>get* methods</strong> – used directly by {@code CountryServiceTest}.</li>
 *   <li>“Friendly” methods (e.g. {@code worldByPopulation()}) – used by
 *       the HTTP layer ({@code CountryRoutes}).</li>
 * </ul>
 */
public class CountryService {

    /**
     * Maximum number of rows returned for any top-N request.
     *
     * {@code CountryServiceTest.topWorldLargeLimitIsClamped},
     * {@code topContinentTrimmedAndClamped} and
     * {@code topRegionTrimmedAndClamped} all expect this to be 500.
     */
    public static final int MAX_LIMIT = 500;

    private final WorldRepo repo;

    // ---------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------

    /** Default constructor – uses a real {@link WorldRepo}. */
    public CountryService() {
        this(new WorldRepo());
    }

    /** Test-friendly constructor allowing a stub {@link WorldRepo}. */
    public CountryService(WorldRepo repo) {
        this.repo = Objects.requireNonNull(repo, "repo");
    }

    // ---------------------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------------------

    /**
     * Normalise a free-text filter (continent / region):
     * <ul>
     *   <li>Trim whitespace.</li>
     *   <li>Return {@code null} for {@code null} or blank strings.</li>
     * </ul>
     */
    private String normalise(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Clamp a numeric limit to the valid range.
     * <ul>
     *   <li>{@code limit <= 0} → 0 (meaning “invalid / nothing”).</li>
     *   <li>{@code 1..MAX_LIMIT} → unchanged.</li>
     *   <li>{@code > MAX_LIMIT} → {@link #MAX_LIMIT}.</li>
     * </ul>
     */
    private int clampLimit(int limit) {
        if (limit <= 0) {
            return 0;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    /**
     * Parse a string limit, trimming whitespace and handling errors.
     * <ul>
     *   <li>{@code null} / blank → 0.</li>
     *   <li>Non-numeric → 0.</li>
     *   <li>Numeric → clamped via {@link #clampLimit(int)}.</li>
     * </ul>
     */
    private int parseLimit(String raw) {
        if (raw == null) {
            return 0;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
        try {
            int value = Integer.parseInt(trimmed);
            return clampLimit(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    // ---------------------------------------------------------------------
    // Methods used by CountryRoutes (HTTP layer)
    // ---------------------------------------------------------------------

    /** R01 – All countries in the world ordered by population DESC. */
    public List<CountryRow> worldByPopulation() {
        return getCountriesInWorldByPopulationDesc();
    }

    /** R02 – All countries in a continent ordered by population DESC. */
    public List<CountryRow> countriesByContinent(String continent) {
        return getCountriesInContinentByPopulationDesc(continent);
    }

    /** R03 – All countries in a region ordered by population DESC. */
    public List<CountryRow> countriesByRegion(String region) {
        return getCountriesInRegionByPopulationDesc(region);
    }

    /** R04 – Top-N countries in the world. */
    public List<CountryRow> topCountriesWorld(int limit) {
        return getTopCountriesInWorldByPopulationDesc(limit);
    }

    /** R05 – Top-N countries in a continent. */
    public List<CountryRow> topCountriesByContinent(String continent, int limit) {
        return getTopCountriesInContinentByPopulationDesc(continent, limit);
    }

    /** R06 – Top-N countries in a region. */
    public List<CountryRow> topCountriesByRegion(String region, int limit) {
        return getTopCountriesInRegionByPopulationDesc(region, limit);
    }

    // ---------------------------------------------------------------------
    // Methods expected by CountryServiceTest (get* API)
    // ---------------------------------------------------------------------

    // ----- R01 – world -----

    public List<CountryRow> getCountriesInWorldByPopulationDesc() {
        return repo.findCountriesInWorldByPopulationDesc();
    }

    // ----- R02 – continent -----

    public List<CountryRow> getCountriesInContinentByPopulationDesc(String continent) {
        String filter = normalise(continent);
        if (filter == null) {
            // tests expect empty list and no repo call for null/blank
            return List.of();
        }
        return repo.findCountriesInContinentByPopulationDesc(filter);
    }

    // ----- R03 – region -----

    public List<CountryRow> getCountriesInRegionByPopulationDesc(String region) {
        String filter = normalise(region);
        if (filter == null) {
            return List.of();
        }
        return repo.findCountriesInRegionByPopulationDesc(filter);
    }

    // ----- R04 – top world -----

    public List<CountryRow> getTopCountriesInWorldByPopulationDesc(int limit) {
        int guarded = clampLimit(limit);
        if (guarded <= 0) {
            return List.of();
        }
        return repo.findTopCountriesInWorldByPopulationDesc(guarded);
    }

    public List<CountryRow> getTopCountriesInWorldByPopulationDesc(String limit) {
        int guarded = parseLimit(limit);
        if (guarded <= 0) {
            return List.of();
        }
        return repo.findTopCountriesInWorldByPopulationDesc(guarded);
    }

    // ----- R05 – top continent -----

    public List<CountryRow> getTopCountriesInContinentByPopulationDesc(String continent, int limit) {
        String filter = normalise(continent);
        int guarded = clampLimit(limit);
        if (filter == null || guarded <= 0) {
            return List.of();
        }
        return repo.findTopCountriesInContinentByPopulationDesc(filter, guarded);
    }

    public List<CountryRow> getTopCountriesInContinentByPopulationDesc(String continent, String limit) {
        String filter = normalise(continent);
        int guarded = parseLimit(limit);
        if (filter == null || guarded <= 0) {
            return List.of();
        }
        return repo.findTopCountriesInContinentByPopulationDesc(filter, guarded);
    }

    // ----- R06 – top region -----

    public List<CountryRow> getTopCountriesInRegionByPopulationDesc(String region, int limit) {
        String filter = normalise(region);
        int guarded = clampLimit(limit);
        if (filter == null || guarded <= 0) {
            return List.of();
        }
        return repo.findTopCountriesInRegionByPopulationDesc(filter, guarded);
    }

    public List<CountryRow> getTopCountriesInRegionByPopulationDesc(String region, String limit) {
        String filter = normalise(region);
        int guarded = parseLimit(limit);
        if (filter == null || guarded <= 0) {
            return List.of();
        }
        return repo.findTopCountriesInRegionByPopulationDesc(filter, guarded);
    }
}
