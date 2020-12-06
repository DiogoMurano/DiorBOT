package xyz.diogomurano.dior;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import xyz.diogomurano.dior.api.DiscordAPI;
import xyz.diogomurano.dior.commands.assuming.AssumingCommand;
import xyz.diogomurano.dior.commands.assuming.AssumingServiceImpl;
import xyz.diogomurano.dior.commands.credit.CreditCommand;
import xyz.diogomurano.dior.commands.note.NoteCommand;
import xyz.diogomurano.dior.database.DatabaseConnection;
import xyz.diogomurano.dior.database.DatabaseCredentials;
import xyz.diogomurano.dior.database.dao.*;
import xyz.diogomurano.dior.database.mysql.MysqlConnection;
import xyz.diogomurano.dior.listener.ReactionListener;
import xyz.diogomurano.dior.listener.SyncListener;
import xyz.diogomurano.dior.listener.TicketListener;
import xyz.diogomurano.dior.ticket.TicketHolderImpl;
import xyz.diogomurano.dior.ticket.TicketServiceImpl;
import xyz.diogomurano.dior.ticket.creation.*;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.TimeZone;

public class Dior {

    public static void main(String[] args) {

        TimeZone tz = TimeZone.getTimeZone("America/Sao_Paulo");
        TimeZone.setDefault(tz);

        try {
            final JDABuilder jdaBuilder = JDABuilder.createDefault("NzA4NzI3NTIwNDYzMTU5Mzg5.Xrbj8A.9UpyzTkFc7tDfaj2Ij55dUh2d7c");
            jdaBuilder.setActivity(Activity.watching("A melhor agência do Habbo Hotel"));
            JDA jda = jdaBuilder.build();

            EventWaiter eventWaiter = new EventWaiter();

            File file = new File("C:" + File.separator + "Users" + File.separator + "brasp" + File.separator + "Desktop", "database.db");
            File dir = new File("C:" + File.separator + "Users" + File.separator + "brasp" + File.separator + "Desktop", "");
            dir.mkdirs();
            if (!file.exists()) {
                file.createNewFile();
            }

            DiscordAPI.create(jda);

            BotManager botManager = new BotManagerImpl().with(bot -> {
                bot.setJda(jda);

                final DatabaseConnection connection = new MysqlConnection(new DatabaseCredentials().with(credentials -> {
                    credentials.setHostname("localhost");
                    credentials.setDatabase("dior");
                    credentials.setUsername("root");
                    credentials.setPassword("13030201Diogo@");
                    credentials.setPort(3306);
                }));
                connection.createTables();

                bot.setConnection(connection);
                bot.setAnnotationDao(new AnnotationDaoImpl(connection));
                bot.setCollaboratorDao(new CollaboratorDaoImpl(connection));
                bot.setEvaluationDao(new EvaluationDaoImpl(connection));
                bot.setPromotionDao(new PromotionDaoImpl(connection));
                bot.setCreditDao(new CreditDaoImpl(connection));
                bot.setPointDao(new PointDaoImpl(connection));
                bot.setTicketCreationService(new TicketCreationServiceImpl(bot));
                bot.setTicketService(new TicketServiceImpl());
                bot.setTicketHolder(new TicketHolderImpl(bot));
                bot.setAssumingService(new AssumingServiceImpl());
                bot.setEventWaiter(eventWaiter);

                final TicketCreationService ticketCreationService = bot.getTicketCreationService();
                ticketCreationService.register(new AnnotationTicketCreation());
                ticketCreationService.register(new DemoteTicketCreation());
                ticketCreationService.register(new DoubtCommand());
                ticketCreationService.register(new EvaluationTicketCreation());
                ticketCreationService.register(new HiringTicketCreation());
                ticketCreationService.register(new InterviewTicketCreation());
                ticketCreationService.register(new ReclamationTicketCreation());
                ticketCreationService.register(new ReportTicketCreation());
                ticketCreationService.register(new SatisfactionInterviewTicketCreation());
                ticketCreationService.register(new SuggestionTicketCreation());
                ticketCreationService.register(new PromotionTicketCreation());

                ticketCreationService.loadAll();
            });

            jda.addEventListener(new ReactionListener(botManager), new SyncListener(botManager), new TicketListener(botManager),
                    eventWaiter, new AssumingCommand(botManager), new CreditCommand(botManager), new NoteCommand(botManager));

        } catch (LoginException | IOException e) {
            e.printStackTrace();
        }
    }

}
