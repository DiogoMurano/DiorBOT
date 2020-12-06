package xyz.diogomurano.dior.database.dao;

import net.dv8tion.jda.api.entities.Guild;
import xyz.diogomurano.dior.api.DiscordAPI;
import xyz.diogomurano.dior.api.models.HabboUser;
import xyz.diogomurano.dior.database.DatabaseConnection;
import xyz.diogomurano.dior.database.dto.AnnotationDto;
import xyz.diogomurano.dior.api.HabboAPI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AnnotationDaoImpl implements AnnotationDao {

    private final DatabaseConnection connection;

    private static final ScheduledExecutorService POOL = Executors.newScheduledThreadPool(1);

    public AnnotationDaoImpl(DatabaseConnection connection) {
        this.connection = connection;

        POOL.scheduleWithFixedDelay(this::clearExpired, 3, 5, TimeUnit.HOURS);
    }

    @Override
    public int countAnnotation(String target) {
        int count = 0;
        try (PreparedStatement stmt = getConnection().prepareStatement("SELECT COUNT(*) FROM `annotation` WHERE `target`=?")) {
            stmt.setString(1, target);
            ResultSet rs = stmt.executeQuery();
            count = rs.getInt(1);
            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return count;
    }

    @Override
    public void register(AnnotationDto annotationDto) {
        try (PreparedStatement stmt = getConnection().prepareStatement("INSERT INTO `annotation` (`author`, `target`, `reason`, `date`) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, annotationDto.getAuthor());
            stmt.setString(2, annotationDto.getTarget());
            stmt.setString(3, annotationDto.getReason());
            stmt.setLong(4, annotationDto.getDate());
            stmt.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void clearExpired() {
        try (PreparedStatement stmt = getConnection().prepareStatement("SELECT * FROM `annotation` WHERE `date`<?")) {
            stmt.setLong(1, (long) (System.currentTimeMillis() - 2.592000000));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String author = rs.getString("author");
                String target = rs.getString("target");
                String reason = rs.getString("reason");
                long date = rs.getLong("date");

                final Guild guild = DiscordAPI.getGuild();
                HabboUser user = HabboAPI.getUser(target);
                guild.getTextChannelById("713818349619904514").sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setColor(guild.getSelfMember().getColor());
                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    embedBuilder.setTitle("Anotação de " + target);
                    embedBuilder.setDescription("O colaborador **" + target + "** teve sua anotação excluída, pois já foi passado o período de **1 mês**." +
                            "\n\nDetalhes da anotação:");
                    embedBuilder.addField("Autor:", author, false);
                    embedBuilder.addField("Anotado:", target, false);
                    embedBuilder.addField("Descrição:", reason, false);
                    embedBuilder.addField("Data:", new SimpleDateFormat("dd/MM/yyyy - HH:mm").format(new Date(date)), false);
                })).queue();
                deleteAnnotation(date);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void deleteAnnotation(long date) {
        try (PreparedStatement stmt = getConnection().prepareStatement("DELETE FROM `annotation` WHERE `date`=?")) {
            stmt.setLong(1, date);
            stmt.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private Connection getConnection() {
        return connection.getConnection();
    }
}
