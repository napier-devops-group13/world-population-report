package com.group13.population;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Extra tests for App helper methods so that the
 * com.group13.population package has high coverage.
 *
 * These tests do not start a real Javalin server.
 * They also do not touch the database configuration.
 */
class AppHelpersTest {

    @Test
    @DisplayName("getIntEnv returns default when variable is not set")
    void getIntEnvReturnsDefaultWhenUnset() {
        // This env var name is very unlikely to exist
        String missingName = "SET09803_PORT_SHOULD_NOT_EXIST";

        int result = App.getIntEnv(missingName, 7070);

        assertEquals(7070, result,
            "When env var is missing, default value should be returned");
    }

    @Test
    @DisplayName("getIntEnv returns default when value is not a number")
    void getIntEnvReturnsDefaultOnNonNumeric() {
        // PATH is almost always present and non-numeric
        int result = App.getIntEnv("PATH", 9999);

        assertEquals(9999, result,
            "When env var exists but is not numeric, default should be returned");
    }

    @Test
    @DisplayName("getIntProp parses valid integer from properties")
    void getIntPropParsesValidInteger() {
        Properties props = new Properties();
        props.setProperty("app.port", "7050");

        int result = App.getIntProp(props, "app.port", 1);

        assertEquals(7050, result,
            "Valid numeric property should be parsed correctly");
    }

    @Test
    @DisplayName("getIntProp returns default when key missing or value invalid")
    void getIntPropReturnsDefaultForMissingOrBad() {
        Properties props = new Properties();
        props.setProperty("bad.port", "not-a-number");

        int fromMissing = App.getIntProp(props, "missing.key", 1234);
        int fromBad = App.getIntProp(props, "bad.port", 1234);

        assertEquals(1234, fromMissing,
            "Missing property should return default value");
        assertEquals(1234, fromBad,
            "Non-numeric property should return default value");
    }

    @Test
    @DisplayName("loadProps returns non-null Properties and works with getIntProp")
    void loadPropsReturnsNonNullProperties() throws Exception {
        // Call the private loadProps() via reflection so we get coverage
        Method method = App.class.getDeclaredMethod("loadProps");
        method.setAccessible(true);

        Properties props = (Properties) method.invoke(null);

        assertNotNull(props, "loadProps should never return null");

        // We don't assume app.port is defined in app.properties.
        // Instead, we verify getIntProp can safely read it with a sensible default.
        int port = App.getIntProp(props, "app.port", 7070);

        assertTrue(port > 0,
            "getIntProp should return a positive port (from app.port if present, or the default 7070)");
    }

    @Test
    @DisplayName("Private App constructor is callable via reflection (for coverage only)")
    void privateConstructorIsCovered() throws Exception {
        Constructor<App> ctor = App.class.getDeclaredConstructor();
        ctor.setAccessible(true);

        App instance = ctor.newInstance();

        assertNotNull(instance,
            "Reflectively constructed App instance should not be null");
    }
}
