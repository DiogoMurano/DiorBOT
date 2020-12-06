package xyz.diogomurano.dior.process;

import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Set;

public interface Process {

    Set<Step> getSteps();

    void add(Step step);

    void remove(Step step);

    Step findByName(int id);

    Step findById(int id);

    Step findCurrent();

    void setCurrent(Step step);

    void sendMessage(TextChannel channel);

}
