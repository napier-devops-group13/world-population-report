package com.group13.population.repo;

import com.group13.population.db.Db;
import com.group13.population.model.CityRow;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guard tests for {@link CityRepo}.
 *
 * 1) databaseIsAvailableForCityRepo:
 *    - Checks that the MySQL DB is reachable on the configured host/port.
 *    - If not reachable, the test is skipped (so "mvn test" can still run
 *      locally without Docker).
 *
 * 2) Remaining tests exercise the validation/guard branches in CityRepo:
 *    - null & blank filter names -> IllegalArgumentException
 *    - non-positive limits       -> IllegalArgumentException
 *
 *    A Db stub throws if getConnection() is called so we prove the guards
 *    run *before* any DB access.
 */
public class CityRepoGuardTest {

    // ---------------------------------------------------------------------
    // 1. DB availability sanity check (original behaviour)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("CityRepoGuardTest – database is reachable (or test is skipped)")
    void databaseIsAvailableForCityRepo() {
        String host = getHost();
        int port = getPort();

        boolean reachable = isPortOpen(host, port, 2000);
        Assumptions.assumeTrue(
            reachable,
            () -> "Failed to connect to database for CityRepoGuardTest at "
                + host + ":" + port
        );
        // If reachable, the assumption passes and the test succeeds.
    }

    private String getHost() {
        String sys = System.getProperty("db.host");
        if (sys != null && !sys.isBlank()) {
            return sys;
        }
        String env = System.getenv("DB_HOST");
        if (env != null && !env.isBlank()) {
            return env;
        }
        return "localhost";
    }

    private int getPort() {
        String sys = System.getProperty("db.port");
        if (sys != null && !sys.isBlank()) {
            return Integer.parseInt(sys);
        }
        String env = System.getenv("DB_PORT");
        if (env != null && !env.isBlank()) {
            return Integer.parseInt(env);
        }
        return 43306; // default test DB port
    }

    private boolean isPortOpen(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    // ---------------------------------------------------------------------
    // 2. Stub Db used to ensure guards prevent DB access
    // ---------------------------------------------------------------------

    /**
     * Db stub that fails the test if any method tries to obtain a real
     * JDBC connection. This lets us prove that guard clauses prevent
     * database access for invalid inputs.
     */
    private static final class FailingDb extends Db {
        @Override
        public Connection getConnection() throws SQLException {
            throw new AssertionError(
                "Db.getConnection() should not be called for invalid CityRepo inputs");
        }
    }

    // ---------------------------------------------------------------------
    // 3. Name-validation guards:
    //    null & blank -> IllegalArgumentException
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Null or blank continent throws IllegalArgumentException")
    void nullOrBlankContinentReturnsEmptyList() {
        CityRepo repo = new CityRepo(new FailingDb());

        // null continent
        assertThrows(IllegalArgumentException.class,
            () -> repo.findCitiesInContinentByPopulationDesc(null));

        // blank continent ("   ")
        assertThrows(IllegalArgumentException.class,
            () -> repo.findCitiesInContinentByPopulationDesc("   "));
    }

    @Test
    @DisplayName("Null or blank region throws IllegalArgumentException")
    void nullOrBlankRegionReturnsEmptyList() {
        CityRepo repo = new CityRepo(new FailingDb());

        // null region
        assertThrows(IllegalArgumentException.class,
            () -> repo.findCitiesInRegionByPopulationDesc(null));

        // blank region
        assertThrows(IllegalArgumentException.class,
            () -> repo.findCitiesInRegionByPopulationDesc("   "));
    }

    @Test
    @DisplayName("Null or blank country throws IllegalArgumentException")
    void nullOrBlankCountryReturnsEmptyList() {
        CityRepo repo = new CityRepo(new FailingDb());

        // null country
        assertThrows(IllegalArgumentException.class,
            () -> repo.findCitiesInCountryByPopulationDesc(null));

        // blank country
        assertThrows(IllegalArgumentException.class,
            () -> repo.findCitiesInCountryByPopulationDesc("   "));
    }

    @Test
    @DisplayName("Null or blank district throws IllegalArgumentException")
    void nullOrBlankDistrictReturnsEmptyList() {
        CityRepo repo = new CityRepo(new FailingDb());

        // null district
        assertThrows(IllegalArgumentException.class,
            () -> repo.findCitiesInDistrictByPopulationDesc(null));

        // blank district
        assertThrows(IllegalArgumentException.class,
            () -> repo.findCitiesInDistrictByPopulationDesc("   "));
    }

    // ---------------------------------------------------------------------
    // 4. Limit-validation guards: non-positive -> IllegalArgumentException
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Non-positive N for world top cities throws IllegalArgumentException")
    void nonPositiveLimitForWorldTopCities() {
        CityRepo repo = new CityRepo(new FailingDb());

        assertThrows(IllegalArgumentException.class,
            () -> repo.findTopCitiesInWorldByPopulationDesc(0));
        assertThrows(IllegalArgumentException.class,
            () -> repo.findTopCitiesInWorldByPopulationDesc(-5));
    }

    @Test
    @DisplayName("Non-positive N for filtered top-city queries throws IllegalArgumentException")
    void nonPositiveLimitForFilteredTopCities() {
        CityRepo repo = new CityRepo(new FailingDb());

        assertThrows(IllegalArgumentException.class,
            () -> repo.findTopCitiesInContinentByPopulationDesc("Europe", 0));
        assertThrows(IllegalArgumentException.class,
            () -> repo.findTopCitiesInRegionByPopulationDesc("Caribbean", 0));
        assertThrows(IllegalArgumentException.class,
            () -> repo.findTopCitiesInCountryByPopulationDesc("Japan", 0));
        assertThrows(IllegalArgumentException.class,
            () -> repo.findTopCitiesInDistrictByPopulationDesc("Tokyo-to", 0));
    }
}
