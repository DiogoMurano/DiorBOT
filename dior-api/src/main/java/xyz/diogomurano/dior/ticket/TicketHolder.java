package xyz.diogomurano.dior.ticket;

import net.dv8tion.jda.api.entities.Member;

public interface TicketHolder {

    void loadTickets();

    void createTicket(Ticket ticket, Member member);

    void archiveTicket(Ticket ticket, Member author, String reason);

    void checkTickets();

}
