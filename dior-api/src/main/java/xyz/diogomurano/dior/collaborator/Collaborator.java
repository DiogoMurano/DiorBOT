package xyz.diogomurano.dior.collaborator;

import net.dv8tion.jda.api.entities.User;

import java.util.Optional;

public interface Collaborator {

    String getHabboName();

    String getDiscordId();

    boolean isLinked();

    Role getRole();

    long getJoinDate();

    long getLastPromoteDate();

    int getEvaluation();

    int getEvaluationCount();

    int getInterview();

    int getInterviewCount();

    void setDiscordId(String discordId);

    void setRole(Role role);

    void setLastPromoteDate(long date);

    void addEvaluation(int evaluation);

    void addInterview(int interview);

    Optional<User> getDiscordUser();

}
