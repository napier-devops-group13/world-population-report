package com.group13.population.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CountryTest {

    @Test
    void constructorAndGettersReturnValues() {
        Country c = new Country(
            "CHN",
            "China",
            "Asia",
            "Eastern Asia",
            1_277_558_000L,
            "Beijing"
        );

        assertEquals("CHN", c.getCode());
        assertEquals("China", c.getName());
        assertEquals("Asia", c.getContinent());
        assertEquals("Eastern Asia", c.getRegion());
        assertEquals(1_277_558_000L, c.getPopulation());
        assertEquals("Beijing", c.getCapital());
    }

    @Test
    void equalsAndHashCodeWorkForSameValues() {
        Country a = new Country("GBR", "United Kingdom",
            "Europe", "British Islands", 59_115_000L, "London");
        Country b = new Country("GBR", "United Kingdom",
            "Europe", "British Islands", 59_115_000L, "London");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equalsReturnsFalseForDifferentValues() {
        Country a = new Country("GBR", "United Kingdom",
            "Europe", "British Islands", 59_115_000L, "London");
        Country b = new Country("FRA", "France",
            "Europe", "Western Europe", 58_518_395L, "Paris");

        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

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

        String s = c.toString();
        assertTrue(s.contains("MMR"));
        assertTrue(s.contains("Myanmar"));
        assertTrue(s.contains("Asia"));
        assertTrue(s.contains("Southeast Asia"));
        assertTrue(s.contains("45"));
        assertTrue(s.contains("Naypyidaw"));
    }
}
