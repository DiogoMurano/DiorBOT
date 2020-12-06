package xyz.diogomurano.dior.collaborator;

import net.dv8tion.jda.api.entities.User;
import xyz.diogomurano.dior.api.DiscordAPI;

import java.util.Optional;

public class CollaboratorImpl implements Collaborator {

    private final String habboName;
    private String discordId;

    private final long joinDate;
    private long lastPromoteDate;
    private Role role;

    private int evaluation;
    private int evaluationCount;
    private int interview;
    private int interviewCount;

    public CollaboratorImpl(String habboName, String discordId, long joinDate, long lastPromoteDate, Role role, int evaluation, int evaluationCount, int interview, int interviewCount) {
        this.habboName = habboName;
        this.discordId = discordId;
        this.joinDate = joinDate;
        this.lastPromoteDate = lastPromoteDate;
        this.role = role;
        this.evaluation = evaluation;
        this.evaluationCount = evaluationCount;
        this.interview = interview;
        this.interviewCount = interviewCount;
    }

    public CollaboratorImpl(String habboName) {
        this(habboName, " ", System.currentTimeMillis(), System.currentTimeMillis(), Role.getDefaultRole(),
                0, 0, 0, 0);
    }

    @Override
    public String getHabboName() {
        return habboName;
    }

    @Override
    public String getDiscordId() {
        return discordId;
    }

    @Override
    public boolean isLinked() {
        return !discordId.equals(" ");
    }

    @Override
    public Role getRole() {
        return role;
    }

    @Override
    public long getJoinDate() {
        return joinDate;
    }

    @Override
    public long getLastPromoteDate() {
        return lastPromoteDate;
    }

    @Override
    public int getEvaluation() {
        return evaluation;
    }

    @Override
    public int getEvaluationCount() {
        return evaluationCount;
    }

    @Override
    public int getInterview() {
        return interview;
    }

    @Override
    public int getInterviewCount() {
        return interviewCount;
    }

    @Override
    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    @Override
    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public void setLastPromoteDate(long date) {
        this.lastPromoteDate = date;
    }

    @Override
    public void addEvaluation(int evaluation) {
        this.evaluation += evaluation;
        this.evaluationCount++;
    }

    @Override
    public void addInterview(int interview) {
        this.interview += interview;
        this.interviewCount++;
    }

    @Override
    public Optional<User> getDiscordUser() {
        return Optional.ofNullable(DiscordAPI.findUserById(discordId));
    }
}
