package com.group13.population.model;

/**
 * Simple value object for population lookups (R27–R31).
 *
 * <p>Represents the population of a single continent, region, country,
 * district or city.</p>
 */
public class PopulationLookupRow {

    private final String name;
    private final long population;

    private PopulationLookupRow(String name, long population) {
        this.name = name;
        this.population = population;
    }

    /**
     * Create a lookup row, normalising the provided values.
     *
     * <p>If {@code name} is {@code null} or blank, {@code "unknown"} is used.
     * If {@code population} is negative, it is clamped to zero.</p>
     *
     * @param name       human-readable name of the location.
     * @param population population count; negative values are treated as zero.
     * @return normalised {@link PopulationLookupRow}.
     */
    public static PopulationLookupRow of(String name, long population) {
        String effectiveName =
            (name == null || name.trim().isEmpty()) ? "unknown" : name;

        long effectivePopulation = Math.max(0L, population);

        return new PopulationLookupRow(effectiveName, effectivePopulation);
    }

    /**
     * Return the location name.
     *
     * @return name of the continent, region, country, district or city.
     */
    public String getName() {
        return name;
    }

    /**
     * Return the population value.
     *
     * @return population count, never negative.
     */
    public long getPopulation() {
        return population;
    }
}
