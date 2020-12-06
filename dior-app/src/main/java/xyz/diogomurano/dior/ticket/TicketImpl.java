package xyz.diogomurano.dior.ticket;

import xyz.diogomurano.dior.process.Metadata;
import xyz.diogomurano.dior.process.Process;
import xyz.diogomurano.dior.process.ProcessImpl;
import xyz.diogomurano.dior.collaborator.Collaborator;

public class TicketImpl implements Ticket {

    private final String authorId;
    private final Collaborator collaborator;
    private final TicketType type;
    private TicketStatus status;
    private final Process process;
    private final Metadata metadata;
    private String channelId;
    private final long createdDate;
    private boolean waitingFinish;

    public TicketImpl(String authorId, Collaborator collaborator, TicketType type) {
        this.authorId = authorId;
        this.collaborator = collaborator;
        this.type = type;
        this.status = TicketStatus.WAITING_DATA;
        this.process = new ProcessImpl();
        this.metadata = new Metadata();
        this.channelId = "";
        this.createdDate = System.currentTimeMillis();
        this.waitingFinish = false;
    }

    @Override
    public String getAuthorId() {
        return authorId;
    }

    @Override
    public Collaborator getCollaborator() {
        return collaborator;
    }

    @Override
    public TicketType getType() {
        return type;
    }

    @Override
    public TicketStatus getStatus() {
        return status;
    }

    @Override
    public Process getProcess() {
        return process;
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public String getChannelId() {
        return channelId;
    }

    @Override
    public long getCreatedDate() {
        return createdDate;
    }

    @Override
    public boolean isWaitingFinish() {
        return waitingFinish;
    }

    @Override
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    @Override
    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    @Override
    public void setWaitingFinish() {
        this.waitingFinish = true;
    }
}
