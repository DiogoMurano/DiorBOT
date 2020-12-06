package xyz.diogomurano.dior.ticket.creation;

import xyz.diogomurano.dior.ticket.TicketType;
import xyz.diogomurano.dior.BotManager;

import java.util.HashMap;
import java.util.Map;

public class TicketCreationServiceImpl implements TicketCreationService {

    private final BotManager botManager;
    private final Map<TicketType, TicketCreation> map;

    public TicketCreationServiceImpl(BotManager botManager) {
        this.botManager = botManager;
        this.map = new HashMap<>();
    }

    @Override
    public void register(TicketCreation ticketCreation) {
        this.map.put(ticketCreation.getType(), ticketCreation);
    }

    @Override
    public TicketCreation findByType(TicketType type) {
        return map.get(type);
    }

    @Override
    public void loadAll() {
        map.forEach((type, ticketCreation) -> ticketCreation.create(botManager));
    }
}
