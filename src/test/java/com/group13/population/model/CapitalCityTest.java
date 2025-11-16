package com.group13.population.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link CapitalCity} model used for the
 * capital city reports (R17–R22).
 *
 * Covers constructor/getters, equality, and string representation.
 */
class CapitalCityTest {

    /**
     * Happy-path test: constructor should store all fields
     * and the getters must return the same values.
     */
    @Test
    void constructorAndGettersReturnValues() {
        // Arrange & act: create a sample capital city
        CapitalCity c = new CapitalCity(
            "Beijing",
            "China",
            21_540_000L
        );

        // Assert: every getter returns what we passed into the constructor
        assertEquals("Beijing", c.getName());
        assertEquals("China", c.getCountry());
        assertEquals(21_540_000L, c.getPopulation());
    }

    /**
     * Two CapitalCity instances with the same field values must be
     * equal and share the same hash code (Java equality contract).
     */
    @Test
    void equalsAndHashCodeWorkForSameValues() {
        CapitalCity a = new CapitalCity("London", "United Kingdom", 8_961_989L);
        CapitalCity b = new CapitalCity("London", "United Kingdom", 8_961_989L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    /**
     * Capital cities with different data must not be equal and
     * should normally produce different hash codes.
     */
    @Test
    void equalsReturnsFalseForDifferentValues() {
        CapitalCity a = new CapitalCity("London", "United Kingdom", 8_961_989L);
        CapitalCity b = new CapitalCity("Paris", "France", 2_148_000L);

        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    /**
     * toString() should include the key fields so that a CapitalCity
     * is easy to read in logs and debugging output.
     */
    @Test
    void toStringContainsAllMainFields() {
        CapitalCity c = new CapitalCity(
            "Naypyidaw",
            "Myanmar",
            924_608L
        );

        String s = c.toString();
        assertTrue(s.contains("Naypyidaw"));
        assertTrue(s.contains("Myanmar"));
        assertTrue(s.contains("924")); // population
    }
}
