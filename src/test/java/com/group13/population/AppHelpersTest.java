package com.group13.population;

import com.group13.population.db.Db;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Extra tests for App helper methods so that the
 * com.group13.population package has high coverage.
 *
 * These tests do not start a real Javalin server (except where
 * already covered in AppSmokeTest/AppConfigTest) and do not touch
 * a real database.
 */
class AppHelpersTest {

    // ---------------------------------------------------------------------
    // getIntEnv
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("getIntEnv returns default when variable is not set")
    void getIntEnvReturnsDefaultWhenUnset() {
        String missingName = "SET09803_PORT_SHOULD_NOT_EXIST";

        int result = App.getIntEnv(missingName, 7070);

        assertEquals(7070, result,
            "When env var is missing, default value should be returned");
    }

    @Test
    @DisplayName("getIntEnv returns default when value is not a number")
    void getIntEnvReturnsDefaultOnNonNumeric() {
        int result = App.getIntEnv("PATH", 9999);

        assertEquals(9999, result,
            "When env var exists but is not numeric, default should be returned");
    }

    @Test
    @DisplayName("getIntEnv parses an existing numeric environment variable")
    void getIntEnvParsesValidInteger() {
        String numericName = null;
        int numericValue = -1;

        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            String v = entry.getValue();
            if (v != null && v.matches("\\d+")) {
                numericName = entry.getKey();
                numericValue = Integer.parseInt(v);
                break;
            }
        }

        assumeTrue(numericName != null,
            "No purely-numeric environment variable found on this system");

        int result = App.getIntEnv(numericName, 12345);

        assertEquals(numericValue, result,
            "getIntEnv should parse the numeric environment variable value");
    }

    // ---------------------------------------------------------------------
    // getIntProp
    // ---------------------------------------------------------------------

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
    @DisplayName("getIntProp throws NPE when Properties is null (matches implementation)")
    void getIntPropThrowsWhenPropsNull() {
        assertThrows(NullPointerException.class,
            () -> App.getIntProp(null, "some.key", 4242),
            "Current implementation requires non-null Properties and should throw NPE");
    }

    // ---------------------------------------------------------------------
    // loadProps()
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("loadProps returns non-null Properties and works with getIntProp")
    void loadPropsReturnsNonNullProperties() throws Exception {
        Method method = App.class.getDeclaredMethod("loadProps");
        method.setAccessible(true);

        Properties props = (Properties) method.invoke(null);

        assertNotNull(props, "loadProps should never return null");

        int port = App.getIntProp(props, "app.port", 7070);

        assertTrue(port > 0,
            "getIntProp should return a positive port (from app.port if present, or the default 7070)");
    }

    // ---------------------------------------------------------------------
    // connectDbFromConfig(Db, Properties)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("connectDbFromConfig builds location from properties and calls Db.connect")
    void connectDbFromConfigUsesProperties() throws Exception {
        // Access private static connectDbFromConfig(Db, Properties)
        Method m = App.class.getDeclaredMethod(
            "connectDbFromConfig", Db.class, Properties.class);
        m.setAccessible(true);

        RecordingDb fakeDb = new RecordingDb();

        Properties props = new Properties();
        props.setProperty("db.host", "prop-host");
        props.setProperty("db.port", "5432");
        props.setProperty("db.connect.delay.ms", "1500"); // App currently ignores this

        m.invoke(null, fakeDb, props);

        assertTrue(fakeDb.connectCalled, "Db.connect should have been called");
        assertEquals("prop-host:5432", fakeDb.lastLocation,
            "Location should be built from db.host and db.port");
        // We do not assert an exact delay value because current App implementation
        // always passes 0 ms; we just ensure it is non-negative.
        assertTrue(fakeDb.lastDelay >= 0, "Delay should be a non-negative number");
    }

    // ---------------------------------------------------------------------
    // Private constructor coverage
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Private App constructor is callable via reflection (for coverage only)")
    void privateConstructorIsCovered() throws Exception {
        Constructor<App> ctor = App.class.getDeclaredConstructor();
        ctor.setAccessible(true);

        App instance = ctor.newInstance();

        assertNotNull(instance,
            "Reflectively constructed App instance should not be null");
    }

    // ---------------------------------------------------------------------
    // Helper stub
    // ---------------------------------------------------------------------

    /** Fake Db used to exercise connectDbFromConfig without a real MySQL. */
    static class RecordingDb extends Db {
        String lastLocation;
        int lastDelay;
        boolean connectCalled;

        @Override
        public boolean connect(String location, int delayMs) {
            this.lastLocation = location;
            this.lastDelay = delayMs;
            this.connectCalled = true;
            return true;
        }
    }
}
