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

public class BotManagerImpl implements BotManager {

    private TicketService ticketService;
    private TicketHolder ticketHolder;
    private DatabaseConnection connection;
    private AnnotationDao annotationDao;
    private CollaboratorDao collaboratorDao;
    private EvaluationDao evaluationDao;
    private PromotionDao promotionDao;
    private CreditDao creditDao;
    private PointDao pointDao;
    private AssumingService assumingService;
    private TicketCreationService ticketCreationService;
    private EventWaiter eventWaiter;
    private JDA jda;

    public BotManagerImpl() {
    }

    @Override
    public TicketService getTicketService() {
        return ticketService;
    }

    @Override
    public TicketHolder getTicketHolder() {
        return ticketHolder;
    }

    @Override
    public DatabaseConnection getConnection() {
        return connection;
    }

    @Override
    public AnnotationDao getAnnotationDao() {
        return annotationDao;
    }

    @Override
    public CollaboratorDao getCollaboratorDao() {
        return collaboratorDao;
    }

    @Override
    public EvaluationDao getEvaluationDao() {
        return evaluationDao;
    }


    @Override
    public PromotionDao getPromotionDao() {
        return promotionDao;
    }

    @Override
    public CreditDao getCreditDao() {
        return creditDao;
    }

    @Override
    public PointDao getPointDao() {
        return pointDao;
    }

    @Override
    public AssumingService getAssumingService() {
        return assumingService;
    }

    @Override
    public TicketCreationService getTicketCreationService() {
        return ticketCreationService;
    }

    @Override
    public JDA getJda() {
        return jda;
    }

    @Override
    public EventWaiter getEventWaiter() {
        return eventWaiter;
    }

    @Override
    public void setTicketService(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Override
    public void setTicketHolder(TicketHolder ticketHolder) {
        this.ticketHolder = ticketHolder;
    }

    @Override
    public void setConnection(DatabaseConnection connection) {
        this.connection = connection;
    }

    @Override
    public void setAnnotationDao(AnnotationDao annotationDao) {
        this.annotationDao = annotationDao;
    }

    @Override
    public void setCollaboratorDao(CollaboratorDao collaboratorDao) {
        this.collaboratorDao = collaboratorDao;
    }

    @Override
    public void setEvaluationDao(EvaluationDao evaluationDao) {
        this.evaluationDao = evaluationDao;
    }

    @Override
    public void setPromotionDao(PromotionDao promotionDao) {
        this.promotionDao = promotionDao;
    }

    @Override
    public void setCreditDao(CreditDao creditDao) {
        this.creditDao = creditDao;
    }

    @Override
    public void setPointDao(PointDao pointDao) {
        this.pointDao = pointDao;
    }

    @Override
    public void setAssumingService(AssumingService assumingService) {
        this.assumingService = assumingService;
    }

    @Override
    public void setTicketCreationService(TicketCreationService ticketCreationService) {
        this.ticketCreationService = ticketCreationService;
    }

    @Override
    public void setJda(JDA jda) {
        this.jda = jda;
    }

    @Override
    public void setEventWaiter(EventWaiter eventWaiter) {
        this.eventWaiter = eventWaiter;
    }

    @Override
    public BotManager with(Consumer<BotManager> consumer) {
        consumer.accept(this);
        return this;
    }
}
