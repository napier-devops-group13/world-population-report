package com.group13.population.repo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Test double for WorldRepo that serves seeded rows (no DB). */
public class FakeCountryRepo extends WorldRepo {

    private final List<CountryRow> dataSorted;

    public FakeCountryRepo(List<CountryRow> seed) {
        Objects.requireNonNull(seed, "seed");
        // Always keep in the same ordering as the coursework requires:
        // population DESC, name ASC (tiebreaker)
        this.dataSorted = seed.stream()
            .sorted(Comparator
                .comparingLong((CountryRow r) -> r.population).reversed()
                .thenComparing(r -> r.name))
            .collect(Collectors.toUnmodifiableList());
    }

    @Override public List<CountryRow> allCountriesWorld() {
        return dataSorted;
    }

    @Override public List<CountryRow> allCountriesContinent(String continent) {
        return dataSorted.stream()
            .filter(r -> r.continent.equals(continent))
            .collect(Collectors.toList());
    }

    @Override public List<CountryRow> allCountriesRegion(String region) {
        return dataSorted.stream()
            .filter(r -> r.region.equals(region))
            .collect(Collectors.toList());
    }

    @Override public List<CountryRow> topCountriesWorld(int n) {
        return top(dataSorted, n);
    }

    @Override public List<CountryRow> topCountriesContinent(String continent, int n) {
        return top(allCountriesContinent(continent), n);
    }

    @Override public List<CountryRow> topCountriesRegion(String region, int n) {
        return top(allCountriesRegion(region), n);
    }

    private static List<CountryRow> top(List<CountryRow> list, int n) {
        if (n <= 0) n = 1;
        int end = Math.min(n, list.size());
        return new ArrayList<>(list.subList(0, end));
    }
}
