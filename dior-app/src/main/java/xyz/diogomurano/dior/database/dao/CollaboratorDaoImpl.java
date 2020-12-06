package xyz.diogomurano.dior.database.dao;

import xyz.diogomurano.dior.database.DatabaseConnection;
import xyz.diogomurano.dior.collaborator.Collaborator;
import xyz.diogomurano.dior.collaborator.CollaboratorImpl;
import xyz.diogomurano.dior.collaborator.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class CollaboratorDaoImpl implements CollaboratorDao {

    private final DatabaseConnection connection;

    public CollaboratorDaoImpl(DatabaseConnection connection) {
        this.connection = connection;

        createOrUpdate(new CollaboratorImpl("DMurano", "560256699118780418", 0,
                0, Role.OWNER, 0, 0, 0, 0));
    }

    @Override
    public Optional<Collaborator> findByHabboName(String habboName) {
        Collaborator collaborator = null;
        try (PreparedStatement stmt = getConnection().prepareStatement("SELECT * FROM `collaborator` WHERE `habbo_name`=?")) {
            stmt.setString(1, habboName);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                final Optional<Role> role = Role.findById(rs.getInt("role"));
                if (role.isPresent()) {
                    collaborator = new CollaboratorImpl(rs.getString("habbo_name"), rs
                            .getString("discord_id"), rs.getLong("join_date"), rs
                            .getLong("last_promote_date"), role.get(), rs.getInt("evaluation"), rs
                            .getInt("evaluation_count"), rs.getInt("interview"), rs
                            .getInt("interview_count"));
                }
            }
            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return Optional.ofNullable(collaborator);
    }

    @Override
    public Optional<Collaborator> findByDiscordId(String discordId) {
        Collaborator collaborator = null;
        try (PreparedStatement stmt = getConnection().prepareStatement("SELECT * FROM `collaborator` WHERE `discord_id`=?")) {
            stmt.setString(1, discordId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                final Optional<Role> role = Role.findById(rs.getInt("role"));
                if (role.isPresent()) {
                    collaborator = new CollaboratorImpl(rs.getString("habbo_name"), rs
                            .getString("discord_id"), rs.getLong("join_date"), rs
                            .getLong("last_promote_date"), role.get(), rs.getInt("evaluation"), rs
                            .getInt("evaluation_count"), rs.getInt("interview"), rs
                            .getInt("interview_count"));
                }
            }
            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return Optional.ofNullable(collaborator);
    }

    @Override
    public void createOrUpdate(Collaborator collaborator) {
        if (findByHabboName(collaborator.getHabboName()).isPresent()) {
            try (PreparedStatement stmt = getConnection().prepareStatement("UPDATE `collaborator` SET `discord_id`=?," +
                    " `role`=?, `last_promote_date`=?, `evaluation`=?, `evaluation_count`=?, `interview`=?, `interview_count`=?" +
                    " WHERE `habbo_name`=?")) {
                stmt.setString(1, collaborator.getDiscordId());
                stmt.setInt(2, collaborator.getRole().getId());
                stmt.setLong(3, collaborator.getLastPromoteDate());
                stmt.setInt(4, collaborator.getEvaluation());
                stmt.setInt(5, collaborator.getEvaluationCount());
                stmt.setInt(6, collaborator.getInterview());
                stmt.setInt(7, collaborator.getInterviewCount());
                stmt.setString(8, collaborator.getHabboName());
                stmt.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } else {
            try (PreparedStatement stmt = getConnection().prepareStatement("INSERT INTO `collaborator` (`habbo_name`, " +
                    "`discord_id`, `role`, `join_date`, `last_promote_date`) VALUES (?, ?, ?, ?, ?)")) {
                stmt.setString(1, collaborator.getHabboName());
                stmt.setString(2, collaborator.getDiscordId());
                stmt.setInt(3, collaborator.getRole().getId());
                stmt.setLong(4, collaborator.getJoinDate());
                stmt.setLong(5, collaborator.getLastPromoteDate());
                stmt.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

    }

    @Override
    public void delete(Collaborator collaborator) {
        try (PreparedStatement stmt = getConnection().prepareStatement("DELETE FROM `collaborator` WHERE `habbo_name`=?")) {
            stmt.setString(1, collaborator.getHabboName());
            stmt.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private Connection getConnection() {
        return connection.getConnection();
    }

}
