package xyz.diogomurano.dior.ticket;

import java.util.Set;
import java.util.function.Predicate;

public interface TicketService {

    Set<Ticket> findAll();

    Set<Ticket> findAllByFilter(Predicate<Ticket> predicate);

    void add(Ticket ticket);

    void remove(Ticket ticket);

    Ticket findByChannel(String channelId);

}
