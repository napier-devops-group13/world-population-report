package com.group13.population.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CountryRow}.
 *
 * <p>These tests exercise:
 * <ul>
 *   <li>Happy-path construction and getters.</li>
 *   <li>Allowance of {@code null} and blank capital city.</li>
 *   <li>Validation of required fields and population &gt;= 0.</li>
 *   <li>Value semantics: {@code equals}, {@code hashCode}, {@code toString}.</li>
 *   <li>{@link CountryRow#fromResultSet(ResultSet)} without any mocking library.</li>
 * </ul>
 */
public class CountryRowTest {

    @Test
    @DisplayName("Constructor stores all fields and trims input")
    void constructorStoresFields() {
        CountryRow row = new CountryRow(
            " GBR ",
            " United Kingdom ",
            " Europe ",
            " British Islands ",
            59623400L,
            " London "
        );

        assertEquals("GBR", row.getCode());
        assertEquals("United Kingdom", row.getName());
        assertEquals("Europe", row.getContinent());
        assertEquals("British Islands", row.getRegion());
        assertEquals(59623400L, row.getPopulation());
        assertEquals("London", row.getCapital());
    }

    @Test
    @DisplayName("Capital may be null when database has no capital city")
    void allowsNullCapital() {
        CountryRow row = new CountryRow(
            "ATA",
            "Antarctica",
            "Antarctica",
            "Antarctica",
            0L,
            null
        );

        assertNull(row.getCapital(), "Capital should be allowed to be null");
    }

    @Test
    @DisplayName("Blank capital is normalised to null")
    void blankCapitalBecomesNull() {
        CountryRow row = new CountryRow(
            "GBR",
            "United Kingdom",
            "Europe",
            "British Islands",
            59623400L,
            "   "       // capital with only spaces
        );

        assertNull(row.getCapital(),
            "Blank capital should be normalised to null by normaliseOptional");
    }

    @Test
    @DisplayName("Null required fields are rejected with NullPointerException")
    void rejectsNullRequiredFields() {
        assertThrows(NullPointerException.class,
            () -> new CountryRow(null, "Name", "Continent", "Region", 1L, "Cap"));

        assertThrows(NullPointerException.class,
            () -> new CountryRow("CODE", null, "Continent", "Region", 1L, "Cap"));

        assertThrows(NullPointerException.class,
            () -> new CountryRow("CODE", "Name", null, "Region", 1L, "Cap"));

        assertThrows(NullPointerException.class,
            () -> new CountryRow("CODE", "Name", "Continent", null, 1L, "Cap"));
    }

    @Test
    @DisplayName("Blank required fields are rejected with IllegalArgumentException")
    void rejectsBlankRequiredFields() {
        assertThrows(IllegalArgumentException.class,
            () -> new CountryRow("   ", "Name", "Continent", "Region", 1L, "Cap"));

        assertThrows(IllegalArgumentException.class,
            () -> new CountryRow("CODE", "   ", "Continent", "Region", 1L, "Cap"));

        assertThrows(IllegalArgumentException.class,
            () -> new CountryRow("CODE", "Name", "   ", "Region", 1L, "Cap"));

        assertThrows(IllegalArgumentException.class,
            () -> new CountryRow("CODE", "Name", "Continent", "   ", 1L, "Cap"));
    }

    @Test
    @DisplayName("Negative population is rejected")
    void rejectsNegativePopulation() {
        assertThrows(IllegalArgumentException.class,
            () -> new CountryRow("GBR", "United Kingdom", "Europe",
                "British Islands", -1L, "London"));
    }

    @Test
    @DisplayName("Two rows with same data are equal and have same hashCode")
    void equalsAndHashCodeForSameValues() {
        CountryRow a = new CountryRow(
            "GBR", "United Kingdom", "Europe", "British Islands", 59623400L, "London");
        CountryRow b = new CountryRow(
            "GBR", "United Kingdom", "Europe", "British Islands", 59623400L, "London");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("Changing any field makes rows not equal")
    void notEqualWhenFieldsDiffer() {
        CountryRow base = new CountryRow(
            "GBR", "United Kingdom", "Europe", "British Islands", 59623400L, "London");

        CountryRow differentCode = new CountryRow(
            "FRA", "United Kingdom", "Europe", "British Islands", 59623400L, "London");
        CountryRow differentName = new CountryRow(
            "GBR", "UK", "Europe", "British Islands", 59623400L, "London");
        CountryRow differentContinent = new CountryRow(
            "GBR", "United Kingdom", "Other", "British Islands", 59623400L, "London");
        CountryRow differentRegion = new CountryRow(
            "GBR", "United Kingdom", "Europe", "Other", 59623400L, "London");
        CountryRow differentPopulation = new CountryRow(
            "GBR", "United Kingdom", "Europe", "British Islands", 1L, "London");
        CountryRow differentCapital = new CountryRow(
            "GBR", "United Kingdom", "Europe", "British Islands", 59623400L, "Edinburgh");

        assertNotEquals(base, differentCode);
        assertNotEquals(base, differentName);
        assertNotEquals(base, differentContinent);
        assertNotEquals(base, differentRegion);
        assertNotEquals(base, differentPopulation);
        assertNotEquals(base, differentCapital);
    }

    @Test
    @DisplayName("toString includes key fields for debugging")
    void toStringIncludesFields() {
        CountryRow row = new CountryRow(
            "GBR", "United Kingdom", "Europe", "British Islands", 59623400L, "London");

        String text = row.toString();

        assertTrue(text.contains("GBR"));
        assertTrue(text.contains("United Kingdom"));
        assertTrue(text.contains("Europe"));
        assertTrue(text.contains("British Islands"));
        assertTrue(text.contains("59623400"));
        assertTrue(text.contains("London"));
    }

    @Test
    @DisplayName("equals is reflexive and handles non-CountryRow objects")
    void equalsHandlesSelfAndOtherTypes() {
        CountryRow row = new CountryRow(
            "GBR", "United Kingdom", "Europe", "British Islands", 59623400L, "London");

        // reflexive: hits the `this == other` branch
        assertEquals(row, row);

        // comparison with non-CountryRow: hits the `instanceof` false branch
        assertNotEquals(row, "not-a-country-row");
    }

    // ---------------------------------------------------------------------
    // New tests: exercise fromResultSet(...) using a JDK dynamic proxy
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("fromResultSet maps JDBC columns correctly")
    void fromResultSetMapsJdbcColumns() throws SQLException {
        ResultSet rs = stubResultSet(
            "GBR",
            "United Kingdom",
            "Europe",
            "British Islands",
            59623400L,
            "London"
        );

        CountryRow row = CountryRow.fromResultSet(rs);

        assertEquals("GBR", row.getCode());
        assertEquals("United Kingdom", row.getName());
        assertEquals("Europe", row.getContinent());
        assertEquals("British Islands", row.getRegion());
        assertEquals(59623400L, row.getPopulation());
        assertEquals("London", row.getCapital());
    }

    @Test
    @DisplayName("fromResultSet allows null capital name")
    void fromResultSetAllowsNullCapital() throws SQLException {
        ResultSet rs = stubResultSet(
            "ATA",
            "Antarctica",
            "Antarctica",
            "Antarctica",
            0L,
            null
        );

        CountryRow row = CountryRow.fromResultSet(rs);

        assertNull(row.getCapital());
    }

    /**
     * Build a tiny ResultSet stub using a JDK dynamic proxy.
     * Only getString(label) and getLong(label) are implemented;
     * any other method will throw UnsupportedOperationException.
     */
    private ResultSet stubResultSet(
        String code,
        String name,
        String continent,
        String region,
        long population,
        String capital
    ) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String methodName = method.getName();

                if ("getString".equals(methodName)) {
                    String column = (String) args[0];
                    switch (column) {
                        case "Code":
                            return code;
                        case "Name":
                            return name;
                        case "Continent":
                            return continent;
                        case "Region":
                            return region;
                        case "CapitalName":
                            return capital;
                        default:
                            throw new SQLException("Unexpected column: " + column);
                    }
                }

                if ("getLong".equals(methodName)) {
                    String column = (String) args[0];
                    if ("Population".equals(column)) {
                        return population;
                    }
                    throw new SQLException("Unexpected column for getLong: " + column);
                }

                // We don't expect any other methods to be called in these tests.
                throw new UnsupportedOperationException(
                    "Method not supported in test stub: " + methodName);
            }
        };

        return (ResultSet) Proxy.newProxyInstance(
            CountryRowTest.class.getClassLoader(),
            new Class<?>[]{ResultSet.class},
            handler
        );
    }
}
