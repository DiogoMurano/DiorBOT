package xyz.diogomurano.dior.ticket.creation;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import xyz.diogomurano.dior.api.DiscordAPI;
import xyz.diogomurano.dior.process.Process;
import xyz.diogomurano.dior.process.Step;
import xyz.diogomurano.dior.ticket.Ticket;
import xyz.diogomurano.dior.ticket.TicketStatus;
import xyz.diogomurano.dior.ticket.TicketType;
import xyz.diogomurano.dior.BotManager;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class DoubtCommand implements TicketCreation{

    private JDA jda;

    @Override
    public void create(BotManager bot) {
        jda = bot.getJda();
    }

    @Override
    public void setupTicket(Ticket ticket) {
        final TextChannel channel = jda.getTextChannelById(ticket.getChannelId());

        final Process process = ticket.getProcess();
        final Guild guild = DiscordAPI.getGuild();
        final Color color = guild.getSelfMember().getColor();

        channel.createPermissionOverride(guild.getRoleById("708534328178769971")).setAllow(Permission.VIEW_CHANNEL).queue();
        channel.createPermissionOverride(guild.getRoleById("709602964213465108")).setAllow(Permission.VIEW_CHANNEL).queue();
        channel.createPermissionOverride(guild.getRoleById("708816697968033854")).setAllow(Permission.VIEW_CHANNEL).queue();
        channel.createPermissionOverride(guild.getRoleById("722654486967091260")).setAllow(Permission.VIEW_CHANNEL).queue();
        channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
            embedBuilder.setTitle("Instruções sobre o Ticket");
            embedBuilder.setDescription("Ticket iniciado! Todas as respostas a partir de agora serão registradas.");
            embedBuilder.setColor(color);
        })).queue();

        process.add(new Step() {

            @Override
            public int getId() {
                return 0;
            }

            @Override
            public MessageEmbed getMessage() {
                return DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Dúvida - Etapa #1");
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Qual a sua dúvida?**");
                    embedBuilder.addField("Instrução:", "Seja o mais detalhado possível para que a resposta" +
                            " possa ser mais precisa.", false);
                });
            }

            @Override
            public void validateMessage(Message message) {
                channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setColor(color);
                    embedBuilder.setTitle("Dúvida registrada");
                    embedBuilder.setDescription("Por favor, aguarde. Você será respondido o mais breve possível.");
                })).queue();
                channel.sendMessage(guild.getRoleById("708534328178769971").getAsMention()).delay(2, TimeUnit.SECONDS)
                        .queue(m -> m.delete().queue());
                channel.sendMessage(guild.getRoleById("708534328178769971").getAsMention()).delay(2, TimeUnit.SECONDS)
                        .queue(m -> m.delete().queue());

                channel.sendMessage(guild.getRoleById("709602964213465108").getAsMention()).delay(2, TimeUnit.SECONDS)
                        .queue(m -> m.delete().queue());
                channel.sendMessage(guild.getRoleById("709602964213465108").getAsMention()).delay(2, TimeUnit.SECONDS)
                        .queue(m -> m.delete().queue());

                channel.sendMessage(guild.getRoleById("708816697968033854").getAsMention()).delay(2, TimeUnit.SECONDS)
                        .queue(m -> m.delete().queue());
                channel.sendMessage(guild.getRoleById("708816697968033854").getAsMention()).delay(2, TimeUnit.SECONDS)
                        .queue(m -> m.delete().queue());

                channel.sendMessage(guild.getRoleById("722654486967091260").getAsMention()).delay(2, TimeUnit.SECONDS)
                        .queue(m -> m.delete().queue());
                channel.sendMessage(guild.getRoleById("722654486967091260").getAsMention()).delay(2, TimeUnit.SECONDS)
                        .queue(m -> m.delete().queue());
                ticket.setStatus(TicketStatus.WAITING_CONCLUSION);
            }

            @Override
            public void validateReaction(MessageReaction reaction) {

            }

            @Override
            public boolean isReact() {
                return false;
            }

            @Override
            public boolean isRating() {
                return false;
            }
        });

    }

    @Override
    public TicketType getType() {
        return TicketType.DOUBT;
    }
}
