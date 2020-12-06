package xyz.diogomurano.dior.ticket.creation;

import xyz.diogomurano.dior.collaborator.CollaboratorImpl;
import xyz.diogomurano.dior.collaborator.Role;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import xyz.diogomurano.dior.BotManager;
import xyz.diogomurano.dior.api.DiscordAPI;
import xyz.diogomurano.dior.api.HabboAPI;
import xyz.diogomurano.dior.api.models.HabboUser;
import xyz.diogomurano.dior.collaborator.Collaborator;
import xyz.diogomurano.dior.database.dao.CollaboratorDao;
import xyz.diogomurano.dior.database.dao.PromotionDao;
import xyz.diogomurano.dior.database.dto.PromotionDto;
import xyz.diogomurano.dior.process.Metadata;
import xyz.diogomurano.dior.process.Process;
import xyz.diogomurano.dior.process.Step;
import xyz.diogomurano.dior.ticket.Ticket;
import xyz.diogomurano.dior.ticket.TicketHolder;
import xyz.diogomurano.dior.ticket.TicketStatus;
import xyz.diogomurano.dior.ticket.TicketType;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PromotionTicketCreation implements TicketCreation {

    private JDA jda;
    private TicketHolder ticketHolder;
    private CollaboratorDao collaboratorDao;
    private PromotionDao promotionDao;

    public void create(BotManager bot) {
        jda = bot.getJda();
        ticketHolder = bot.getTicketHolder();
        collaboratorDao = bot.getCollaboratorDao();
        promotionDao = bot.getPromotionDao();
    }

    public void setupTicket(Ticket ticket) {
        TextChannel channel = jda.getTextChannelById(ticket.getChannelId());
        final Process process = ticket.getProcess();
        final Guild guild = DiscordAPI.getGuild();
        final Color color = guild.getSelfMember().getColor();
        channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
            embedBuilder.setTitle("Instruções sobre o Ticket");
            embedBuilder.setDescription("Ticket iniciado! Todas as respostas a partir de agora serão registradas e " +
                    "o prazo para finalização é de até 30 minutos. Caso o tempo seja excedido, o ticket será " +
                    "automaticamente cancelado.\n\nAcompanhe o tempo na **parte superior** deste canal.");
            embedBuilder.setColor(color);
        })).queue();
        final Metadata metadata = ticket.getMetadata();
        process.add(new Step() {
            @Override
            public int getId() {
                return 0;
            }

            @Override
            public MessageEmbed getMessage() {
                return DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Promoção - Etapa #1");
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Qual o nickname do jogador que deseja promover?**");
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

                    embedBuilder.setTitle("Promoção - Etapa #2");
                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Este é o usuário que deseja promover?**");
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

                    embedBuilder.setTitle("Promoção - Etapa #3");
                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Este é o cargo para qual deseja promover?**");
                    embedBuilder.addField("Cargo:", role.getName(), false);
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
                    Date lastPromoteDate = new Date(collaborator == null ? 0 : collaborator.getLastPromoteDate());
                    lastPromoteDate.setHours(0);
                    lastPromoteDate.setMinutes(0);
                    lastPromoteDate.setSeconds(0);

                    Date actualDate = new Date(System.currentTimeMillis());
                    actualDate.setHours(0);
                    actualDate.setMinutes(0);
                    actualDate.setSeconds(0);

                    Role role = metadata.get("role");
                    if (ticket.getCollaborator().getRole().isUnder(role.getPromoter())) {
                        channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                            embedBuilder.setTitle("Cancelamento de Ticket");
                            embedBuilder.setDescription("Você não possui permissão para promover este cargo. O ticket será cancelado.");
                            embedBuilder.setColor(Color.RED);
                        })).queue();
                        ticketHolder.archiveTicket(ticket, reaction.getGuild().getSelfMember(), "Ticket " +
                                "cancelado, pois o autor não possui permissão para promover este colaborador.");
                        return;
                    }

                    long daysToPromote = role.getDaysToPromote() * 24 * 60 * 60 * 1000;
                    if (actualDate.getTime() - lastPromoteDate.getTime() < daysToPromote) {
                        long timeLeft = actualDate.getTime() - lastPromoteDate.getTime();
                        channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                            embedBuilder.setTitle("Cancelamento de Ticket");
                            embedBuilder.setDescription("Conforme a hierarquia, o colaborador só pode ser promovido com " +
                                    "**" + role.getDaysToPromote() + "** dias de cargo.\n" +
                                    "O colaborador atualmente possui **" + ((int) timeLeft / (24 * 60 * 60 * 1000)) + "** dias de cargo" +
                                    "\n\n Para prosseguir, é necessário iniciar o processo de **__CONTRATAÇÃO__**");
                            embedBuilder.setColor(Color.RED);
                        })).queue();
                        ticketHolder.archiveTicket(ticket, reaction.getGuild().getSelfMember(), "Ticket " +
                                "cancelado, pois o colaborador não está em tempo hábil para promoção.");
                        return;
                    }

                    process.setCurrent(process.findById(3));
                    process.sendMessage(channel);
                } else {
                    Role role = metadata.get("role");
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Cancelamento de Ticket");
                        embedBuilder.setDescription("Conforme a hierarquia, o próximo cargo deste colaborador seria " +
                                "**" + role.getName() + "**.\n\nCaso deseje prosseguir com outro cargo tente o processo " +
                                "de **CONTRATAÇÃO**.\nEste ticket será finalizado.");
                        embedBuilder.setColor(Color.RED);
                    })).queue();
                    ticketHolder.archiveTicket(ticket, reaction.getGuild().getSelfMember(), "Ticket " +
                            "cancelado, pois o autor recusou o cargo da promoção.");
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
                return 3;
            }

            @Override
            public MessageEmbed getMessage() {
                return DiscordAPI.createEmbed(embedBuilder -> {
                    HabboUser user = metadata.get("user");
                    embedBuilder.setTitle("Promoção - Etapa #4");
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

                    embedBuilder.setTitle("Promoção - Etapa #5");
                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    embedBuilder.setColor(color);
                    embedBuilder.setDescription("**Confirma o envio desta promoção?**");
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
                    Role role = metadata.get("role");

                    final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    final String author =ticket.getCollaborator().getHabboName();

                    collaborator.setRole(role);
                    collaboratorDao.createOrUpdate(collaborator);

                    if(role.isLowLevel()) {
                        jda.getTextChannelById("708761536667320401").sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                            embedBuilder.setTitle("Promoção de " + user.getName());
                            embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                            embedBuilder.setColor(color);
                            embedBuilder.setDescription("Autor: **" + author + "\n"
                                    + "\n"
                                    + "Novo cargo: **" + role.getName() + "**\n"
                                    + "Último cargo: **" + lastRole + "\n"
                                    + "\n"
                                    + "**Data da última promoção:** " + metadata.get("lastDate") + "\n" +
                                    "**Data desta promoção:** " + dateFormat.format(System.currentTimeMillis()));
                            embedBuilder.addField("Observações:", metadata.get("observation"), false);
                        })).queue(message -> channel.sendMessage(message.getJumpUrl()).queue());
                    } else {
                        jda.getTextChannelById("726265538736816269").sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                            embedBuilder.setTitle("Promoção de " + user.getName());
                            embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                            embedBuilder.setColor(color);
                            embedBuilder.setDescription("Autor: **" + author + "\n"
                                    + "\n"
                                    + "Novo cargo: **" + role.getName() + "**\n"
                                    + "Último cargo: **" + lastRole + "\n"
                                    + "\n"
                                    + "**Data da última promoção:** " + metadata.get("lastDate") + "\n" +
                                    "**Data desta promoção:** " + dateFormat.format(System.currentTimeMillis()));
                            embedBuilder.addField("Observações:", metadata.get("observation"), false);
                        })).queue(message -> channel.sendMessage(message.getJumpUrl()).queue());
                    }

                    Collaborator finalCollaborator = collaborator;
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Promoção bem sucedida!");
                        embedBuilder.setDescription("A promoção foi publicada com sucesso. Este ticket foi definido como concluído!");
                        if (finalCollaborator.getDiscordId().equals(" ")) {
                            embedBuilder.addField("Instruções:", "Este colaborador ainda não está vinculado" +
                                    " em nosso discord. Envie o grupo para ele e faça a vinculação através do comando" +
                                    " '**-vincular <Nick> <@Discord>**' no canal <#711471232238747709>.", false);
                            embedBuilder.addField("Link do grupo:", "https://discord.gg/Y2MUxmn", false);
                        } else {
                            embedBuilder.addField("Instruções:", "Aguarde o colaborador ser aceito nos grupos" +
                                    " do **HABBO** para que o ticket seja finalizado.", false);
                        }
                        embedBuilder.setColor(color);
                    })).queue(message -> message.pin().queue());

                    promotionDao.register(new PromotionDto(author, user.getName(), role, System.currentTimeMillis()));

                    final String discordId = collaborator.getDiscordId();
                    if (!discordId.equals(" ")) {
                        final Member member = guild.getMemberById(discordId);
                        if (member != null) {
                            final Set<Role> all = Role.getAllBelow(collaborator.getRole());
                            final List<net.dv8tion.jda.api.entities.Role> roles = member.getRoles();
                            all.forEach(r -> {
                                if (roles.stream().anyMatch(ro -> ro.getId().equals(r.getRoleId()))) {
                                    guild.removeRoleFromMember(member, DiscordAPI.findRoleById(r.getRoleId())).queue();
                                    guild.removeRoleFromMember(member, DiscordAPI.findRoleById(r.getSector().getRoleId())).queue();
                                }
                            });
                            guild.addRoleToMember(member, guild.getRoleById("708533887931777054")).queue();
                            guild.addRoleToMember(member, guild.getRoleById(collaborator.getRole().getRoleId())).queue();
                            guild.addRoleToMember(member, guild.getRoleById(collaborator.getRole().getSector().getRoleId())).queue();
                        }
                    }

                    channel.getManager().setName("concluído-" + user.getName()).queue();
                    ticket.setStatus(TicketStatus.WAITING_CONCLUSION);
                    channel.sendMessage(jda.getUserById("560256699118780418").getAsMention()).delay(1, TimeUnit.SECONDS)
                            .queue(message -> message.delete().queue());

                } else {
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Promoção cancelada!");
                        embedBuilder.setDescription("A promoção foi cancelada. Caso necessário, abra um novo ticket!");
                        embedBuilder.setColor(color);
                    })).queue();
                    ticketHolder.archiveTicket(ticket, reaction.getGuild().getSelfMember(), "Promoção cancelada por solicitação do usuário!");
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
        return TicketType.PROMOTION;
    }


}
