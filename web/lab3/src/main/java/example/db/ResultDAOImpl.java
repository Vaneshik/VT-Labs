package example.db;

import example.entity.ResultEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class ResultDAOImpl implements ResultDAO {

    private static final String INSERT_RESULT =
            "INSERT INTO point_model (x, y, r, hit) VALUES (?, ?, ?, ?) RETURNING id";

    private static final String UPDATE_RESULT =
            "UPDATE point_model SET x = ?, y = ?, r = ?, hit = ? WHERE id = ?";

    private static final String SELECT_BY_ID =
            "SELECT id, x, y, r, hit FROM point_model WHERE id = ?";

    private static final String SELECT_ALL =
            "SELECT id, x, y, r, hit FROM point_model";

    private static final String DELETE_RESULT =
            "DELETE FROM point_model WHERE x = ? AND y = ? AND r = ? AND hit = ?";

    private static final String CLEAR_RESULTS =
            "DELETE FROM point_model";

    @Override
    public void addNewResult(ResultEntity result) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_RESULT)) {

            ps.setDouble(1, result.getX());
            ps.setDouble(2, result.getY());
            ps.setDouble(3, result.getR());
            ps.setBoolean(4, result.isHit());

            // Execute the statement and get the generated ID
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long generatedId = rs.getLong("id");
                    result.setId(generatedId); // Store it back in our object
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateResult(Long result_id, ResultEntity result) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_RESULT)) {

            ps.setDouble(1, result.getX());
            ps.setDouble(2, result.getY());
            ps.setDouble(3, result.getR());
            ps.setBoolean(4, result.isHit());
            ps.setLong(5, result_id);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ResultEntity getResultById(Long result_id) {
        ResultEntity entity = null;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {

            ps.setLong(1, result_id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id    = rs.getLong("id");
                    double x   = rs.getDouble("x");
                    double y   = rs.getDouble("y");
                    double r   = rs.getDouble("r");
                    boolean hit= rs.getBoolean("hit");

                    // Now use the constructor that sets 'id'
                    entity = new ResultEntity(id, x, y, r, hit);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return entity;
    }

    @Override
    public Collection<ResultEntity> getAllResults() {
        Collection<ResultEntity> results = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long id    = rs.getLong("id");
                double x   = rs.getDouble("x");
                double y   = rs.getDouble("y");
                double r   = rs.getDouble("r");
                boolean hit= rs.getBoolean("hit");

                // Use the constructor that sets 'id'
                ResultEntity entity = new ResultEntity(id, x, y, r, hit);
                results.add(entity);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    @Override
    public void deleteResult(ResultEntity result) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_RESULT)) {

            ps.setDouble(1, result.getX());
            ps.setDouble(2, result.getY());
            ps.setDouble(3, result.getR());
            ps.setBoolean(4, result.isHit());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearResults() {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(CLEAR_RESULTS)) {

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        String url      = "jdbc:postgresql://localhost:5432/studs";
        String user     = "s409858";
        String password = "dmfMuVPpI4Rt35fX";
        return DriverManager.getConnection(url, user, password);
    }
}