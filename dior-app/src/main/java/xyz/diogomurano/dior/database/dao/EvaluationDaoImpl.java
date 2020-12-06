package xyz.diogomurano.dior.database.dao;

import xyz.diogomurano.dior.database.DatabaseConnection;
import xyz.diogomurano.dior.database.dto.EvaluationDto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EvaluationDaoImpl implements EvaluationDao {

    private final DatabaseConnection connection;

    public EvaluationDaoImpl(DatabaseConnection connection) {
        this.connection = connection;
    }

    @Override
    public int countEvaluations(String author, long startDate, long endDate) {
        int count = 0;
        try (PreparedStatement stmt = getConnection().prepareStatement("SELECT COUNT(*) FROM `evaluation` WHERE " +
                "`author`=? AND `date`>? AND `date`<?")) {
            stmt.setString(1, author);
            stmt.setLong(2, startDate);
            stmt.setLong(3, endDate);
            ResultSet rs = stmt.executeQuery();
            count = rs.getInt(1);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return count;
    }

    @Override
    public void register(EvaluationDto evaluationDto) {
        try (PreparedStatement stmt = getConnection().prepareStatement("INSERT INTO `evaluation` (`author`, `target`," +
                " `finalNote`, `date`) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, evaluationDto.getAuthor());
            stmt.setString(2, evaluationDto.getTarget());
            stmt.setDouble(3, evaluationDto.getFinalNote());
            stmt.setLong(4, evaluationDto.getDate());
            stmt.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private Connection getConnection() {
        return connection.getConnection();
    }
}
