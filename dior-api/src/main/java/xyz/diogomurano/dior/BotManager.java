package xyz.diogomurano.dior;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.JDA;
import xyz.diogomurano.dior.commands.assuming.AssumingService;
import xyz.diogomurano.dior.database.DatabaseConnection;
import xyz.diogomurano.dior.database.dao.*;
import xyz.diogomurano.dior.ticket.TicketHolder;
import xyz.diogomurano.dior.ticket.TicketService;
import xyz.diogomurano.dior.ticket.creation.TicketCreationService;

import java.util.function.Consumer;

public interface BotManager {

    TicketService getTicketService();

    TicketHolder getTicketHolder();

    DatabaseConnection getConnection();

    AnnotationDao getAnnotationDao();

    CollaboratorDao getCollaboratorDao();

    EvaluationDao getEvaluationDao();

    PromotionDao getPromotionDao();

    CreditDao getCreditDao();

    PointDao getPointDao();

    AssumingService getAssumingService();

    TicketCreationService getTicketCreationService();

    JDA getJda();

    EventWaiter getEventWaiter();

    void setTicketService(TicketService ticketService);

    void setTicketHolder(TicketHolder ticketHolder);

    void setConnection(DatabaseConnection connection);

    void setAnnotationDao(AnnotationDao annotationDao);

    void setCollaboratorDao(CollaboratorDao collaboratorDao);

    void setEvaluationDao(EvaluationDao evaluationDao);

    void setPromotionDao(PromotionDao promotionDao);

    void setCreditDao(CreditDao creditDao);

    void setPointDao(PointDao pointDao);

    void setAssumingService(AssumingService assumingService);

    void setTicketCreationService(TicketCreationService ticketCreationService);

    void setJda(JDA jda);

    void setEventWaiter(EventWaiter eventWaiter);

    BotManager with(Consumer<BotManager> consumer);

}
