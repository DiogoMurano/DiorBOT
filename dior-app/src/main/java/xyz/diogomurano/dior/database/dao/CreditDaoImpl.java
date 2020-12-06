package xyz.diogomurano.dior.database.dao;

import xyz.diogomurano.dior.collaborator.Credit;
import xyz.diogomurano.dior.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CreditDaoImpl implements CreditDao {

    private final DatabaseConnection connection;

    public CreditDaoImpl(DatabaseConnection connection) {
        this.connection = connection;
    }

    @Override
    public Credit findByName(String name) {
        Credit credit = null;
        try (PreparedStatement stmt = getConnection().prepareStatement("SELECT * FROM `credit` WHERE `name`=?")) {
            stmt.setString(1, name);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                credit = Credit.builder()
                        .habboName(name)
                        .coins(rs.getInt("coins"))
                        .build();
            }
            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return credit;
    }

    @Override
    public void register(Credit credit) {
        if (findByName(credit.getHabboName()) != null) {
            try (PreparedStatement stmt = getConnection().prepareStatement("UPDATE `credit` SET `coins`=? WHERE `name`=?")) {
                stmt.setInt(1, credit.getCoins());
                stmt.setString(2, credit.getHabboName());
                stmt.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } else {
            try (PreparedStatement stmt = getConnection().prepareStatement("INSERT INTO `credit` (`name`, `coins`) VALUES (?, ?)")) {
                stmt.setString(1, credit.getHabboName());
                stmt.setInt(2, credit.getCoins());
                stmt.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    private Connection getConnection() {
        return connection.getConnection();
    }
}
