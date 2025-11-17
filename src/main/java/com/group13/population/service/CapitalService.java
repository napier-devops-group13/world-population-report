package com.group13.population.service;

import com.group13.population.model.CapitalCity;
import com.group13.population.repo.CapitalRepo;

import java.util.List;
import java.util.Objects;

/**
 * Service layer for capital city reports (R17–R22).
 * Thin wrapper over {@link CapitalRepo} with light input hygiene.
 */
public final class CapitalService {

    private final CapitalRepo repo;

    public CapitalService(CapitalRepo repo) {
        this.repo = Objects.requireNonNull(repo, "repo");
    }

    // ================ R17–R19: all capitals =================

    /** R17 – all capital cities in the world (population DESC). */
    public List<CapitalCity> allCapitalsWorld() {
        return repo.allCapitalsWorld();
    }

    /** R18 – all capital cities in a continent (population DESC). */
    public List<CapitalCity> allCapitalsContinent(String continent) {
        return repo.allCapitalsContinent(safe(continent));
    }

    /** R19 – all capital cities in a region (population DESC). */
    public List<CapitalCity> allCapitalsRegion(String region) {
        return repo.allCapitalsRegion(safe(region));
    }

    // ================ R20–R22: top-N capitals =================

    /** R20 – top-N capital cities in the world (population DESC). */
    public List<CapitalCity> topCapitalsWorld(int n) {
        return repo.topCapitalsWorld(positive(n));
    }

    /** R21 – top-N capital cities in a continent (population DESC). */
    public List<CapitalCity> topCapitalsContinent(String continent, int n) {
        return repo.topCapitalsContinent(safe(continent), positive(n));
    }

    /** R22 – top-N capital cities in a region (population DESC). */
    public List<CapitalCity> topCapitalsRegion(String region, int n) {
        return repo.topCapitalsRegion(safe(region), positive(n));
    }

    // ================= helpers =================

    private static String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    private static int positive(int n) {
        return n <= 0 ? 1 : n;
    }
}
