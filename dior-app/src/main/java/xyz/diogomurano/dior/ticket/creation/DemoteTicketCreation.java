package xyz.diogomurano.dior.ticket.creation;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import xyz.diogomurano.dior.api.DiscordAPI;
import xyz.diogomurano.dior.api.models.HabboUser;
import xyz.diogomurano.dior.process.Metadata;
import xyz.diogomurano.dior.process.Process;
import xyz.diogomurano.dior.process.Step;
import xyz.diogomurano.dior.BotManager;
import xyz.diogomurano.dior.api.HabboAPI;
import xyz.diogomurano.dior.collaborator.Collaborator;
import xyz.diogomurano.dior.database.dao.CollaboratorDao;
import xyz.diogomurano.dior.ticket.Ticket;
import xyz.diogomurano.dior.ticket.TicketHolder;
import xyz.diogomurano.dior.ticket.TicketStatus;
import xyz.diogomurano.dior.ticket.TicketType;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class DemoteTicketCreation implements TicketCreation{

    private JDA jda;
    private TicketHolder ticketHolder;
    private CollaboratorDao collaboratorDao;

    public void create(BotManager bot) {
        jda = bot.getJda();
        ticketHolder = bot.getTicketHolder();
        collaboratorDao = bot.getCollaboratorDao();
    }

    public void setupTicket(Ticket ticket) {
        TextChannel channel = jda.getTextChannelById(ticket.getChannelId());
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
                    embedBuilder.setTitle("Demissão - Etapa #1");
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Qual o nickname do jogador que deseja demitir?**");
                    embedBuilder.addField("Instrução:", "Digite o nickname exatamente como é exibido no jogo.", false);
                });
            }

            @Override
            public void validateMessage(Message message) {
                String nickname = message.getContentDisplay();

                final HabboUser user = HabboAPI.getUser(nickname);
                if (user == null) {
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Erro");
                        embedBuilder.setColor(Color.RED);
                        embedBuilder.setDescription("O nickname informado não foi localizado. Verifique e tente novamente.");
                    })).queue();
                    return;
                }
                final Collaborator collaborator = collaboratorDao.findByHabboName(user.getName()).orElse(null);
                if (collaborator == null) {
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Erro");
                        embedBuilder.setColor(Color.RED);
                        embedBuilder.setDescription("O nickname informado não pertence a uma colaborador da Dior. Verifique e tente novamente.");
                    })).queue();
                    return;
                }
                metadata.store("collaborator", collaborator);
                metadata.store("user", user);
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
                    HabboUser user = metadata.get("user");
                    Collaborator collaborator = metadata.get("collaborator");

                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                    String lastDate = collaborator != null ? dateFormat.format(collaborator.getLastPromoteDate()) : "Inexistente";
                    metadata.store("lastDate", lastDate);

                    embedBuilder.setTitle("Demissão - Etapa #2");
                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Este é o usuário que deseja demitir?**");
                    embedBuilder.addField("Nickname:", user.getName(), false);
                    embedBuilder.addField("Missão:", user.getMotto(), false);
                    embedBuilder.addField("Cargo atual:", collaborator != null ? collaborator.getRole().getName() : "Nenhum", false);
                    embedBuilder.addField("Data da última promoção:", lastDate, false);
                });
            }

            @Override
            public void validateMessage(Message message) {
            }

            @Override
            public void validateReaction(MessageReaction reaction) {
                final String emoji = reaction.getReactionEmote().getAsCodepoints();
                if (emoji.equalsIgnoreCase("U+2705")) {
                    process.setCurrent(process.findById(2));
                    process.sendMessage(channel);
                } else {
                    process.setCurrent(process.findById(0));
                    process.sendMessage(channel);
                    metadata.remove("user");
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

        process.add(new Step() {
            @Override
            public int getId() {
                return 2;
            }

            @Override
            public MessageEmbed getMessage() {
                return DiscordAPI.createEmbed(embedBuilder -> {
                    HabboUser user = metadata.get("user");
                    Collaborator collaborator = metadata.get("collaborator");

                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                    String lastDate = collaborator != null ? dateFormat.format(collaborator.getLastPromoteDate()) : "Inexistente";
                    metadata.store("lastDate", lastDate);

                    embedBuilder.setTitle("Demissão - Etapa #3");
                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Qual a razão da demissão?**");
                    embedBuilder.addField("Instruções:", "Envie a descrição detalhada com o que aconteceu. O" +
                            " bot não reconhece prints, portanto caso queira anexar alguma, faça upload em um site como https://imgur.com/", false);
                });
            }

            @Override
            public void validateMessage(Message message) {
                metadata.store("description", message.getContentDisplay());
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
                    HabboUser user = metadata.get("user");
                    Collaborator collaborator = metadata.get("collaborator");

                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                    String lastDate = collaborator != null ? dateFormat.format(collaborator.getLastPromoteDate()) : "Inexistente";
                    metadata.store("lastDate", lastDate);

                    embedBuilder.setTitle("Demissão - Etapa #4");
                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Confirma o envio desta demissão?**");
                    embedBuilder.addField("Nickname:", user.getName(), false);
                    embedBuilder.addField("Cargo atual:", collaborator.getRole().getName(), false);
                    embedBuilder.addField("Descrição:", metadata.get("description"), false);
                });
            }

            @Override
            public void validateMessage(Message message) {
                metadata.store("description", message.getContentDisplay());
                process.setCurrent(process.findById(3));
                process.sendMessage(channel);
            }

            @Override
            public void validateReaction(MessageReaction reaction) {

                final String emoji = reaction.getReactionEmote().getAsCodepoints();
                if (emoji.equalsIgnoreCase("U+2705")) {
                    Collaborator collaborator = metadata.get("collaborator");
                    HabboUser user = metadata.get("user");
                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    String lastDate = dateFormat.format(collaborator.getLastPromoteDate());
                    metadata.store("lastDate", lastDate);
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Demissão de " + user.getName());
                        embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                        embedBuilder.setColor(color);
                        embedBuilder.addField("Autor:", ticket.getCollaborator().getHabboName(), false);
                        embedBuilder.addField("Nickname:", user.getName(), false);
                        embedBuilder.addField("Cargo atual:", collaborator.getRole().getName(), false);
                        embedBuilder.addField("Descrição:", metadata.get("description"), false);
                    })).queue(message -> {
                        message.pin().queue();
                        message.addReaction("✅").queue();
                        message.addReaction("❌").queue();
                    });

                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Demissão solicitada!");
                        embedBuilder.setDescription("A demissão foi publicada com sucesso. Este ticket foi definido como concluído!");
                        embedBuilder.addField("Status:", "Aguarde algum membro da FUNDAÇÃO " +
                                "confirmar o envio desta demissão.", false);
                        embedBuilder.setColor(guild.getSelfMember().getColor());
                    })).queue(message -> message.pin().queue());

                    ticket.setStatus(TicketStatus.WAITING_CONCLUSION);
                    channel.sendMessage(guild.getRoleById("708816697968033854").getAsMention()).delay(1, TimeUnit.SECONDS)
                            .queue(message -> message.delete().queue());

                } else {
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Demissão cancelada!");
                        embedBuilder.setDescription("A demissão foi cancelada. Caso necessário, abra um novo ticket!");
                        embedBuilder.setColor(guild.getSelfMember().getColor());
                    })).queue();
                    ticketHolder.archiveTicket(ticket, reaction.getGuild().getSelfMember(), "Demissão cancelada por solicitação do usuário!");
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
        return TicketType.DEMOTE;
    }

}
