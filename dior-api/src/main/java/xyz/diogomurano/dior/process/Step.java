package xyz.diogomurano.dior.process;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;

public interface Step {

    int getId();

    MessageEmbed getMessage();

    void validateMessage(Message message);

    void validateReaction(MessageReaction reaction);

    boolean isReact();

    boolean isRating();

}
