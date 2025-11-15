package com.group13.population.service;

import com.group13.population.repo.WorldRepo;

import java.util.List;
import java.util.Objects;

/**
 * Service layer for country reports (R01–R06).
 *
 * <p>This is a thin layer over {@link WorldRepo} that:
 * <ul>
 *   <li>exposes clearly-named methods for each requirement, and</li>
 *   <li>performs light validation / sanitising of input.</li>
 * </ul>
 * Keeping this layer small makes the behaviour easy to unit test.</p>
 */
public final class CountryService {

    private final WorldRepo repo;

    public CountryService(WorldRepo repo) {
        this.repo = Objects.requireNonNull(repo, "repo");
    }

    /* ================== R01–R03: all countries ================== */

    /** R01 — all countries in the world (population DESC). */
    public List<WorldRepo.CountryRow> allCountriesWorld() {
        return repo.allCountriesWorld();
    }

    /** R02 — all countries in a continent (population DESC). */
    public List<WorldRepo.CountryRow> allCountriesContinent(String continent) {
        return repo.allCountriesContinent(safe(continent));
    }

    /** R03 — all countries in a region (population DESC). */
    public List<WorldRepo.CountryRow> allCountriesRegion(String region) {
        return repo.allCountriesRegion(safe(region));
    }

    /* ================== R04–R06: top-N lists ================== */

    /** R04 — top-N countries in the world (population DESC). */
    public List<WorldRepo.CountryRow> topCountriesWorld(int n) {
        return repo.topCountriesWorld(positive(n));
    }

    /** R05 — top-N countries in a continent (population DESC). */
    public List<WorldRepo.CountryRow> topCountriesContinent(String continent, int n) {
        return repo.topCountriesContinent(safe(continent), positive(n));
    }

    /** R06 — top-N countries in a region (population DESC). */
    public List<WorldRepo.CountryRow> topCountriesRegion(String region, int n) {
        return repo.topCountriesRegion(safe(region), positive(n));
    }

    /* ================== helpers ================== */

    /**
     * Defensive helper – normalise potentially null/whitespace input
     * coming from the web layer.
     */
    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    /**
     * Defensive helper – ensure we never ask the repo for zero or
     * negative rows. Routes already validate this, but the default
     * makes the method safe to call from anywhere.
     */
    private static int positive(int n) {
        return n <= 0 ? 1 : n;
    }
}
