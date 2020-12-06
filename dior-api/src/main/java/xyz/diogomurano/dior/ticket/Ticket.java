package xyz.diogomurano.dior.ticket;

import xyz.diogomurano.dior.process.Metadata;
import xyz.diogomurano.dior.process.Process;
import xyz.diogomurano.dior.collaborator.Collaborator;

public interface Ticket {

    String getAuthorId();

    Collaborator getCollaborator();

    TicketType getType();

    TicketStatus getStatus();

    Process getProcess();

    Metadata getMetadata();

    String getChannelId();

    long getCreatedDate();

    boolean isWaitingFinish();

    void setChannelId(String channelId);

    void setStatus(TicketStatus status);

    void setWaitingFinish();

}
