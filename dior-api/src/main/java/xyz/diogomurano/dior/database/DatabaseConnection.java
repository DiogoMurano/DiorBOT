package xyz.diogomurano.dior.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public interface DatabaseConnection {

    Connection getConnection();

    boolean isConnected();

    void setupConnection();

    default void createTables() {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS `collaborator` (`habbo_name` VARCHAR(32) PRIMARY KEY NOT NULL," +
                    " `discord_id` VARCHAR(36), `role` INT(2), `evaluation` INT(8) NOT NULL DEFAULT '0', `evaluation_count`" +
                    " INT(8) NOT NULL DEFAULT '0', `interview` INT(8) NOT NULL DEFAULT '0', `interview_count` INT(8) NOT" +
                    " NULL DEFAULT '0', `join_date` BIGINT(20), `last_promote_date` BIGINT(20))");
            stmt.execute("CREATE TABLE IF NOT EXISTS `annotation` (`author` VARCHAR(32) NOT NULL, `target` " +
                    "VARCHAR(32) NOT NULL, `reason` TEXT, `date` BIGINT(20) NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS `evaluation` (`author` VARCHAR(32) NOT NULL, `target` VARCHAR(32) " +
                    "NOT NULL, `finalNote` DECIMAL(8, 8) NOT NULL, `date` BIGINT(20) NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS `interview` (`author` VARCHAR(32) NOT NULL, `target` VARCHAR(32) " +
                    "NOT NULL, `finalNote` DECIMAL(2, 2) NOT NULL, `date` BIGINT(20) NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS `promotion` (`author` VARCHAR(32) NOT NULL, `target` VARCHAR(32) " +
                    "NOT NULL, `role` INT(2) NOT NULL, `date` BIGINT(20) NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS `credit` (`name` VARCHAR(32) NOT NULL, `coins` INT(4) " +
                    "NOT NULL DEFAULT 0)");
            stmt.execute("CREATE TABLE IF NOT EXISTS `point` (`name` VARCHAR(32) NOT NULL, `points` INT(4) " +
                    "NOT NULL DEFAULT 0)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
