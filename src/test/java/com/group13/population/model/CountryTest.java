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
    void equalsAndHashCodeUseAllFields() {
        Country a = new Country(
            "CHN", "China", "Asia", "Eastern Asia", 1_277_558_000L, "Beijing"
        );
        Country b = new Country(
            "CHN", "China", "Asia", "Eastern Asia", 1_277_558_000L, "Beijing"
        );
        Country differentPopulation = new Country(
            "CHN", "China", "Asia", "Eastern Asia", 123L, "Beijing"
        );

        // same data → equal
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        // different data / type / null → not equal
        assertNotEquals(a, differentPopulation);
        assertNotEquals(a.hashCode(), differentPopulation.hashCode());
        assertNotEquals(a, null);
        assertNotEquals(a, "not-a-country");
    }

    @Test
    void toStringContainsKeyFields() {
        Country c = new Country(
            "CHN", "China", "Asia", "Eastern Asia", 1_277_558_000L, "Beijing"
        );

        String s = c.toString();
        assertTrue(s.contains("CHN"));
        assertTrue(s.contains("China"));
        assertTrue(s.contains("Asia"));
        assertTrue(s.contains("Eastern Asia"));
        assertTrue(s.contains("Beijing"));
    }
}
