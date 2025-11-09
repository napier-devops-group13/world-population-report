package com.group13.population.repo;

import com.group13.population.db.Db;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Data access for country reports (R01–R06).
 * Produces rows with: Code, Name, Continent, Region, Population, Capital.
 */
public class WorldRepo {

    private static final String SELECT =
        "SELECT c.Code AS Code, "
            + "       c.Name AS Name, "
            + "       c.Continent AS Continent, "
            + "       c.Region AS Region, "
            + "       c.Population AS Population, "
            + "       cap.Name AS CapitalName "
            + "FROM country c "
            + "LEFT JOIN city cap ON cap.ID = c.Capital";

    private static final String ORDER_BY_NAME_ASC = " ORDER BY c.Name ASC";
    private static final String ORDER_BY_POP_DESC_NAME_ASC =
        " ORDER BY c.Population DESC, c.Name ASC";

    // Locale-consistent “Name ASC” to match tests exactly.
    private static final Collator EN_COLLATOR = Collator.getInstance(Locale.ENGLISH);
    private static final Comparator<CountryReport> NAME_ASC =
        Comparator.comparing(CountryReport::getName, EN_COLLATOR);

    private final Db db;

    public WorldRepo(final Db db) {
        this.db = db;
    }

    // R01–R03 (default Name ASC) with optional by-population variants

    public List<CountryReport> countriesWorld() throws SQLException {
        final String sql = SELECT + ORDER_BY_NAME_ASC + ";";
        List<CountryReport> rows = query(sql);
        rows.sort(NAME_ASC);
        return rows;
    }

    public List<CountryReport> countriesWorldByPopulation() throws SQLException {
        final String sql = SELECT + ORDER_BY_POP_DESC_NAME_ASC + ";";
        return query(sql);
    }

    public List<CountryReport> countriesByContinent(final String continent) throws SQLException {
        final String sql = SELECT + " WHERE c.Continent = ?" + ORDER_BY_NAME_ASC + ";";
        List<CountryReport> rows = query(sql, continent);
        rows.sort(NAME_ASC);
        return rows;
    }

    public List<CountryReport> countriesByContinentByPopulation(final String continent)
        throws SQLException {
        final String sql = SELECT + " WHERE c.Continent = ?" + ORDER_BY_POP_DESC_NAME_ASC + ";";
        return query(sql, continent);
    }

    public List<CountryReport> countriesByRegion(final String region) throws SQLException {
        final String sql = SELECT + " WHERE c.Region = ?" + ORDER_BY_NAME_ASC + ";";
        List<CountryReport> rows = query(sql, region);
        rows.sort(NAME_ASC);
        return rows;
    }

    public List<CountryReport> countriesByRegionByPopulation(final String region)
        throws SQLException {
        final String sql = SELECT + " WHERE c.Region = ?" + ORDER_BY_POP_DESC_NAME_ASC + ";";
        return query(sql, region);
    }

    // R04–R06: Top-N by Population (tie-break Name)

    public List<CountryReport> topCountriesWorld(final int n) throws SQLException {
        final String sql = SELECT + ORDER_BY_POP_DESC_NAME_ASC + " LIMIT ?;";
        return query(sql, n);
    }

    public List<CountryReport> topCountriesByContinent(final String continent, final int n)
        throws SQLException {
        final String sql =
            SELECT + " WHERE c.Continent = ?" + ORDER_BY_POP_DESC_NAME_ASC + " LIMIT ?;";
        return query(sql, continent, n);
    }

    public List<CountryReport> topCountriesByRegion(final String region, final int n)
        throws SQLException {
        final String sql =
            SELECT + " WHERE c.Region = ?" + ORDER_BY_POP_DESC_NAME_ASC + " LIMIT ?;";
        return query(sql, region, n);
    }

    // -------------------------------- helpers --------------------------------

    private static CountryReport row(final ResultSet rs) throws SQLException {
        CountryReport r = new CountryReport();
        r.setCode(rs.getString("Code"));
        r.setName(rs.getString("Name"));
        r.setContinent(rs.getString("Continent"));
        r.setRegion(rs.getString("Region"));
        r.setPopulation(rs.getLong("Population"));
        r.setCapital(rs.getString("CapitalName")); // may be null
        return r;
    }

    private List<CountryReport> query(final String sql, final Object... params) throws SQLException {
        List<CountryReport> out = new ArrayList<>();
        try (Connection conn = db.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(row(rs));
                }
            }
        }
        return out;
    }
}
