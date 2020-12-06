package xyz.diogomurano.dior.database.dao;

import xyz.diogomurano.dior.collaborator.Role;
import xyz.diogomurano.dior.database.DatabaseConnection;
import xyz.diogomurano.dior.database.dto.PromotionDto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PromotionDaoImpl implements PromotionDao {

    private final DatabaseConnection connection;
    private boolean register;

    public PromotionDaoImpl(DatabaseConnection connection) {
        this.connection = connection;
        this.register = false;
    }

    @Override
    public int countEvaluations(String author, long startDate, long endDate) {
        int count = 0;
        try (PreparedStatement stmt = getConnection().prepareStatement("SELECT COUNT(*) FROM `promotion` WHERE " +
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
    public void register(PromotionDto promotionDto) {
        try (PreparedStatement stmt = getConnection().prepareStatement("INSERT INTO `promotion` (`author`, `target`," +
                " `role`, `date`) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, promotionDto.getAuthor());
            stmt.setString(2, promotionDto.getTarget());
            stmt.setInt(3, promotionDto.getRole().getId());
            stmt.setLong(4, promotionDto.getDate());
            stmt.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void registerGM() {
        if(register) {
            register(new PromotionDto("DMurano", "DMurano", Role.OWNER, System.currentTimeMillis()));
        }
    }

    private Connection getConnection() {
        return connection.getConnection();
    }
}
