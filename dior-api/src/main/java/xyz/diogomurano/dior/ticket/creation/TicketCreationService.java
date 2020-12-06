package xyz.diogomurano.dior.ticket.creation;

import xyz.diogomurano.dior.ticket.TicketType;

public interface TicketCreationService {

    void register(TicketCreation ticketCreation);

    TicketCreation findByType(TicketType type);

    void loadAll();

}
