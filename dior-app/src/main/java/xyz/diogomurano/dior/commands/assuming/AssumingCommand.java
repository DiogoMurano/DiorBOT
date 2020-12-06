package xyz.diogomurano.dior.commands.assuming;

import com.jagrosh.jdautilities.menu.ButtonMenu;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import xyz.diogomurano.dior.api.DiscordAPI;
import xyz.diogomurano.dior.api.models.HabboUser;
import xyz.diogomurano.dior.BotManager;
import xyz.diogomurano.dior.api.HabboAPI;
import xyz.diogomurano.dior.collaborator.Collaborator;
import xyz.diogomurano.dior.database.dao.CollaboratorDao;
import xyz.diogomurano.dior.utils.DateUtils;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AssumingCommand extends ListenerAdapter {

    private static final ScheduledExecutorService POOL = Executors.newScheduledThreadPool(4);

    private final JDA jda;
    private final AssumingService assumingService;
    private final CollaboratorDao collaboratorDao;

    private ButtonMenu.Builder assumingRequestBuilder;
    private ButtonMenu.Builder assumingBuilder;
    private ButtonMenu.Builder quitingBuilder;

    public AssumingCommand(BotManager botManager) {
        jda = botManager.getJda();
        assumingService = botManager.getAssumingService();
        collaboratorDao = botManager.getCollaboratorDao();

        POOL.schedule(() -> {
            final Guild guild = botManager.getJda().getGuildById("708524719573303337");
            final Emote c1 = guild.getEmoteById("714678987343003669");
            final Emote c2 = guild.getEmoteById("714678986915053609");
            final Emote aux = guild.getEmoteById("714678987498061875");
            final Emote aux2 = guild.getEmoteById("714678987279826944");
            final Color color = guild.getSelfMember().getColor();
            this.assumingRequestBuilder = new ButtonMenu.Builder().setEventWaiter(botManager.getEventWaiter())
                    .addChoice(c1).addChoice(c2).addChoice(aux).addChoice(aux2)
                    .setDescription("Para assumir, clique na reação correspondente à posição\n\n" +
                            c1.getAsMention() + " para assumir o **C1**\n" +
                            c2.getAsMention() + " para assumir o **C2**\n" +
                            aux.getAsMention() + " para assumir o **Auxílio de SEDE**\n" +
                            aux2.getAsMention() + " para assumir o **Auxílio de HALL 1**").setColor(color);
            this.assumingBuilder = new ButtonMenu.Builder().setColor(color).setEventWaiter(botManager.getEventWaiter())
                    .addChoice("✅").addChoice("❌");
            this.quitingBuilder = new ButtonMenu.Builder().setColor(color).setEventWaiter(botManager.getEventWaiter())
                    .addChoice("✅").addChoice("❌");
        }, 2, TimeUnit.SECONDS);
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        final Guild guild = event.getGuild();
        final MessageChannel channel = event.getChannel();
        if (event.getAuthor().isBot()) {
            return;
        }
        if (channel.getId().equals("710816059602632745")) {
            try {
                event.getMessage().delete().queue();
            } catch (Exception e) {
            }
            final String message = event.getMessage().getContentDisplay();

            final Color color = guild.getSelfMember().getColor();
            if (message.equals("-assumir")) {
                channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setColor(DiscordAPI.getMessageColor());
                    embedBuilder.setDescription(guild.getEmoteById("712822459165835294").getAsMention() + " Carregando...");
                })).queue(m -> {
                    assumingRequestBuilder.setAction(reactionEmote -> {
                        m.delete().queue();
                        final Collaborator collaborator = collaboratorDao.findByDiscordId(event.getMember().getId()).orElse(null);

                        if (collaborator == null) {
                            channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                                embedBuilder.setTitle("Erro");
                                embedBuilder.setColor(Color.RED);
                                embedBuilder.setDescription("Não localizamos seus dados em nosso banco de dados.");
                            })).delay(10, TimeUnit.SECONDS).queue(msg -> msg.delete().queue());
                            return;
                        }
                        AssumingType assumingType;
                        final String id = reactionEmote.getId();
                        switch (id) {
                            case "714678987343003669":
                                assumingType = AssumingType.C1;
                                break;
                            case "714678986915053609":
                                assumingType = AssumingType.C2;
                                break;
                            case "714678987498061875":
                                assumingType = AssumingType.AUX_GENERAL;
                                break;
                            default:
                                assumingType = AssumingType.AUX_HALL_1;
                                break;
                        }
                        showAssumingMessage(event.getMember(), channel, assumingType, collaborator);
                    });
                    POOL.schedule(() -> {
                        m.delete().complete();
                    }, 15, TimeUnit.SECONDS);
                    assumingRequestBuilder.build().display(m);
                });
            } else if (message.equals("-deixar")) {
                channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setColor(DiscordAPI.getMessageColor());
                    embedBuilder.setDescription(guild.getEmoteById("712822459165835294").getAsMention() + " Carregando...");
                })).queue(m -> {
                    final Collaborator collaborator = collaboratorDao.findByDiscordId(event.getMember().getId()).orElse(null);
                    if (collaborator == null) {
                        channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                            embedBuilder.setTitle("Erro");
                            embedBuilder.setColor(Color.RED);
                            embedBuilder.setDescription("Não localizamos seus dados em nosso banco de dados.");
                        })).delay(10, TimeUnit.SECONDS).queue(msg -> msg.delete().queue());
                        return;
                    }
                    final Assuming assuming = assumingService.findByUser(collaborator.getHabboName());
                    if (assuming == null) {
                        channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                            embedBuilder.setTitle("Erro");
                            embedBuilder.setDescription("Você não está assumindo nenhuma posição.");
                            embedBuilder.setColor(color);
                        })).delay(10, TimeUnit.SECONDS).queue();
                        return;
                    }
                    quitingBuilder.setDescription("Você está deixando a posição **" + assuming.getType().getName() + "**.\n\nConfirma que deseja deixá-la?");
                    quitingBuilder.setAction(reactionEmote -> {
                        m.delete().queue();
                        if (reactionEmote.getName().equals("✅")) {
                            quitAssuming(event.getMember(), channel, collaborator, assuming);
                        }
                    });
                    quitingBuilder.build().display(m);
                    POOL.schedule(() -> {
                        if (m != null) {
                            final Message messageById = channel.getHistory().getMessageById(m.getId());
                            if (messageById != null) {
                                messageById.delete().queue();
                            }
                        }
                    }, 15, TimeUnit.SECONDS);
                });
            } else {
                channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Comando inválido.");
                    embedBuilder.setDescription("Este chat é reservado para comandos, tais como:");
                    embedBuilder.addField("Assumir:", "**-assumir** (Digite e clique na reação)", false);
                    embedBuilder.addField("Deixar:", "**-deixar**", false);
                    embedBuilder.setColor(color);
                })).delay(10, TimeUnit.SECONDS).queue(m -> m.delete().queue());
            }
        }
    }

    private void showAssumingMessage(Member author, MessageChannel channel, AssumingType assumingType, Collaborator collaborator) {
        final Assuming assuming = assumingService.get(assumingType);
        if (assuming == null) {
            assumingBuilder.setDescription("Você está prestes a assumir o **" + assumingType.getName() + "**\n\nPosição " +
                    "atualmente **__vaga__**.\nAo confirmar, deixará qualquer outra posição que esteja assumindo.");
        } else {
            assumingBuilder.setDescription("Você está prestes a assumir o **" + assumingType.getName() + "**\n\nPosição " +
                    "ocupada por **__" + assuming.getUser() + "__**.\nAo confirmar, o " + assuming.getUser() + " deixará esta posição.");
        }
        final Color color = jda.getGuildById("708524719573303337").getSelfMember().getColor();
        assumingBuilder.setAction(reactionEmote -> {
            if (reactionEmote.getName().equals("✅")) {
                final String habboName = collaborator.getHabboName();
                if (assuming != null) {
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        long totalTime = System.currentTimeMillis() - assuming.getStartDate();
                        final HabboUser user = HabboAPI.getUser(assuming.getUser());
                        embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                        embedBuilder.setTitle("Deixando posição");
                        embedBuilder.setDescription("Colaborador deixando posição, pois foi assumida pelo **" + habboName + "**");
                        embedBuilder.addField("Colaborador:", assuming.getUser(), false);
                        embedBuilder.addField("Posição:", assumingType.getName(), false);
                        embedBuilder.addField("Tempo total:", DateUtils.formatDifference(totalTime), false);
                        embedBuilder.setColor(color);
                    })).queue();
                }
                final Assuming userAssuming = assumingService.findByUser(habboName);
                if (userAssuming != null && userAssuming.getType() == assumingType) {
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Erro");
                        embedBuilder.setDescription("Você já está assumindo esta posição!");
                        embedBuilder.setColor(color);
                    })).delay(10, TimeUnit.SECONDS).queue(message -> message.delete().queue());
                    return;
                }
                if (userAssuming != null) {
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        final HabboUser user = HabboAPI.getUser(userAssuming.getUser());
                        long totalTime = System.currentTimeMillis() - assuming.getStartDate();
                        embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                        embedBuilder.setTitle("Deixando posição");
                        embedBuilder.setDescription("Colaborador deixando posição, pois está assumindo **" + assumingType.getName() + "**.");
                        embedBuilder.addField("Colaborador:", userAssuming.getUser(), false);
                        embedBuilder.addField("Posição:", assumingType.getName(), false);
                        embedBuilder.addField("Tempo total:", DateUtils.formatDifference(totalTime), false);
                        embedBuilder.setColor(color);
                    })).queue();
                }
                assumingService.put(assumingType, new AssumingImpl(habboName, System.currentTimeMillis(), System.currentTimeMillis(), assumingType));
                channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                    final HabboUser user = HabboAPI.getUser(habboName);
                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    embedBuilder.setTitle("Assumindo posição");
                    embedBuilder.setDescription("Colaborador assumindo posição");
                    embedBuilder.addField("Colaborador:", habboName, false);
                    embedBuilder.addField("Posição:", assumingType.getName(), false);
                    embedBuilder.setColor(color);
                })).queue();
                channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Instruções");
                    embedBuilder.setDescription(author.getAsMention() + " para deixar a posição, digite **'-deixar'**");
                    embedBuilder.setColor(color);
                })).delay(10, TimeUnit.SECONDS).queue(message -> {
                    message.delete().queue();
                });
            }
        });
        assumingBuilder.setFinalAction(message -> message.delete().queue()).setTimeout(15, TimeUnit.SECONDS).build().display(channel);
    }

    private void quitAssuming(Member author, MessageChannel channel, Collaborator collaborator, Assuming assuming) {
        final Color color = jda.getGuildById("708524719573303337").getSelfMember().getColor();
        channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
            final HabboUser user = HabboAPI.getUser(assuming.getUser());
            long totalTime = System.currentTimeMillis() - assuming.getStartDate();
            embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
            embedBuilder.setTitle("Deixando posição");
            embedBuilder.setDescription("Colaborador deixando posição");
            embedBuilder.addField("Colaborador:", assuming.getUser(), false);
            embedBuilder.addField("Posição:", assuming.getType().getName(), false);
            embedBuilder.addField("Tempo total:", DateUtils.formatDifference(totalTime), false);
            embedBuilder.setColor(color);
        })).queue();
        assumingService.remove(assuming.getType());
    }

}
