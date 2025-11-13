package com.group13.population.repo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC repository for the 6 country reports (R01–R06).
 * Returns rows with: code, name, continent, region, population, capital.
 */
public class WorldRepo {

    /**
     * Optional shared connection injected from App / tests.
     * If this is non-null, the repo will use it and will NOT close it.
     * The caller (App / tests) is responsible for closing it.
     */
    private final Connection sharedConn;

    // Base projection used by all queries
    private static final String SELECT =
        "SELECT c.Code AS Code, "
            + "       c.Name AS Name, "
            + "       c.Continent AS Continent, "
            + "       c.Region AS Region, "
            + "       c.Population AS Population, "
            + "       cap.Name AS CapitalName "
            + "FROM country c "
            + "LEFT JOIN city cap ON cap.ID = c.Capital";

    // All R01–R06 require population DESC (tie-break name ASC)
    private static final String ORDER_BY_POP_DESC_NAME_ASC =
        " ORDER BY c.Population DESC, c.Name ASC";

    /* ---------- Constructors ---------- */

    /** Default constructor – used by the running app. */
    public WorldRepo() {
        this.sharedConn = null;
    }

    /**
     * Constructor used by integration tests (and optionally App).
     * The repository will use this connection but never close it.
     */
    public WorldRepo(Connection sharedConn) {
        this.sharedConn = sharedConn;
    }

    /* ---------- Public API used by CountryService ---------- */

    public List<CountryRow> allCountriesWorld() {
        final String sql = SELECT + ORDER_BY_POP_DESC_NAME_ASC;
        return run(sql, ps -> { });
    }

    public List<CountryRow> allCountriesContinent(String continent) {
        final String sql =
            SELECT + " WHERE c.Continent = ?" + ORDER_BY_POP_DESC_NAME_ASC;
        return run(sql, ps -> ps.setString(1, continent));
    }

    public List<CountryRow> allCountriesRegion(String region) {
        final String sql =
            SELECT + " WHERE c.Region = ?" + ORDER_BY_POP_DESC_NAME_ASC;
        return run(sql, ps -> ps.setString(1, region));
    }

    public List<CountryRow> topCountriesWorld(int n) {
        final String sql = SELECT + ORDER_BY_POP_DESC_NAME_ASC + " LIMIT ?";
        return run(sql, ps -> ps.setInt(1, n));
    }

    public List<CountryRow> topCountriesContinent(String continent, int n) {
        final String sql =
            SELECT + " WHERE c.Continent = ?"
                + ORDER_BY_POP_DESC_NAME_ASC + " LIMIT ?";
        return run(sql, ps -> {
            ps.setString(1, continent);
            ps.setInt(2, n);
        });
    }

    public List<CountryRow> topCountriesRegion(String region, int n) {
        final String sql =
            SELECT + " WHERE c.Region = ?"
                + ORDER_BY_POP_DESC_NAME_ASC + " LIMIT ?";
        return run(sql, ps -> {
            ps.setString(1, region);
            ps.setInt(2, n);
        });
    }

    /* --- “N” aliases so tests can call topNCountries*() --- */

    public List<CountryRow> topNCountriesWorld(int n) {
        return topCountriesWorld(n);
    }

    public List<CountryRow> topNCountriesContinent(String continent, int n) {
        return topCountriesContinent(continent, n);
    }

    public List<CountryRow> topNCountriesRegion(String region, int n) {
        return topCountriesRegion(region, n);
    }

    /* ---------- JDBC plumbing ---------- */

    private interface Binder {
        void bind(PreparedStatement ps) throws Exception;
    }

    private List<CountryRow> run(String sql, Binder binder) {
        List<CountryRow> out = new ArrayList<>();

        // If a shared connection was injected (tests / App), use it
        if (sharedConn != null) {
            try {
                try (PreparedStatement ps = sharedConn.prepareStatement(sql)) {
                    binder.bind(ps);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            out.add(mapRow(rs));
                        }
                    }
                }
            } catch (Exception e) {
                // Keep it simple for coursework: swallow and return empty list
            }
            return out;
        }

        // Otherwise, open a new connection per call (used by default constructor)
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            // Same as above
        }
        return out;
    }

    private CountryRow mapRow(ResultSet rs) throws Exception {
        return new CountryRow(
            rs.getString("Code"),
            rs.getString("Name"),
            rs.getString("Continent"),
            rs.getString("Region"),
            rs.getLong("Population"),
            rs.getString("CapitalName") // may be null
        );
    }

    private Connection open() throws Exception {
        // Pull from env with sensible fallbacks for IDE vs docker-compose
        String host = env("DB_HOST", "localhost");
        int port = Integer.parseInt(env("DB_PORT", "43306")); // IDE default; compose app→db usually 3306
        String user = env("DB_USER", "root");
        String pass = env("DB_PASS", "example");
        String name = env("DB_NAME", "world");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + name
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, user, pass);
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v.trim();
    }

    /** JSON-friendly row; Jackson will serialize field names as shown. */
    public static final class CountryRow {
        public final String code;
        public final String name;
        public final String continent;
        public final String region;
        public final long population;
        public final String capital;

        public CountryRow(
            String code,
            String name,
            String continent,
            String region,
            long population,
            String capital
        ) {
            this.code = code;
            this.name = name;
            this.continent = continent;
            this.region = region;
            this.population = population;
            this.capital = capital;
        }

        // ---- Getters used by tests (e.g. rows.get(0).population()) ----

        public String code() {
            return code;
        }

        public String name() {
            return name;
        }

        public String continent() {
            return continent;
        }

        public String region() {
            return region;
        }

        public long population() {
            return population;
        }

        public String capital() {
            return capital;
        }
    }
}
