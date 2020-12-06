package xyz.diogomurano.dior.commands.assuming;

public class AssumingImpl implements Assuming{

    private final String user;
    private final long startDate;
    private final long finishDate;
    private final AssumingType assumingType;

    public AssumingImpl(String user, long startDate, long finishDate, AssumingType assumingType) {
        this.user = user;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.assumingType = assumingType;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public long getStartDate() {
        return startDate;
    }

    @Override
    public long getFinishDate() {
        return finishDate;
    }

    @Override
    public AssumingType getType() {
        return assumingType;
    }
}
