package xyz.diogomurano.dior.ticket.creation;

import xyz.diogomurano.dior.collaborator.Role;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import xyz.diogomurano.dior.api.DiscordAPI;
import xyz.diogomurano.dior.api.models.HabboUser;
import xyz.diogomurano.dior.process.Metadata;
import xyz.diogomurano.dior.process.Process;
import xyz.diogomurano.dior.process.Step;
import xyz.diogomurano.dior.ticket.Ticket;
import xyz.diogomurano.dior.ticket.TicketHolder;
import xyz.diogomurano.dior.ticket.TicketType;
import xyz.diogomurano.dior.BotManager;
import xyz.diogomurano.dior.api.HabboAPI;

import java.awt.*;

public class ReportTicketCreation implements TicketCreation {

    private JDA jda;
    private TicketHolder ticketHolder;

    @Override
    public void create(BotManager bot) {
        jda = bot.getJda();
        ticketHolder = bot.getTicketHolder();
    }

    @Override
    public void setupTicket(Ticket ticket) {
        final TextChannel channel = jda.getTextChannelById(ticket.getChannelId());

        final Process process = ticket.getProcess();
        final Guild guild = DiscordAPI.getGuild();
        final Color color = guild.getSelfMember().getColor();
        if (channel != null) {
            channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                embedBuilder.setTitle("Instruções sobre o Ticket");
                embedBuilder.setDescription("Ticket iniciado! Todas as respostas a partir de agora serão registradas e " +
                        "o prazo para finalização é de até 30 minutos. Caso o tempo seja excedido, o ticket será " +
                        "automaticamente cancelado.\n\nAcompanhe o tempo na **parte superior** deste canal.");
                embedBuilder.setColor(color);
            })).queue();
        }
        final Metadata metadata = ticket.getMetadata();
        process.add(new Step() {
            @Override
            public int getId() {
                return 0;
            }

            @Override
            public MessageEmbed getMessage() {
                return DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Relatório Presencial - Etapa #1");
                    embedBuilder.setDescription("**Esse relatório é referente a qual horário?**");
                    embedBuilder.addField("Instruções", "Favor adotar a formatação **HH:mm**, exemplo: **18:30**", false);
                    embedBuilder.setColor(color);
                });
            }

            @Override
            public void validateMessage(Message message) {
                metadata.store("hour", message.getContentDisplay());
                process.setCurrent(process.findById(1));
                process.sendMessage(channel);
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

        process.add(new Step() {
            @Override
            public int getId() {
                return 1;
            }

            @Override
            public MessageEmbed getMessage() {
                return DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Relatório Presencial - Etapa #2");
                    embedBuilder.setDescription("**Quem está assumindo o HALL 1?**");
                    embedBuilder.addField("Instruções:", "Digite o nick do jogador exatamente como é exibido no jogo.", false);
                    embedBuilder.setColor(color);
                });
            }

            @Override
            public void validateMessage(Message message) {
                metadata.store("hall1", message.getContentDisplay());
                process.setCurrent(process.findById(2));
                process.sendMessage(channel);
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

        process.add(new Step() {
            @Override
            public int getId() {
                return 2;
            }

            @Override
            public MessageEmbed getMessage() {
                return DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Relatório Presencial - Etapa #3");
                    embedBuilder.setDescription("**Quem está assumindo o HALL 2?**");
                    embedBuilder.addField("Instruções:", "Digite os nicks dos jogadores exatamente como são exibidos no jogo.", false);
                    embedBuilder.setColor(color);
                });
            }

            @Override
            public void validateMessage(Message message) {
                metadata.store("hall2", message.getContentDisplay());
                process.setCurrent(process.findById(3));
                process.sendMessage(channel);
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

        process.add(new Step() {
            @Override
            public int getId() {
                return 3;
            }

            @Override
            public MessageEmbed getMessage() {
                return DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Relatório Presencial - Etapa #4");
                    embedBuilder.setDescription("**Quem está assumindo o HALL 3?**");
                    embedBuilder.addField("Instruções:", "Digite os nicks dos jogadores exatamente como são exibidos no jogo.", false);
                    embedBuilder.setColor(color);
                });
            }

            @Override
            public void validateMessage(Message message) {
                metadata.store("hall3", message.getContentDisplay());
                process.setCurrent(process.findById(4));
                process.sendMessage(channel);
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

        process.add(new Step() {
            @Override
            public int getId() {
                return 4;
            }

            @Override
            public MessageEmbed getMessage() {
                return DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Relatório Presencial - Etapa #5");
                    embedBuilder.setDescription("**Quem está assumindo o HALL 4?**");
                    embedBuilder.addField("Instruções:", "Digite os nicks dos jogadores exatamente como são exibidos no jogo.", false);
                    embedBuilder.setColor(color);
                });
            }

            @Override
            public void validateMessage(Message message) {
                metadata.store("hall4", message.getContentDisplay());
                process.setCurrent(process.findById(5));
                process.sendMessage(channel);
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

        process.add(new Step() {
            @Override
            public int getId() {
                return 5;
            }

            @Override
            public MessageEmbed getMessage() {
                return DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Relatório Presencial - Etapa #6");
                    embedBuilder.setDescription("**Quem está assumindo o C1?**");
                    embedBuilder.addField("Instruções:", "Digite o nick do jogador exatamente como é exibido no jogo.", false);
                    embedBuilder.setColor(color);
                });
            }

            @Override
            public void validateMessage(Message message) {
                metadata.store("c1", message.getContentDisplay());
                process.setCurrent(process.findById(6));
                process.sendMessage(channel);
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

        process.add(new Step() {
            @Override
            public int getId() {
                return 6;
            }

            @Override
            public MessageEmbed getMessage() {
                return DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Relatório Presencial - Etapa #7");
                    embedBuilder.setDescription("**Quem está assumindo o C2?**");
                    embedBuilder.addField("Instruções:", "Digite o nick do jogador exatamente como é exibido no jogo.", false);
                    embedBuilder.setColor(color);
                });
            }

            @Override
            public void validateMessage(Message message) {
                metadata.store("c2", message.getContentDisplay());
                process.setCurrent(process.findById(7));
                process.sendMessage(channel);
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

        process.add(new Step() {
            @Override
            public int getId() {
                return 7;
            }

            @Override
            public MessageEmbed getMessage() {
                return DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Relatório Presencial - Etapa #8");
                    embedBuilder.setDescription("**Quem está assumindo o Auxílio de Sede?**");
                    embedBuilder.addField("Instruções:", "Digite o nick do jogador exatamente como é exibido no jogo.", false);
                    embedBuilder.setColor(color);
                });
            }

            @Override
            public void validateMessage(Message message) {
                metadata.store("auxs", message.getContentDisplay());
                process.setCurrent(process.findById(8));
                process.sendMessage(channel);
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

        process.add(new Step() {
            @Override
            public int getId() {
                return 8;
            }

            @Override
            public MessageEmbed getMessage() {
                return DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Relatório Presencial - Etapa #9");
                    embedBuilder.setDescription("**Quem está assumindo o Auxílio de HALL 1?**");
                    embedBuilder.addField("Instruções:", "Digite o nick do jogador exatamente como é exibido no jogo.", false);
                    embedBuilder.setColor(color);
                });
            }

            @Override
            public void validateMessage(Message message) {
                metadata.store("auxh", message.getContentDisplay());
                process.setCurrent(process.findById(10));
                process.sendMessage(channel);
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

        process.add(new Step() {
            @Override
            public int getId() {
                return 10;
            }

            @Override
            public MessageEmbed getMessage() {
                return DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Relatório Presencial - Etapa #11");
                    embedBuilder.setDescription("**Há quantas pessoas no quarto?**");
                    embedBuilder.addField("Instruções:", "Confira a quantidade na lista de quartos.", false);
                    embedBuilder.setColor(color);
                });
            }

            @Override
            public void validateMessage(Message message) {
                metadata.store("count", message.getContentDisplay());
                process.setCurrent(process.findById(11));
                process.sendMessage(channel);
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

        process.add(new Step() {
            @Override
            public int getId() {
                return 11;
            }

            @Override
            public MessageEmbed getMessage() {
                return DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Relatório Presencial - Etapa #12");
                    embedBuilder.setDescription("**Em que posição está nosso quarto dentre os Quartos mais populares?**");
                    embedBuilder.addField("Instruções:", "Caso não esteja presente, digite 'Nenhum'.", false);
                    embedBuilder.setColor(color);
                });
            }

            @Override
            public void validateMessage(Message message) {
                metadata.store("top", message.getContentDisplay());
                process.setCurrent(process.findById(12));
                process.sendMessage(channel);
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

        process.add(new Step() {
            @Override
            public int getId() {
                return 12;
            }

            @Override
            public MessageEmbed getMessage() {
                return DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Relatório - " + metadata.get("hour"));
                    embedBuilder.setDescription("**Confirma o envio deste relatório?**");
                    embedBuilder.addField("Autor:", ticket.getCollaborator().getHabboName(), false);
                    embedBuilder.addField("HALL 1:", metadata.get("hall1"), false);
                    embedBuilder.addField("HALL 2:", metadata.get("hall2"), false);
                    embedBuilder.addField("HALL 3:", metadata.get("hall3"), false);
                    embedBuilder.addField("HALL 4:", metadata.get("hall4"), false);
                    embedBuilder.addField("C1:", metadata.get("c1"), true);
                    embedBuilder.addField("C2:", metadata.get("c2"), true);
                    embedBuilder.addBlankField(true);
                    embedBuilder.addField("Auxílio de Sede:", metadata.get("auxs"), true);
                    embedBuilder.addField("Auxílio de HALL 1:", metadata.get("auxh"), true);
                    embedBuilder.addBlankField(true);
                    embedBuilder.addField("Quantidade:", metadata.get("count"), true);
                    embedBuilder.addField("Posição no TOP:", metadata.get("top"), true);
                    embedBuilder.setColor(color);
                });
            }

            @Override
            public void validateMessage(Message message) {

            }

            @Override
            public void validateReaction(MessageReaction reaction) {
                final String emoji = reaction.getReactionEmote().getAsCodepoints();
                if (emoji.equalsIgnoreCase("U+2705")) {
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Relatório enviado.");
                        embedBuilder.setDescription("O relatório foi publicado com sucesso.");
                        embedBuilder.setColor(color);
                    })).queue();
                    final HabboUser user = HabboAPI.getUser(ticket.getCollaborator().getHabboName());
                    jda.getTextChannelById("710816308303888415").sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                        embedBuilder.setTitle("Relatório - " + metadata.get("hour"));
                        embedBuilder.addField("Autor:", ticket.getCollaborator().getHabboName(), false);
                        embedBuilder.addField("HALL 1:", metadata.get("hall1"), false);
                        embedBuilder.addField("HALL 2:", metadata.get("hall2"), false);
                        embedBuilder.addField("HALL 3:", metadata.get("hall3"), false);
                        embedBuilder.addField("HALL 4:", metadata.get("hall4"), false);
                        embedBuilder.addField("C1:", metadata.get("c1"), true);
                        embedBuilder.addField("C2:", metadata.get("c2"), true);
                        embedBuilder.addBlankField(true);
                        embedBuilder.addField("Auxílio de Sede:", metadata.get("auxs"), true);
                        embedBuilder.addField("Auxílio de HALL 1:", metadata.get("auxh"), true);
                        embedBuilder.addBlankField(true);
                        embedBuilder.addField("Quantidade:", metadata.get("count"), true);
                        embedBuilder.addField("Posição no TOP:", metadata.get("top"), true);
                        embedBuilder.setColor(color);
                    })).queue(message -> channel.sendMessage(message.getJumpUrl()).queue());
                    ticketHolder.archiveTicket(ticket, guild.getSelfMember(), "Relatório finalizado com sucesso.");
                } else {
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Relatório cancelado!");
                        embedBuilder.setDescription("A relatório foi cancelado. Caso necessário, abra um novo ticket!");
                        embedBuilder.setColor(color);
                    })).queue();
                    ticketHolder.archiveTicket(ticket, guild.getSelfMember(), "Relatório cancelado por solicitação do usuário!");
                }
            }

            @Override
            public boolean isReact() {
                return true;
            }

            @Override
            public boolean isRating() {
                return false;
            }
        });
    }

    @Override
    public TicketType getType() {
        return TicketType.REPORT;
    }

}
