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

    /* ---------- Public API used by CountryService ---------- */

    public List<CountryRow> allCountriesWorld() {
        final String sql = SELECT + ORDER_BY_POP_DESC_NAME_ASC;
        // Empty binder must still satisfy Checkstyle's whitespace rules
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

    /* ---------- JDBC plumbing (self-contained, no external Db class) ---------- */

    private interface Binder {
        void bind(PreparedStatement ps) throws Exception;
    }

    private List<CountryRow> run(String sql, Binder binder) {
        List<CountryRow> out = new ArrayList<>();
        try (Connection conn = open();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new CountryRow(
                        rs.getString("Code"),
                        rs.getString("Name"),
                        rs.getString("Continent"),
                        rs.getString("Region"),
                        rs.getLong("Population"),
                        rs.getString("CapitalName") // may be null
                    ));
                }
            }
        } catch (Exception e) {
            // keep it simple for coursework: return empty on failure
            // (alternatively wrap in RuntimeException)
        }
        return out;
    }

    private Connection open() throws Exception {
        // Pull from env with sensible fallbacks for IDE vs docker-compose
        String host = env("DB_HOST", "localhost");
        int port = Integer.parseInt(env("DB_PORT", "43306")); // IDE default; compose app→db would be 3306
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
    }
}
