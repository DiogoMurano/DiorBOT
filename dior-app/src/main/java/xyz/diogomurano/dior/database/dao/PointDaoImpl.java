package xyz.diogomurano.dior.database.dao;

import xyz.diogomurano.dior.collaborator.Point;
import xyz.diogomurano.dior.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PointDaoImpl implements PointDao {

    private final DatabaseConnection connection;

    public PointDaoImpl(DatabaseConnection connection) {
        this.connection = connection;
    }

    @Override
    public Point findByName(String name) {
        Point point = null;
        try (PreparedStatement stmt = getConnection().prepareStatement("SELECT * FROM `point` WHERE `name`=?")) {
            stmt.setString(1, name);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                point = Point.builder()
                        .habboName(name)
                        .points(rs.getInt("points"))
                        .build();
            }
            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return point;
    }

    @Override
    public void register(Point point) {
        if (findByName(point.getHabboName()) != null) {
            try (PreparedStatement stmt = getConnection().prepareStatement("UPDATE `point` SET `points`=? WHERE `name`=?")) {
                stmt.setInt(1, point.getPoints());
                stmt.setString(2, point.getHabboName());
                stmt.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } else {
            try (PreparedStatement stmt = getConnection().prepareStatement("INSERT INTO `point` (`name`, `points`) VALUES (?, ?)")) {
                stmt.setString(1, point.getHabboName());
                stmt.setInt(2, point.getPoints());
                stmt.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    @Override
    public List<Point> selectTopPoints() {
        List<Point> points = new ArrayList<>();

        try (PreparedStatement stmt = getConnection().prepareStatement("SELECT * FROM `point` ORDER BY `points` DESC LIMIT 10;")) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                points.add(Point.builder()
                        .habboName(rs.getString("name"))
                        .points(rs.getInt("points"))
                        .build());
            }
            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return points;
    }

    private Connection getConnection() {
        return connection.getConnection();
    }
}
