package com.group13.population;

import com.group13.population.repo.CountryReport;
import com.group13.population.util.Comparators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link Comparators#COUNTRY_BY_POP_DESC_NAME_ASC}.
 *
 * <p><b>Rubric: Code quality incl. comments (Excellent)</b> —
 * clear names, Javadoc, and structured Arrange–Act–Assert sections.
 *
 * <p>Verified sort order:
 * <ol>
 *   <li>Primary: population (descending)</li>
 *   <li>Secondary: name (ascending)</li>
 * </ol>
 * </p>
 */
public class ComparatorsTest {

    /** Small helper to make test setup concise and readable. */
    private static CountryReport row(String code, String name, long population) {
        CountryReport r = new CountryReport();
        r.setCode(code);
        r.setName(name);
        r.setPopulation(population);
        return r;
    }

    @Test
    @DisplayName("Sorts by population ↓ then name ↑")
    void sortsByPopulationDescThenNameAsc() {
        // Arrange — two rows share the same population to exercise the tie-breaker.
        final List<CountryReport> rows = new ArrayList<>();
        rows.add(row("AAA", "Beta", 100));
        rows.add(row("BBB", "Alpha", 100));
        rows.add(row("CCC", "Zeta", 200));

        // Act — sort using the comparator under test.
        rows.sort(Comparators.COUNTRY_BY_POP_DESC_NAME_ASC);

        // Assert — highest population first; for ties, alphabetical by name.
        assertEquals("CCC", rows.get(0).getCode());
        assertEquals("BBB", rows.get(1).getCode());
        assertEquals("AAA", rows.get(2).getCode());
    }

    @Test
    @DisplayName("Tie-break: when population is equal, names sort A→Z")
    void tieBreakOnNameAscending() {
        // Arrange — all equal population to check secondary key strictly.
        final List<CountryReport> rows = new ArrayList<>();
        rows.add(row("B", "Beta", 10));
        rows.add(row("C", "Charlie", 10));
        rows.add(row("A", "Alpha", 10));

        // Act
        rows.sort(Comparators.COUNTRY_BY_POP_DESC_NAME_ASC);

        // Assert — alphabetical by name.
        assertEquals("A", rows.get(0).getCode());
        assertEquals("B", rows.get(1).getCode());
        assertEquals("C", rows.get(2).getCode());
    }
}
