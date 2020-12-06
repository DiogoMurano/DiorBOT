package xyz.diogomurano.dior.ticket.creation;

import xyz.diogomurano.dior.collaborator.Role;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import xyz.diogomurano.dior.api.DiscordAPI;
import xyz.diogomurano.dior.api.models.HabboUser;
import xyz.diogomurano.dior.process.Metadata;
import xyz.diogomurano.dior.process.Process;
import xyz.diogomurano.dior.process.Step;
import xyz.diogomurano.dior.ticket.TicketType;
import xyz.diogomurano.dior.BotManager;
import xyz.diogomurano.dior.api.HabboAPI;
import xyz.diogomurano.dior.collaborator.Collaborator;
import xyz.diogomurano.dior.database.dao.CollaboratorDao;
import xyz.diogomurano.dior.ticket.Ticket;
import xyz.diogomurano.dior.ticket.TicketHolder;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class SatisfactionInterviewTicketCreation implements TicketCreation {

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
                    embedBuilder.setTitle("Entrevista - Etapa #1");
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Qual o nickname do jogador que deseja entrevistar?**");
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
                        embedBuilder.setDescription("O nickname informado não pertence a um colaborador da Dior. Verifique e tente novamente.");
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

                    embedBuilder.setTitle("Entrevista - Etapa #2");
                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Este é o usuário que deseja entrevistar?**");
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
                    embedBuilder.setTitle("Entrevista - Etapa #3");
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Qual seu nivel de frequência na Dior? **");
                    embedBuilder.addField("Instrução:", "Copie a mensagem e envie ao colaborador. Registre a resposta aqui.", false);
                });
            }

            @Override
            public void validateMessage(Message message) {
                metadata.store("step1", message.getContentDisplay());
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
                    embedBuilder.setTitle("Entrevista - Etapa #4");
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Na sua opinião, no que a Dior deve melhorar?**");
                    embedBuilder.addField("Instrução:", "Copie a mensagem e envie ao colaborador. Registre a resposta aqui.", false);
                });
            }

            @Override
            public void validateMessage(Message message) {
                metadata.store("step2", message.getContentDisplay());
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
                    embedBuilder.setTitle("Entrevista - Etapa #5");
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**O que você mais gosta na  Dior?**");
                    embedBuilder.addField("Instrução:", "Copie a mensagem e envie ao colaborador. Registre a resposta aqui.", false);
                });
            }

            @Override
            public void validateMessage(Message message) {
                metadata.store("step3", message.getContentDisplay());
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
                    embedBuilder.setTitle("Entrevista - Etapa #6");
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**O que você não gosta na Dior?**");
                    embedBuilder.addField("Instrução:", "Copie a mensagem e envie ao colaborador. Registre a resposta aqui.", false);
                });
            }

            @Override
            public void validateMessage(Message message) {
                metadata.store("step4", message.getContentDisplay());
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
                    embedBuilder.setTitle("Entrevista - Etapa #7");
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**De 0 à 10 qual sua nota para à Dior?**");
                    embedBuilder.addField("Instrução:", "Copie a mensagem e envie ao colaborador. Registre a resposta aqui.", false);
                });
            }

            @Override
            public void validateMessage(Message message) {
                metadata.store("step5", message.getContentDisplay());
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
                    embedBuilder.setTitle("Entrevista - Etapa #8");
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Confirma o envio desta entrevista?**");
                    embedBuilder.addField("Qual seu nivel de frequência na Dior?", metadata.get("step1"), false);
                    embedBuilder.addField("Na sua opinião, no que a Dior deve melhorar?", metadata.get("step2"), false);
                    embedBuilder.addField("O que você mais gosta na  Dior?", metadata.get("step3"), false);
                    embedBuilder.addField("O que você não gosta na Dior?", metadata.get("step4"), false);
                    embedBuilder.addField("De 0 à 10 qual sua nota para à dior?", metadata.get("step5"), false);
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
                        embedBuilder.setTitle("Entrevista concluida com sucesso!");
                        embedBuilder.setDescription("O processo de entrevista foi finalizado com sucesso. Este ticket será finalizado.");
                        embedBuilder.setColor(color);
                    })).queue();
                    HabboUser user = metadata.get("user");
                    jda.getTextChannelById("724052224434765914").sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Entrevista de " + user.getName());
                        embedBuilder.setDescription("Autor: **" + ticket.getCollaborator().getRole().getName() + " " + ticket.getCollaborator().getHabboName() + "**");
                        embedBuilder.addField("Qual seu nivel de frequência na Dior? ", metadata.get("step1"), false);
                        embedBuilder.addField("Na sua opinião, no que a Dior deve melhorar?", metadata.get("step2"), false);
                        embedBuilder.addField("O que você mais gosta na  Dior?", metadata.get("step3"), false);
                        embedBuilder.addField("O que você não gosta na Dior?", metadata.get("step4"), false);
                        embedBuilder.addField("De 0 à 10 qual sua nota para à dior?", metadata.get("step5"), false);
                        embedBuilder.setColor(color);
                        embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    })).queue(message -> channel.sendMessage(message.getJumpUrl()).queue());
                    ticketHolder.archiveTicket(ticket, guild.getSelfMember(), "Entrevista finalizada com sucesso.");
                } else {
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Entrevista cancelada!");
                        embedBuilder.setDescription("A entrevista foi cancelada. Caso necessário, abra um novo ticket!");
                        embedBuilder.setColor(color);
                    })).queue();
                    ticketHolder.archiveTicket(ticket, guild.getSelfMember(), "Entrevista cancelada por solicitação do usuário!");
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
        return TicketType.SATISFACTION_INTERVIEW;
    }

}
