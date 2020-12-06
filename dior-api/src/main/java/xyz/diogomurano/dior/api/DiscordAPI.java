package xyz.diogomurano.dior.api;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.util.function.Consumer;

public class DiscordAPI {

    private static JDA jda;

    public static void create(JDA jda) {
        DiscordAPI.jda = jda;
    }

    public static MessageEmbed createEmbed(Consumer<EmbedBuilder> consumer) {
        EmbedBuilder builder = new EmbedBuilder();
        consumer.accept(builder);
        return builder.build();
    }

    public static Color getMessageColor() {
        return getGuild().getSelfMember().getColor();
    }

    public static Role findRoleById(String roleId) {
        return getGuild().getRoleById(roleId);
    }

    public static Member findMemberById(String memberId) {
        return getGuild().getMemberById(memberId);
    }

    public static User findUserById(String id) {
        return jda.getUserById(id);
    }

    public static Guild getGuild() {
        return jda.getGuildById("708524719573303337");
    }

}