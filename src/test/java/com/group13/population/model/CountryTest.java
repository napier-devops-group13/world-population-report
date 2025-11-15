package com.group13.population.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Country} model.
 * Covers constructor/getters, equality, and string representation.
 */
class CountryTest {

    /**
     * Happy-path test: constructor should store all fields
     * and the getters must return the same values.
     */
    @Test
    void constructorAndGettersReturnValues() {
        // Arrange & act: create a sample Country
        Country c = new Country(
            "CHN",
            "China",
            "Asia",
            "Eastern Asia",
            1_277_558_000L,
            "Beijing"
        );

        // Assert: every getter returns what we passed into the constructor
        assertEquals("CHN", c.getCode());
        assertEquals("China", c.getName());
        assertEquals("Asia", c.getContinent());
        assertEquals("Eastern Asia", c.getRegion());
        assertEquals(1_277_558_000L, c.getPopulation());
        assertEquals("Beijing", c.getCapital());
    }

    /**
     * Two Country instances with the same field values must be
     * equal and share the same hash code (Java equality contract).
     */
    @Test
    void equalsAndHashCodeWorkForSameValues() {
        Country a = new Country("GBR", "United Kingdom",
            "Europe", "British Islands", 59_115_000L, "London");
        Country b = new Country("GBR", "United Kingdom",
            "Europe", "British Islands", 59_115_000L, "London");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    /**
     * Countries with different data must not be equal and
     * should normally produce different hash codes.
     */
    @Test
    void equalsReturnsFalseForDifferentValues() {
        Country a = new Country("GBR", "United Kingdom",
            "Europe", "British Islands", 59_115_000L, "London");
        Country b = new Country("FRA", "France",
            "Europe", "Western Europe", 58_518_395L, "Paris");

        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    /**
     * toString() should include the key fields so that a Country
     * is easy to read in logs and debugging output.
     */
    @Test
    void toStringContainsAllMainFields() {
        Country c = new Country(
            "MMR",
            "Myanmar",
            "Asia",
            "Southeast Asia",
            45_000_000L,
            "Naypyidaw"
        );

        // Assert: string representation mentions each important field
        String s = c.toString();
        assertTrue(s.contains("MMR"));
        assertTrue(s.contains("Myanmar"));
        assertTrue(s.contains("Asia"));
        assertTrue(s.contains("Southeast Asia"));
        assertTrue(s.contains("45"));
        assertTrue(s.contains("Naypyidaw"));
    }
}
