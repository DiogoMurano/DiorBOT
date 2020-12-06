package xyz.diogomurano.dior.ticket.creation;

import xyz.diogomurano.dior.collaborator.Role;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import xyz.diogomurano.dior.api.DiscordAPI;
import xyz.diogomurano.dior.api.models.HabboUser;
import xyz.diogomurano.dior.process.Process;
import xyz.diogomurano.dior.BotManager;
import xyz.diogomurano.dior.api.HabboAPI;
import xyz.diogomurano.dior.collaborator.Collaborator;
import xyz.diogomurano.dior.database.dao.CollaboratorDao;
import xyz.diogomurano.dior.collaborator.CollaboratorImpl;
import xyz.diogomurano.dior.process.Metadata;
import xyz.diogomurano.dior.process.Step;
import xyz.diogomurano.dior.ticket.Ticket;
import xyz.diogomurano.dior.ticket.TicketHolder;
import xyz.diogomurano.dior.ticket.TicketStatus;
import xyz.diogomurano.dior.ticket.TicketType;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class HiringTicketCreation implements TicketCreation{

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
                    embedBuilder.setTitle("Contratação - Etapa #1");
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Qual o nickname do jogador no qual deseja contratar?**");
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

                    embedBuilder.setTitle("Contratação - Etapa #2");
                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Este é o usuário no qual deseja contratar?**");
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

                    Role role = collaborator == null ? Role.TRAINEE : (Role.findById(collaborator.getRole().getId() + 1).orElse(Role.OWNER));
                    metadata.store("role", role);

                    embedBuilder.setTitle("Contratação - Etapa #3");
                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Para qual cargo deseja contratá-lo?**");
                    embedBuilder.addField("Instruções:", "Escreva o cargo exatamente como é exibido em <#708899412830715916>.", false);
                });
            }

            @Override
            public void validateMessage(Message message) {
                String roleName = message.getContentDisplay();
                Role role = Role.findByName(roleName).orElse(null);

                if (role == null) {
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Erro");
                        embedBuilder.setDescription("O cargo **" + roleName + "** não foi localizado. Verifique e tente novamente.");
                        embedBuilder.setColor(color);
                    })).queue();
                    process.setCurrent(process.findById(2));
                    process.sendMessage(channel);
                    return;
                }

                if (role == Role.TRAINEE) {
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Erro");
                        embedBuilder.setDescription("O processo para contratação de **ESTAGIÁRIO** deve ser **PROMOÇÃO**." +
                                " Esse ticket será cancelado. É solicitado que abra um novo ticket para essa ação.");
                        embedBuilder.setColor(color);
                    })).queue();
                    ticketHolder.archiveTicket(ticket, guild.getSelfMember(), "Ticket " +
                            "cancelado, pois o processo adequado para essa ação seria PROMOÇÃO.");
                    return;
                }

                Collaborator collaborator = metadata.get("collaborator");
                Role actualRole = collaborator == null ? Role.TRAINEE : collaborator.getRole();
                if (actualRole.getId() >= role.getId()) {
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Erro");
                        embedBuilder.setDescription("O colaborador já possui o cargo **" + actualRole.getName() + "**," +
                                " portanto essa ação não é possivel. O ticket será cancelado.");
                        embedBuilder.setColor(color);
                    })).queue();
                    ticketHolder.archiveTicket(ticket, message.getGuild().getSelfMember(), "Ticket " +
                            "cancelado, pois o cargo selecionado é invalido para esta contratação.");
                    return;
                }

                if(ticket.getCollaborator().getRole().isUnder(role.getPromoter())) {
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Cancelamento de Ticket");
                        embedBuilder.setDescription("Você não possui permissão para promover este cargo. O ticket será cancelado.");
                        embedBuilder.setColor(Color.RED);
                    })).queue();
                    ticketHolder.archiveTicket(ticket, message.getGuild().getSelfMember(), "Ticket " +
                            "cancelado, pois o autor não possui permissão para promover este colaborador.");
                    return;
                }

                metadata.store("role", role);
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
                    embedBuilder.setTitle("Contratação - Etapa #4");
                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Há mais alguma observação a ser adicionada?**");
                    embedBuilder.addField("Instrução:", "Caso não haja, digite \"Nenhuma\".", false);
                });
            }

            @Override
            public void validateMessage(Message message) {
                String content = message.getContentDisplay();
                metadata.store("observation", content);
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
                    HabboUser user = metadata.get("user");
                    Collaborator collaborator = metadata.get("collaborator");
                    Role role = metadata.get("role");
                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                    embedBuilder.setTitle("Contratação - Etapa #5");
                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Confirma o envio desta contratação?**\nSerá solicitado permissão para os membros da **PRESIDÊNCIA/FUNDAÇÃO**.");
                    embedBuilder.addField("Nickname:", user.getName(), false);
                    embedBuilder.addField("Cargo atual:", collaborator != null ? collaborator.getRole().getName() : "Nenhum", false);
                    embedBuilder.addField("Data da última promoção:", collaborator != null ? dateFormat.format(collaborator
                            .getLastPromoteDate()) : "Inexistente", false);
                    embedBuilder.addField("Novo cargo:", "**" + role.getName() + "**", false);
                    embedBuilder.addField("Observações:", "**" + metadata.get("observation") + "**", false);
                });
            }

            @Override
            public void validateMessage(Message message) {

            }

            @Override
            public void validateReaction(MessageReaction reaction) {
                final String emoji = reaction.getReactionEmote().getAsCodepoints();
                if (emoji.equalsIgnoreCase("U+2705")) {
                    Collaborator collaborator = metadata.get("collaborator");
                    HabboUser user = metadata.get("user");
                    final String lastRole = collaborator == null ? "Nenhum" : collaborator.getRole().getName();
                    if (collaborator == null) {
                        collaborator = new CollaboratorImpl(user.getName());
                    }
                    xyz.diogomurano.dior.collaborator.Role role = metadata.get("role");

                    final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Contratação de " + user.getName());
                        embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                        embedBuilder.setColor(color);
                        embedBuilder.setDescription("Autor: **" +ticket.getCollaborator().getHabboName() + "\n"
                                + "\n"
                                + "Novo cargo: **" + role.getName() + "**\n"
                                + "Último cargo: **" + lastRole + "\n"
                                + "\n"
                                + "**Data da última contratação:** " + metadata.get("lastDate") + "\n" +
                                "**Data desta contratação:** " + dateFormat.format(System.currentTimeMillis()));
                        embedBuilder.addField("Observações:", metadata.get("observation"), false);
                    })).queue(message -> {
                        message.pin().queue();
                        message.addReaction("✅").queue();
                        message.addReaction("❌").queue();
                    });

                    Collaborator finalCollaborator = collaborator;
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Contratação bem sucedida!");
                        embedBuilder.setDescription("A contratação foi publicada com sucesso. Este ticket foi definido como concluído!");
                        if (finalCollaborator.getDiscordId().equals(" ")) {
                            embedBuilder.addField("Instruções:", "Este colaborador ainda não está vinculado" +
                                    " em nosso discord. Envie o grupo para ele e faça a vinculação através do comando" +
                                    " '**-vincular <Nick> <@Discord>**' no canal <#711471232238747709>.", false);
                            embedBuilder.addField("Link do grupo:", "https://discord.gg/Y2MUxmn", false);
                        }
                        embedBuilder.addField("Status:", "Aguarde algum membro da PRESIDÊNCIA/FUNDAÇÃO " +
                                "confirmar o envio desta contratação.", false);
                        embedBuilder.setColor(color);
                    })).queue(message -> message.pin().queue());

                    ticket.setStatus(TicketStatus.WAITING_CONCLUSION);
                    channel.sendMessage(guild.getRoleById("709602964213465108").getAsMention()).delay(1, TimeUnit.SECONDS)
                            .queue(message -> message.delete().queue());
                    channel.sendMessage(guild.getRoleById("708816697968033854").getAsMention()).delay(1, TimeUnit.SECONDS)
                            .queue(message -> message.delete().queue());

                } else {
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Contratação cancelada!");
                        embedBuilder.setDescription("A contratação foi cancelada. Caso necessário, abra um novo ticket!");
                        embedBuilder.setColor(color);
                    })).queue();
                    ticketHolder.archiveTicket(ticket, guild.getSelfMember(), "Contratação cancelada por solicitação do usuário!");
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
        return TicketType.HIRING;
    }


}
