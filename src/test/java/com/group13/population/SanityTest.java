package com.group13.population;

import com.group13.population.db.Db;
import com.group13.population.repo.CountryReport;
import com.group13.population.repo.WorldRepo;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Quick CR2 check: verifies sorting (DESC by population) and presence of Capital field.
 * A generous timeout tolerates a cold MySQL start during CI.
 */
@Timeout(90) // seconds
public class SanityTest {

    @Test
    void worldTopIsSortedAndHasCapital() throws Exception {
        WorldRepo repo = new WorldRepo(new Db());
        List<CountryReport> top = repo.topCountriesWorld(5); // <-- no named arg

        assertEquals(5, top.size(), "Should return exactly 5 results");

        for (int i = 1; i < top.size(); i++) {
            CountryReport a = top.get(i - 1);
            CountryReport b = top.get(i);
            assertTrue(a.getPopulation() >= b.getPopulation(),
                "List must be sorted DESC by population");
            assertNotNull(b.getCapital(), "Capital field must be present");
            assertFalse(b.getCapital().isBlank(), "Capital must not be blank");
        }
    }
}
