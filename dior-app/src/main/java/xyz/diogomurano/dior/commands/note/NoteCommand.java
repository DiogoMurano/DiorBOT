package xyz.diogomurano.dior.commands.note;

import com.jagrosh.jdautilities.menu.ButtonMenu;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import xyz.diogomurano.dior.api.DiscordAPI;
import xyz.diogomurano.dior.api.models.HabboUser;
import xyz.diogomurano.dior.BotManager;
import xyz.diogomurano.dior.api.HabboAPI;

import javax.annotation.Nonnull;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NoteCommand extends ListenerAdapter {

    private static final ScheduledExecutorService POOL = Executors.newScheduledThreadPool(2);
    private ButtonMenu.Builder noteBuilder;

    public NoteCommand(BotManager botManager) {
        POOL.schedule(() -> {
            final Guild guild = botManager.getJda().getGuildById("708524719573303337");
            final Color color = guild.getSelfMember().getColor();
            this.noteBuilder = new ButtonMenu.Builder().setEventWaiter(botManager.getEventWaiter())
                    .addChoice("\uD83D\uDFE9").addChoice("\uD83D\uDFE5").setColor(color);
        }, 2, TimeUnit.SECONDS);
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        final JDA jda = event.getJDA();
        final Guild guild = event.getGuild();
        final MessageChannel channel = event.getChannel();
        try {
            if (event.getAuthor().isBot() || event.getMember().getNickname() == null) {
                return;
            }
        } catch (Exception e) {
            return;
        }
        if (channel.getId().equals("710815692085002250")) {
            try {
                event.getMessage().delete().queue();
            } catch (Exception e) {
            }
            final String message = event.getMessage().getContentDisplay();
            final Color color = guild.getSelfMember().getColor();

            if (message.startsWith("-nota")) {
                String[] args = message.split(" ");
                if (args.length != 2 || !args[0].equals("-nota")) {
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Argumentos inválidos.");
                        embedBuilder.setDescription("Este chat é reservado para comando, tal como:");
                        embedBuilder.addField("Nota:", "**-nota** Nome do Modelo", false);
                        embedBuilder.setColor(color);
                    })).delay(10, TimeUnit.SECONDS).queue(m -> m.delete().queue());
                    return;
                }

                String model = args[1];
                final HabboUser user = HabboAPI.getUser(model);
                if (user == null) {
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Erro");
                        embedBuilder.setDescription("O nickname especificado não foi encontrado, verifique e tente novamente.");
                        embedBuilder.setColor(color);
                    })).delay(10, TimeUnit.SECONDS).queue(m -> m.delete().queue());
                    return;
                }

                noteBuilder.setColor(color);
                noteBuilder.setDescription("Qual nota deseja atribuir para **" + user.getName() + "**?" +
                        "\n" +
                        "\n\uD83D\uDFE9 caso seja **APROVADO**" +
                        "\n\n\uD83D\uDFE5 caso seja **REPROVADO**.");

                channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setColor(DiscordAPI.getMessageColor());
                    embedBuilder.setDescription(guild.getEmoteById("712822459165835294").getAsMention() + " Carregando...");
                })).queue(m -> {
                    noteBuilder.setAction(reactionEmote -> {
                        m.delete().queue();
                        Date date = new Date(System.currentTimeMillis());
                        Date expireDate = new Date(System.currentTimeMillis());
                        expireDate.setHours(23);
                        expireDate.setMinutes(59);
                        expireDate.setSeconds(59);
                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
                        String note = "";
                        if (reactionEmote.getName().equals("\uD83D\uDFE9")) {
                            note = "\uD83D\uDFE9 APROVADO";
                        } else {
                            note = "\uD83D\uDFE5 REPROVADO";
                        }
                        String finalNote = note;
                        channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                            embedBuilder.setColor(color);
                            embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                            embedBuilder.setTitle("Nota de " + user.getName());
                            embedBuilder.addField("Autor:", event.getMember().getNickname(), false);
                            embedBuilder.addField("Modelo:", user.getName(), false);
                            embedBuilder.addField("Nota:", "**" + finalNote + "**", false);
                            embedBuilder.setFooter("Postado em: " + dateFormat.format(date) + "\nExpira em: " + dateFormat.format(expireDate));
                        })).queue();
                    });
                    POOL.schedule(() -> {
                        m.delete().complete();
                    }, 15, TimeUnit.SECONDS);
                    noteBuilder.build().display(m);
                });
            } else {
                channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Comando inválido.");
                    embedBuilder.setDescription("Este chat é reservado para comando, tal como:");
                    embedBuilder.addField("Nota:", "**-nota** Nome do Modelo", false);
                    embedBuilder.setColor(color);
                })).delay(10, TimeUnit.SECONDS).queue(m -> m.delete().queue());
            }
        }
    }
}
