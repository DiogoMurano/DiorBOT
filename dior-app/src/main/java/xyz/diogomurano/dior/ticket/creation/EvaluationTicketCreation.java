package xyz.diogomurano.dior.ticket.creation;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import xyz.diogomurano.dior.api.DiscordAPI;
import xyz.diogomurano.dior.api.models.HabboUser;
import xyz.diogomurano.dior.process.Metadata;
import xyz.diogomurano.dior.process.Process;
import xyz.diogomurano.dior.process.Step;
import xyz.diogomurano.dior.ticket.Ticket;
import xyz.diogomurano.dior.ticket.TicketType;
import xyz.diogomurano.dior.BotManager;
import xyz.diogomurano.dior.api.HabboAPI;
import xyz.diogomurano.dior.collaborator.Collaborator;
import xyz.diogomurano.dior.database.dao.CollaboratorDao;
import xyz.diogomurano.dior.database.dao.EvaluationDao;
import xyz.diogomurano.dior.database.dto.EvaluationDto;
import xyz.diogomurano.dior.ticket.TicketHolder;

import java.awt.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EvaluationTicketCreation implements TicketCreation{

    private JDA jda;
    private TicketHolder ticketHolder;
    private CollaboratorDao collaboratorDao;
    private EvaluationDao evaluationDao;

    public void create(BotManager bot) {
        jda = bot.getJda();
        ticketHolder = bot.getTicketHolder();
        collaboratorDao = bot.getCollaboratorDao();
        evaluationDao = bot.getEvaluationDao();
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
                    embedBuilder.setTitle("Avaliação - Etapa #1");
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Qual o nickname do jogador que deseja avaliar?**");
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

                    embedBuilder.setTitle("Avaliação - Etapa #2");
                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Este é o usuário que deseja avaliar?**");
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
                    embedBuilder.setTitle("Avaliação - Etapa #3");
                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("O que tem a dizer referente ao **DESEMPENHO EM SEDE** deste colaborador?");
                    embedBuilder.addField("Instruções", "Este critério deve avaliar o comportamento, educação" +
                            " e conhecimento do colaborador enquanto exerce suas funções em sede.", false);
                });
            }

            @Override
            public void validateMessage(Message message) {
                metadata.store("performance", message.getContentDisplay());
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
                    embedBuilder.setTitle("Avaliação - Etapa #4");
                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("Qual nota de 1 a 5 você dá a esse colaborador referente ao seu **DESEMPENHO**?");
                    embedBuilder.addField("Instruções", "Reaja para definir a nota.", false);
                });
            }

            @Override
            public void validateMessage(Message message) {

            }

            @Override
            public void validateReaction(MessageReaction reaction) {
                final String emote = reaction.getReactionEmote().getAsCodepoints();
                int note = 0;
                switch (emote) {
                    case "U+31U+fe0fU+20e3":
                        note = 1;
                        break;
                    case "U+32U+fe0fU+20e3":
                        note = 2;
                        break;
                    case "U+33U+fe0fU+20e3":
                        note = 3;
                        break;
                    case "U+34U+fe0fU+20e3":
                        note = 4;
                        break;
                    default:
                        note = 5;
                        break;
                }
                metadata.store("performanceNote", note);
                process.setCurrent(process.findById(4));
                process.sendMessage(channel);
            }

            @Override
            public boolean isReact() {
                return false;
            }

            @Override
            public boolean isRating() {
                return true;
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
                    HabboUser user = metadata.get("user");
                    embedBuilder.setTitle("Avaliação - Etapa #5");
                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("O que tem a dizer referente à **ORTOGRAFIA** deste colaborador?");
                    embedBuilder.addField("Instruções", "Este critério deve avaliar o uso de acentuações, " +
                            "pontuações, negrito, início de palavras em letra maiúscula e escrita correta das palavras " +
                            "conforme normas gramaticais.", false);
                });
            }

            @Override
            public void validateMessage(Message message) {
                metadata.store("orthography", message.getContentDisplay());
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
                    HabboUser user = metadata.get("user");
                    embedBuilder.setTitle("Avaliação - Etapa #6");
                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("Qual nota de 1 a 5 você dá a esse colaborador referente à sua **ORTOGRAFIA**?");
                    embedBuilder.addField("Instruções", "Reaja para definir a nota.", false);
                });
            }

            @Override
            public void validateMessage(Message message) {

            }

            @Override
            public void validateReaction(MessageReaction reaction) {
                final String emote = reaction.getReactionEmote().getAsCodepoints();
                int note = 0;
                switch (emote) {
                    case "U+31U+fe0fU+20e3":
                        note = 1;
                        break;
                    case "U+32U+fe0fU+20e3":
                        note = 2;
                        break;
                    case "U+33U+fe0fU+20e3":
                        note = 3;
                        break;
                    case "U+34U+fe0fU+20e3":
                        note = 4;
                        break;
                    default:
                        note = 5;
                        break;
                }
                metadata.store("orthographyNote", note);
                process.setCurrent(process.findById(6));
                process.sendMessage(channel);
            }

            @Override
            public boolean isReact() {
                return false;
            }

            @Override
            public boolean isRating() {
                return true;
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
                    HabboUser user = metadata.get("user");
                    Collaborator collaborator = metadata.get("collaborator");

                    String performance = metadata.get("performance");
                    String orthography = metadata.get("orthography");
                    int performanceNote = metadata.get("performanceNote");
                    int orthographyNote = metadata.get("orthographyNote");

                    StringBuilder performanceBuilder = new StringBuilder();
                    performanceBuilder.append("· ");
                    for (int x = 0; x < performanceNote; x++) {
                        performanceBuilder.append(":star:");
                    }
                    StringBuilder orthographyBuilder = new StringBuilder();
                    orthographyBuilder.append("· ");
                    for (int x = 0; x < orthographyNote; x++) {
                        orthographyBuilder.append(":star:");
                    }

                    embedBuilder.setTitle("Avaliação - Etapa #7");
                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Confirma o envio desta avaliação?**");
                    embedBuilder.addField("Nickname:", user.getName(), false);
                    embedBuilder.addField("Cargo:", collaborator.getRole().getName(), false);
                    embedBuilder.addField("Desempenho em sede:", performance, false);
                    embedBuilder.addField("Nota - Desempenho:", performanceBuilder.toString(), false);
                    embedBuilder.addField("Ortografia:", orthography, false);
                    embedBuilder.addField("Nota - Ortografia:", orthographyBuilder.toString(), false);
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
                        embedBuilder.setTitle("Avaliação concluida com sucesso!");
                        embedBuilder.setDescription("O processo de avaliação foi finalizado com sucesso. Este ticket será finalizado.");
                        embedBuilder.setColor(color);
                    })).queue();
                    String performance = metadata.get("performance");
                    String orthography = metadata.get("orthography");
                    int performanceNote = metadata.get("performanceNote");
                    int orthographyNote = metadata.get("orthographyNote");

                    StringBuilder performanceBuilder = new StringBuilder();
                    performanceBuilder.append("· ");
                    for (int x = 0; x < performanceNote; x++) {
                        performanceBuilder.append(":star:");
                    }
                    StringBuilder orthographyBuilder = new StringBuilder();
                    orthographyBuilder.append("· ");
                    for (int x = 0; x < orthographyNote; x++) {
                        orthographyBuilder.append(":star:");
                    }

                    double finalNote = (performanceNote * 0.6) + (orthographyNote * 0.4);
                    DecimalFormat decimalFormat = new DecimalFormat("#.00");

                    HabboUser user = metadata.get("user");
                    Collaborator collaborator = metadata.get("collaborator");
                    final String nickname =ticket.getCollaborator().getHabboName();
                    jda.getTextChannelById("710815362135883867").sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Avaliação de " + user.getName());
                        embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                        embedBuilder.setColor(color);
                        embedBuilder.setDescription("Autor: **" + nickname + "**");
                        embedBuilder.addField("Avaliado:", user.getName(), false);
                        embedBuilder.addField("Cargo:", collaborator.getRole().getName(), false);
                        embedBuilder.addField("Promovido em:", new SimpleDateFormat("dd/MM/yyyy").format(new
                                Date(collaborator.getLastPromoteDate())), false);
                        embedBuilder.addField("Desempenho em sede:", performance, false);
                        embedBuilder.addField("Nota - Desempenho:", performanceBuilder.toString(), false);
                        embedBuilder.addField("Ortografia:", orthography, false);
                        embedBuilder.addField("Nota - Ortografia:", orthographyBuilder.toString(), false);
                        embedBuilder.addField("Nota final:", decimalFormat.format(finalNote), false);
                    })).queue(message -> channel.sendMessage(message.getJumpUrl()).queue());
                    ticketHolder.archiveTicket(ticket, guild.getSelfMember(), "Avaliação finalizada com sucesso.");
                    final User evaluated = jda.getUserById(collaborator.getDiscordId());
                    try {
                        if (!collaborator.getDiscordId().equals(" ")) {
                            evaluated.openPrivateChannel().queue(privateChannel -> {
                                privateChannel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                                    embedBuilder.setTitle("Você recebeu uma avaliação");
                                    embedBuilder.setDescription("Encare de forma construtiva os pontos em que você deve melhorar e continue buscando sempre a excelência.");
                                    embedBuilder.setColor(color);
                                })).queue();

                                privateChannel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                                    embedBuilder.setTitle("Avaliação de " + user.getName());
                                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                                    embedBuilder.setColor(color);
                                    embedBuilder.setDescription("Autor: **" + nickname + "**");
                                    embedBuilder.addField("Avaliado:", user.getName(), false);
                                    embedBuilder.addField("Cargo:", collaborator.getRole().getName(), false);
                                    embedBuilder.addField("Promovido em:", new SimpleDateFormat("dd/MM/yyyy").format(new
                                            Date(collaborator.getLastPromoteDate())), false);
                                    embedBuilder.addField("Desempenho em sede:", performance, false);
                                    embedBuilder.addField("Nota - Desempenho:", performanceBuilder.toString(), false);
                                    embedBuilder.addField("Ortografia:", orthography, false);
                                    embedBuilder.addField("Nota - Ortografia:", orthographyBuilder.toString(), false);
                                    embedBuilder.addField("Nota final:", decimalFormat.format(finalNote), false);
                                })).queue();
                            });
                        }
                    } catch (Exception e) {
                        jda.getTextChannelById("709242221408419861").sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                            embedBuilder.setTitle("Falha ao enviar DM");
                            embedBuilder.setDescription("O bot fracassou ao enviar uma mensagem privada ao autor de avaliação " + evaluated.getAsMention() + ".");
                            embedBuilder.setColor(Color.RED);
                        })).queue();
                    }

                    final User author = jda.getUserById(ticket.getAuthorId());
                    try {
                        author.openPrivateChannel().queue(privateChannel -> {

                            privateChannel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                                embedBuilder.setTitle("Avaliação de " + user.getName());
                                embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                                embedBuilder.setColor(color);
                                embedBuilder.setDescription("Autor: **" + nickname + "**");
                                embedBuilder.addField("Avaliado:", user.getName(), false);
                                embedBuilder.addField("Cargo:", collaborator.getRole().getName(), false);
                                embedBuilder.addField("Promovido em:", new SimpleDateFormat("dd/MM/yyyy").format(new
                                        Date(collaborator.getLastPromoteDate())), false);
                                embedBuilder.addField("Desempenho em sede:", performance, false);
                                embedBuilder.addField("Nota - Desempenho:", performanceBuilder.toString(), false);
                                embedBuilder.addField("Ortografia:", orthography, false);
                                embedBuilder.addField("Nota - Ortografia:", orthographyBuilder.toString(), false);
                                embedBuilder.addField("Nota final:", decimalFormat.format(finalNote), false);
                            })).complete();
                        });
                    } catch (Exception e) {
                        jda.getTextChannelById("709242221408419861").sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                            embedBuilder.setTitle("Falha ao enviar DM");
                            embedBuilder.setDescription("O bot fracassou ao enviar uma mensagem privada ao autor de avaliação " + author.getAsMention() + ".");
                            embedBuilder.setColor(Color.RED);
                        })).queue();
                    }

                    evaluationDao.register(new EvaluationDto(nickname, user.getName(), finalNote, System.currentTimeMillis()));
                } else {
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Avaliação cancelada!");
                        embedBuilder.setDescription("A avaliação foi cancelada. Caso necessário, abra um novo ticket!");
                        embedBuilder.setColor(color);
                    })).queue();
                    ticketHolder.archiveTicket(ticket, guild.getSelfMember(), "Avaliação cancelada por solicitação do usuário!");
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
        return TicketType.EVALUATION;
    }
}
