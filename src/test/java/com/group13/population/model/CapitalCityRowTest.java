package com.group13.population.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CapitalCityRow}.
 *
 * <p>These tests exercise:</p>
 * <ul>
 *   <li>Happy-path construction and getters.</li>
 *   <li>Validation of required fields (name, country).</li>
 *   <li>Validation of population &gt;= 0.</li>
 *   <li>Value semantics: {@code equals}, {@code hashCode}, {@code toString}.</li>
 * </ul>
 *
 * <p>Fields match the coursework capital city report specification:
 * Name, Country, Population.</p>
 */
public class CapitalCityRowTest {

    @Test
    @DisplayName("Constructor stores all fields and trims input")
    void constructorStoresFields() {
        CapitalCityRow row = new CapitalCityRow(
            " London ",
            " United Kingdom ",
            7285000L
        );

        assertEquals("London", row.getName());
        assertEquals("United Kingdom", row.getCountry());
        assertEquals(7285000L, row.getPopulation());
    }

    @Test
    @DisplayName("Null required fields are rejected with NullPointerException")
    void rejectsNullRequiredFields() {
        assertThrows(NullPointerException.class,
            () -> new CapitalCityRow(null, "Country", 1L));

        assertThrows(NullPointerException.class,
            () -> new CapitalCityRow("Name", null, 1L));
    }

    @Test
    @DisplayName("Blank required fields are rejected with IllegalArgumentException")
    void rejectsBlankRequiredFields() {
        assertThrows(IllegalArgumentException.class,
            () -> new CapitalCityRow("   ", "Country", 1L));

        assertThrows(IllegalArgumentException.class,
            () -> new CapitalCityRow("Name", "   ", 1L));
    }

    @Test
    @DisplayName("Negative population is rejected")
    void rejectsNegativePopulation() {
        assertThrows(IllegalArgumentException.class,
            () -> new CapitalCityRow("London", "United Kingdom", -1L));
    }

    @Test
    @DisplayName("Two rows with same data are equal and have same hashCode")
    void equalsAndHashCodeForSameValues() {
        CapitalCityRow a = new CapitalCityRow(
            "London", "United Kingdom", 7285000L);
        CapitalCityRow b = new CapitalCityRow(
            "London", "United Kingdom", 7285000L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("Changing any field makes rows not equal")
    void notEqualWhenFieldsDiffer() {
        CapitalCityRow base = new CapitalCityRow(
            "London", "United Kingdom", 7285000L);

        CapitalCityRow differentName = new CapitalCityRow(
            "Edinburgh", "United Kingdom", 7285000L);
        CapitalCityRow differentCountry = new CapitalCityRow(
            "London", "France", 7285000L);
        CapitalCityRow differentPopulation = new CapitalCityRow(
            "London", "United Kingdom", 1L);

        assertNotEquals(base, differentName);
        assertNotEquals(base, differentCountry);
        assertNotEquals(base, differentPopulation);
    }

    @Test
    @DisplayName("toString includes key fields for debugging")
    void toStringIncludesFields() {
        CapitalCityRow row = new CapitalCityRow(
            "London", "United Kingdom", 7285000L);

        String text = row.toString();

        assertTrue(text.contains("London"));
        assertTrue(text.contains("United Kingdom"));
        assertTrue(text.contains("7285000"));
    }

    @Test
    @DisplayName("equals is reflexive and handles non-CapitalCityRow objects")
    void equalsHandlesSelfAndOtherTypes() {
        CapitalCityRow row = new CapitalCityRow(
            "London", "United Kingdom", 7285000L);

        // reflexive: hits the `this == other` branch
        assertEquals(row, row);

        // comparison with non-CapitalCityRow: hits the `instanceof` false branch
        assertNotEquals(row, "not-a-capital-row");
    }
}
