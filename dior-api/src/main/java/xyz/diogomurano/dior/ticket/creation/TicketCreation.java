package xyz.diogomurano.dior.ticket.creation;

import xyz.diogomurano.dior.ticket.TicketType;
import xyz.diogomurano.dior.BotManager;
import xyz.diogomurano.dior.ticket.Ticket;

public interface TicketCreation {

    void create(BotManager manager);

    void setupTicket(Ticket ticket);

    TicketType getType();

}
