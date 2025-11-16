package com.group13.population.web;

import com.group13.population.model.CapitalCity;
import com.group13.population.repo.FakeCapitalRepo;
import com.group13.population.service.CapitalService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Service-level test verifying that the world capital report
 * is ordered by population DESC.
 *
 * Covers the ordering for the capital reports (R17/R20).
 */
class CapitalReportsOrderingTest {

    private static CapitalCity cap(
        String name,
        String country,
        long population
    ) {
        return new CapitalCity(name, country, population);
    }

    @Test
    void worldCapitalReportIsDescByPopulation() {
        // Seed data deliberately out of order by population
        List<CapitalCity> seed = List.of(
            cap("CapitalA", "CountryA", 5L),
            cap("CapitalB", "CountryB", 15L),
            cap("CapitalC", "CountryC", 10L)
        );

        // Use the fake repo so we don't hit a real DB
        CapitalService service = new CapitalService(new FakeCapitalRepo(seed));
        List<CapitalCity> all = service.allCapitalsWorld();

        assertEquals(3, all.size());
        assertTrue(
            all.get(0).getPopulation() >= all.get(1).getPopulation(),
            "First capital should have population >= second"
        );
        assertTrue(
            all.get(1).getPopulation() >= all.get(2).getPopulation(),
            "Second capital should have population >= third"
        );
    }
}
