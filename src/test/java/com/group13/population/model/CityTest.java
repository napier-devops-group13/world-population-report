package com.group13.population.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link City}.
 *
 * Mirrors the style used for Country and CapitalCity model tests:
 *  - constructor + getters
 *  - static factory method
 *  - equals / hashCode
 *  - toString contains key fields
 */
class CityTest {

    @Test
    void constructorAndGettersExposeFields() {
        // given
        String name = "Kabul";
        String country = "Afghanistan";
        String district = "Kabol";
        long population = 1780000L;

        // when
        City city = new City(name, country, district, population);

        // then
        assertEquals(name, city.getName());
        assertEquals(country, city.getCountry());
        assertEquals(district, city.getDistrict());
        assertEquals(population, city.getPopulation());
    }

    @Test
    void staticFactoryMethodOfBuildsSameCity() {
        // given
        String name = "Edinburgh";
        String country = "United Kingdom";
        String district = "Scotland";
        long population = 488050L;

        // when
        City city = City.of(name, country, district, population);

        // then
        assertEquals(name, city.getName());
        assertEquals(country, city.getCountry());
        assertEquals(district, city.getDistrict());
        assertEquals(population, city.getPopulation());
    }

    @Test
    void equalsAndHashCodeMatchForSameValues() {
        // given
        City a = new City("Kabul", "Afghanistan", "Kabol", 1780000L);
        City b = new City("Kabul", "Afghanistan", "Kabol", 1780000L);

        // then
        assertEquals(a, b, "Cities with the same field values should be equal");
        assertEquals(a.hashCode(), b.hashCode(),
            "Equal cities must have the same hashCode");
    }

    @Test
    void equalsDetectsDifferentValues() {
        City base = new City("Kabul", "Afghanistan", "Kabol", 1780000L);

        City differentName = new City("Herat", "Afghanistan", "Herat", 272806L);
        City differentCountry = new City("Kabul", "TestCountry", "Kabol", 1780000L);
        City differentDistrict = new City("Kabul", "Afghanistan", "TestDistrict", 1780000L);
        City differentPopulation = new City("Kabul", "Afghanistan", "Kabol", 999999L);

        assertNotEquals(base, differentName);
        assertNotEquals(base, differentCountry);
        assertNotEquals(base, differentDistrict);
        assertNotEquals(base, differentPopulation);
        assertNotEquals(base, null);
        assertNotEquals(base, "not a city");
    }

    @Test
    void toStringContainsKeyFieldsForDebugging() {
        City city = new City("Kabul", "Afghanistan", "Kabol", 1780000L);

        String text = city.toString();
        assertNotNull(text);
        assertTrue(text.contains("Kabul"));
        assertTrue(text.contains("Afghanistan"));
        assertTrue(text.contains("Kabol"));
        assertTrue(text.contains("1780000"));
    }
}
