package com.group13.population.repo;

import com.group13.population.model.CapitalCity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Test double for {@link CapitalRepo} that serves seeded capital cities
 * entirely from memory (no DB).
 *
 * Used by unit tests for {@code CapitalService} / {@code CapitalRoutes}
 * so we can verify ordering and top-N behaviour without MySQL.
 */
public final class FakeCapitalRepo implements CapitalRepo {

    /** Immutable list of capitals, kept sorted by population DESC, name ASC. */
    private final List<CapitalCity> dataSorted;

    /**
     * Seed this fake repo with a list of capitals.
     * The list is copied and normalised to the same ordering as the
     * coursework requirements (population DESC, then name ASC).
     */
    public FakeCapitalRepo(List<CapitalCity> seed) {
        Objects.requireNonNull(seed, "seed");
        this.dataSorted = seed.stream()
            .sorted(Comparator
                .comparingLong(CapitalCity::getPopulation).reversed()
                .thenComparing(CapitalCity::getName))
            .collect(Collectors.toUnmodifiableList());
    }

    /* ===================== R17–R19: all capitals ===================== */

    @Override
    public List<CapitalCity> allCapitalsWorld() {
        // Already sorted in the constructor
        return new ArrayList<>(dataSorted);
    }

    @Override
    public List<CapitalCity> allCapitalsContinent(String continent) {
        // If CapitalCity later gets a continent field, you can refine this to filter.
        // For now this fake focuses on ordering/top-N behaviour.
        return new ArrayList<>(dataSorted);
    }

    @Override
    public List<CapitalCity> allCapitalsRegion(String region) {
        // Same note as allCapitalsContinent.
        return new ArrayList<>(dataSorted);
    }

    /* ===================== R20–R22: top-N capitals ===================== */

    @Override
    public List<CapitalCity> topCapitalsWorld(int n) {
        return top(dataSorted, n);
    }

    @Override
    public List<CapitalCity> topCapitalsContinent(String continent, int n) {
        return top(allCapitalsContinent(continent), n);
    }

    @Override
    public List<CapitalCity> topCapitalsRegion(String region, int n) {
        return top(allCapitalsRegion(region), n);
    }

    /* ===================== helpers ===================== */

    private static List<CapitalCity> top(List<CapitalCity> list, int n) {
        if (n <= 0) {
            n = 1;
        }
        int end = Math.min(n, list.size());
        return new ArrayList<>(list.subList(0, end));
    }
}
