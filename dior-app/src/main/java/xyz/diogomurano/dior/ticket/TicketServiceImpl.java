package xyz.diogomurano.dior.ticket;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TicketServiceImpl implements TicketService {

    private final Set<Ticket> tickets;

    public TicketServiceImpl() {
        tickets = new HashSet<>();
    }

    @Override
    public Set<Ticket> findAll() {
        return tickets;
    }

    @Override
    public Set<Ticket> findAllByFilter(Predicate<Ticket> predicate) {
        return tickets.stream().filter(predicate).collect(Collectors.toSet());
    }

    @Override
    public void add(Ticket ticket) {
        this.tickets.add(ticket);
    }

    @Override
    public void remove(Ticket ticket) {
        this.tickets.remove(ticket);
    }

    @Override
    public Ticket findByChannel(String channelId) {
        return tickets.stream().filter(ticket -> ticket.getChannelId().equals(channelId)).findAny().orElse(null);
    }
}
