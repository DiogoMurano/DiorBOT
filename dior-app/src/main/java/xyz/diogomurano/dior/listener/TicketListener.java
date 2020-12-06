package xyz.diogomurano.dior.listener;

import com.jagrosh.jdautilities.menu.ButtonMenu;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import xyz.diogomurano.dior.BotManager;
import xyz.diogomurano.dior.api.DiscordAPI;
import xyz.diogomurano.dior.api.HabboAPI;
import xyz.diogomurano.dior.api.models.HabboUser;
import xyz.diogomurano.dior.collaborator.Collaborator;
import xyz.diogomurano.dior.collaborator.CollaboratorImpl;
import xyz.diogomurano.dior.database.dao.AnnotationDao;
import xyz.diogomurano.dior.database.dao.CollaboratorDao;
import xyz.diogomurano.dior.database.dao.PromotionDao;
import xyz.diogomurano.dior.database.dto.AnnotationDto;
import xyz.diogomurano.dior.database.dto.PromotionDto;
import xyz.diogomurano.dior.process.Metadata;
import xyz.diogomurano.dior.ticket.*;

import javax.annotation.Nonnull;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TicketListener extends ListenerAdapter {

    private final JDA jda;
    private final TicketService ticketService;
    private final TicketHolder ticketHolder;
    private final CollaboratorDao collaboratorDao;
    private final AnnotationDao annotationDao;
    private final PromotionDao promotionDao;

    private final ButtonMenu.Builder noteBuilder;

    private static final ExecutorService POOL = Executors.newFixedThreadPool(4);

    public TicketListener(BotManager botManager) {
        this.jda = botManager.getJda();
        this.ticketService = botManager.getTicketService();
        this.ticketHolder = botManager.getTicketHolder();
        this.collaboratorDao = botManager.getCollaboratorDao();
        this.annotationDao = botManager.getAnnotationDao();
        this.promotionDao = botManager.getPromotionDao();

        noteBuilder = new ButtonMenu.Builder().setEventWaiter(botManager.getEventWaiter())
                .addChoice("1️⃣").addChoice("2️⃣").addChoice("3️⃣").addChoice("4️⃣").addChoice("5️⃣");
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        super.onGuildMessageReceived(event);

        final Color color = event.getGuild().getSelfMember().getColor();
        final Member member = event.getMember();
        final TextChannel channel = event.getChannel();
        final Guild guild = channel.getGuild();

        String command = event.getMessage().getContentDisplay();
        if (channel.getId().equals("713963418268205096") && command.equals("-criar")) {
            final Collaborator collaborator = getCollaborator(member.getId());
            if (collaborator != null) {
                ticketHolder.createTicket(new TicketImpl(member.getId(), collaborator, TicketType.OTHERS), member);
                channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Ticket criado");
                    embedBuilder.setDescription("O canal foi criado com sucesso.");
                    embedBuilder.setColor(color);
                })).queue();
            }
            return;
        }
        final Ticket ticket = ticketService.findByChannel(channel.getId());
        if (ticket != null) {
            if (member.getRoles().contains(jda.getRoleById("709271796884570144"))) {
                if (command.startsWith("-finalizar")) {
                    String reason = command.replace("-finalizar", "");
                    if (reason.length() < 5) {
                        channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> embedBuilder
                                .addField("Erro:", "O motivo do fechamento deve possuir mais que 5 caracteres.",
                                        false))).queue();
                        return;
                    }
                    ticketHolder.archiveTicket(ticket, member, reason);
                    return;
                } else if (command.startsWith("-adicionar")) {
                    final List<Member> members = event.getMessage().getMentionedMembers();
                    final List<Role> roles = event.getMessage().getMentionedRoles();
                    if (members.isEmpty()) {
                        if (!roles.isEmpty()) {
                            final Role role = roles.get(0);
                            if (channel.getPermissionOverride(role) == null) {
                                channel.createPermissionOverride(role).setAllow(Permission.VIEW_CHANNEL).queue();
                            } else {
                                channel.upsertPermissionOverride(role).setAllow(Permission.VIEW_CHANNEL).queue();
                            }
                            channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                                embedBuilder.setTitle("Adicionado com sucesso");
                                embedBuilder.setDescription("O cargo " + role.getAsMention() + " foi adicionado a este ticket.");
                                embedBuilder.setColor(color);
                            })).queue();
                            return;
                        }
                        channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                            embedBuilder.setTitle("Erro");
                            embedBuilder.setDescription("Você deve mencionar o membro ou o cargo a ser adicionado.");
                            embedBuilder.setColor(color);
                        })).queue();
                        return;
                    }
                    final Member m = members.get(0);
                    if (channel.getPermissionOverride(m) == null) {
                        channel.createPermissionOverride(m).setAllow(Permission.VIEW_CHANNEL).queue();
                    } else {
                        channel.upsertPermissionOverride(m).setAllow(Permission.VIEW_CHANNEL).queue();
                    }
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Adicionado com sucesso");
                        embedBuilder.setDescription("O membro " + m.getAsMention() + " foi adicionado a este ticket.");
                        embedBuilder.setColor(color);
                    })).queue();
                    return;
                } else if (command.startsWith("-remover")) {
                    final List<Member> members = event.getMessage().getMentionedMembers();
                    final List<Role> roles = event.getMessage().getMentionedRoles();
                    if (members.isEmpty()) {
                        if (!roles.isEmpty()) {
                            final Role role = roles.get(0);
                            if (channel.getPermissionOverride(role) == null) {
                                channel.createPermissionOverride(role).setDeny(Permission.VIEW_CHANNEL).queue();
                            } else {
                                channel.upsertPermissionOverride(role).setDeny(Permission.VIEW_CHANNEL).queue();
                            }
                            channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                                embedBuilder.setTitle("Removido com sucesso");
                                embedBuilder.setDescription("O cargo " + role.getAsMention() + " foi removido deste ticket.");
                                embedBuilder.setColor(color);
                            })).queue();
                            return;
                        }
                        channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                            embedBuilder.setTitle("Erro");
                            embedBuilder.setDescription("Você deve mencionar o membro a ser removido.");
                            embedBuilder.setColor(color);
                        })).queue();
                        return;
                    }
                    final Member m = members.get(0);
                    channel.upsertPermissionOverride(m).setDeny(Permission.VIEW_CHANNEL).queue();
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Removido com sucesso");
                        embedBuilder.setDescription("O membro " + m.getAsMention() + " foi removido deste ticket.");
                        embedBuilder.setColor(color);
                    })).queue();
                    return;
                } else if (command.startsWith("-renomear")) {
                    String reason = command.replace("-renomear", "");
                    if (reason.length() < 5) {
                        channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> embedBuilder
                                .addField("Erro:", "O nome do canal deve possuir mais que 5 caracteres.",
                                        false))).queue();
                        return;
                    }
                    channel.getManager().setName(reason.replace(" ", "-")).delay(1, TimeUnit.SECONDS).queue(aVoid -> {
                        channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                            embedBuilder.setTitle("Renomeado com sucesso");
                            embedBuilder.setDescription("O ticket teve seu nome definido para " + channel.getName() + " com sucesso.");
                            embedBuilder.setColor(color);
                        })).queue();
                    });
                    return;
                } else if (command.startsWith("-tópico")) {
                    String reason = command.replace("-tópico", "");
                    if (reason.length() < 5) {
                        channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> embedBuilder
                                .addField("Erro:", "O tópico do canal deve possuir mais que 5 caracteres.",
                                        false))).queue();
                        return;
                    }
                    channel.getManager().setTopic(reason).delay(1, TimeUnit.SECONDS).queue(aVoid -> {
                        channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                            embedBuilder.setTitle("Topico atualizado com sucesso");
                            embedBuilder.setDescription("O ticket teve seu tópico definido para " + channel.getTopic() + " com sucesso.");
                            embedBuilder.setColor(color);
                        })).queue();
                    });
                    return;
                } else if (command.startsWith("-webhook")) {
                    String reason = command.replace("-webhook", "");
                    if (reason.length() < 5) {
                        channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> embedBuilder
                                .addField("Erro:", "O webhook do canal deve possuir mais que 5 caracteres.",
                                        false))).queue();
                        return;
                    }
                    channel.createWebhook(reason).delay(1, TimeUnit.SECONDS).queue(webhook ->
                            channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                                embedBuilder.setTitle("Criado com sucesso!");
                                embedBuilder.setDescription("O webhook para esse canal foi gerado com sucesso.");
                                embedBuilder.setColor(color);
                                embedBuilder.addField("Link:", webhook.getUrl(), false);
                            })).queue());
                    return;
                } else if (command.startsWith("-encerrar")) {
                    final TicketType type = ticket.getType();
                    if (type == TicketType.SUGGESTION || type == TicketType.RECLAMATION || type == TicketType.DOUBT) {
                        noteBuilder.setDescription(guild.getMemberById(ticket.getAuthorId()).getAsMention()
                                + " de 1 a 5 qual nota você aplica ao atendimento de " + member.getAsMention());
                        noteBuilder.setColor(color);
                        channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                            embedBuilder.setColor(DiscordAPI.getMessageColor());
                            embedBuilder.setDescription(guild.getEmoteById("712822459165835294").getAsMention() + " Carregando...");
                        })).queue(m -> {
                            
                            noteBuilder.setAction(reactionEmote -> {
                                final String emote = reactionEmote.getAsCodepoints();
                                int note;
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
                                final StringBuilder performanceBuilder = new StringBuilder();
                                performanceBuilder.append("· ");
                                for (int x = 0; x < note; x++) {
                                    performanceBuilder.append(":star:");
                                }

                                jda.getTextChannelById("718985732261281883").sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                                    final String habboName = ticket.getCollaborator().getHabboName();
                                    final HabboUser user = HabboAPI.getUser(habboName);
                                    embedBuilder.setTitle("Ouvidoria - " + habboName);
                                    embedBuilder.setColor(color);
                                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                                    embedBuilder.setDescription("O ticket foi encerrado.");
                                    embedBuilder.addField("Tipo:", type.getName(), false);
                                    embedBuilder.addField("Atendido por:", member.getNickname(), false);
                                    embedBuilder.addField("Avaliação:", performanceBuilder.toString(), false);
                                })).queue();
                                ticketHolder.archiveTicket(ticket, member, "Ticket finalizado com sucesso.");
                                m.delete().queue();
                            });
                            noteBuilder.build().display(m);
                        });
                    }
                }
            }
            if (ticket.getAuthorId().equals(event.getAuthor().getId()) && !ticket.isWaitingFinish() && ticket
                    .getStatus() == TicketStatus.WAITING_DATA) {
                try {
                    POOL.execute(() -> ticket.getProcess().findCurrent().validateMessage(event.getMessage()));
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        super.onGuildMessageReactionAdd(event);

        final TextChannel channel = event.getChannel();
        final Ticket ticket = ticketService.findByChannel(channel.getId());
        if (ticket != null) {
            if (ticket.getAuthorId().equals(event.getMember().getId()) && ticket.getStatus() == TicketStatus.WAITING_DATA && !ticket.isWaitingFinish()) {
                channel.deleteMessageById(event.getMessageId()).queue();
                POOL.execute(() -> ticket.getProcess().findCurrent().validateReaction(event.getReaction()));
            } else if (ticket.getType() == TicketType.HIRING && ticket.getStatus() == TicketStatus.WAITING_CONCLUSION) {
                final Member member = event.getMember();
                final List<Role> roles = member.getRoles();
                final Guild guild = event.getGuild();
                if (roles.contains(guild.getRoleById("708816697968033854")) || roles.contains(guild.getRoleById("709602964213465108"))
                        || roles.contains(guild.getRoleById("722654486967091260"))) {
                    POOL.execute(() -> {
                        final String emoji = event.getReactionEmote().getAsCodepoints();
                        if (emoji.equalsIgnoreCase("U+2705")) {
                            final Metadata metadata = ticket.getMetadata();
                            Collaborator collaborator = metadata.get("collaborator");
                            HabboUser user = metadata.get("user");
                            final String lastRole = collaborator == null ? "Nenhum" : collaborator.getRole().getName();
                            if (collaborator == null) {
                                collaborator = new CollaboratorImpl(user.getName());
                            }
                            xyz.diogomurano.dior.collaborator.Role role = metadata.get("role");
                            final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                            final JDA jda = event.getJDA();

                            if(role.isLowLevel()) {
                                jda.getTextChannelById("708761536667320401").sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                                    embedBuilder.setTitle("Contratação de " + user.getName());
                                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                                    embedBuilder.setColor(DiscordAPI.getGuild().getSelfMember().getColor());
                                    embedBuilder.setDescription("Autor: **" + ticket.getCollaborator().getHabboName() + "\n"
                                            + "\n"
                                            + "Novo cargo: **" + role.getName() + "**\n"
                                            + "Último cargo: **" + lastRole + "\n"
                                            + "\n"
                                            + "**Data da última promoção:** " + metadata.get("lastDate") + "\n" +
                                            "**Data desta contratação:** " + dateFormat.format(System.currentTimeMillis()));
                                    embedBuilder.addField("Observações:", metadata.get("observation"), false);
                                    embedBuilder.addField("Autorizado por:", event.getMember().getNickname(), false);
                                })).queue(message -> channel.sendMessage(message.getJumpUrl()).queue());
                            } else {
                                jda.getTextChannelById("726265538736816269").sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                                    embedBuilder.setTitle("Contratação de " + user.getName());
                                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                                    embedBuilder.setColor(DiscordAPI.getGuild().getSelfMember().getColor());
                                    embedBuilder.setDescription("Autor: **" + ticket.getCollaborator().getHabboName() + "\n"
                                            + "\n"
                                            + "Novo cargo: **" + role.getName() + "**\n"
                                            + "Último cargo: **" + lastRole + "\n"
                                            + "\n"
                                            + "**Data da última promoção:** " + metadata.get("lastDate") + "\n" +
                                            "**Data desta contratação:** " + dateFormat.format(System.currentTimeMillis()));
                                    embedBuilder.addField("Observações:", metadata.get("observation"), false);
                                    embedBuilder.addField("Autorizado por:", event.getMember().getNickname(), false);
                                })).queue(message -> channel.sendMessage(message.getJumpUrl()).queue());
                            }

                            promotionDao.register(new PromotionDto(member.getNickname(), user.getName(), role, System.currentTimeMillis()));

                            collaborator.setRole(role);
                            collaboratorDao.createOrUpdate(collaborator);
                            channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                                embedBuilder.setTitle("Contratação concluída com sucesso.");
                                embedBuilder.setDescription("A contratação foi aprovada pelo **" + member.getNickname() + "** e publicada com sucesso!");
                                embedBuilder.addField("Observações:", "O colaborador já pode ser aceito nos respectivos grupos.", false);
                            })).queue();
                            channel.getManager().setName("concluído-" + user.getName()).complete();
                            ticket.setStatus(TicketStatus.WAITING_CONCLUSION);
                            channel.sendMessage(jda.getUserById("560256699118780418").getAsMention()).delay(1, TimeUnit.SECONDS)
                                    .queue(message -> message.delete().queue());


                            final String discordId = collaborator.getDiscordId();
                            if (!discordId.equals(" ")) {
                                final Member findMember = guild.getMemberById(discordId);
                                System.out.println(findMember);
                                if (findMember != null) {
                                    final Set<xyz.diogomurano.dior.collaborator.Role> all = xyz.diogomurano.dior.collaborator.Role.getAllBelow(collaborator.getRole());
                                    all.forEach(r -> {
                                        if (roles.stream().anyMatch(ro -> ro.getId().equals(r.getRoleId()))) {
                                            guild.removeRoleFromMember(findMember, DiscordAPI.findRoleById(r.getRoleId())).queue();
                                            guild.removeRoleFromMember(findMember, DiscordAPI.findRoleById(r.getSector().getRoleId())).queue();
                                        }
                                    });
                                    guild.addRoleToMember(findMember, guild.getRoleById("708533887931777054")).queue();
                                    guild.addRoleToMember(findMember, guild.getRoleById(collaborator.getRole().getRoleId())).queue();
                                    guild.addRoleToMember(findMember, guild.getRoleById(collaborator.getRole().getSector().getRoleId())).queue();
                                }
                            }
                        } else {
                            channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                                embedBuilder.setTitle("Contratação cancelada.");
                                embedBuilder.setDescription("A contratação foi recusada pelo **" + member.getNickname() + "** e o ticket será cancelado!");
                            })).queue();
                            ticketHolder.archiveTicket(ticket, member, "Contratação recusada pelo " + member.getNickname() + ".");

                        }
                    });
                }
            } else {
                if (ticket.getType() == TicketType.DEMOTE && ticket.getStatus() == TicketStatus.WAITING_CONCLUSION) {
                    final Member member = event.getMember();
                    final List<Role> roles = member.getRoles();
                    final Guild guild = event.getGuild();
                    if (roles.contains(guild.getRoleById("708816697968033854")) || roles.contains(guild.getRoleById("722654486967091260"))) {
                        POOL.execute(() -> {
                            final String emoji = event.getReactionEmote().getAsCodepoints();
                            if (emoji.equalsIgnoreCase("U+2705")) {
                                final Metadata metadata = ticket.getMetadata();
                                Collaborator collaborator = metadata.get("collaborator");
                                HabboUser user = metadata.get("user");
                                final JDA jda = event.getJDA();
                                jda.getTextChannelById("710817471468404766").sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                                    embedBuilder.setTitle("Demissão de " + user.getName());
                                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                                    embedBuilder.setColor(guild.getSelfMember().getColor());
                                    embedBuilder.addField("Autor:", ticket.getCollaborator().getHabboName(), false);
                                    embedBuilder.addField("Nickname:", user.getName(), false);
                                    embedBuilder.addField("Cargo atual:", collaborator.getRole().getName(), false);
                                    embedBuilder.addField("Descrição:", metadata.get("description"), false);
                                    embedBuilder.addField("Autorizada por:", event.getMember().getNickname(), false);
                                })).queue(message -> channel.sendMessage(message.getJumpUrl()).queue());
                                collaboratorDao.delete(collaborator);
                                channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                                    embedBuilder.setTitle("Demissão concluída com sucesso.");
                                    embedBuilder.setDescription("A demissão foi aprovada pelo **" + member.getNickname() + "** e publicada com sucesso!");
                                })).queue();
                                ticketHolder.archiveTicket(ticket, event.getMember(), "Demissão realizada com sucesso.");

                                if (!collaborator.getDiscordId().equals(" ")) {
                                    Member m = event.getGuild().getMemberById(collaborator.getDiscordId());
                                    System.out.println(m);
                                    if (m != null) {
                                        System.out.println("teste");
                                        m.getRoles().forEach(role -> event.getGuild().removeRoleFromMember(m, role).queue());

                                        System.out.println(m.getRoles());
                                    }
                                }

                            } else {
                                channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                                    embedBuilder.setTitle("Demissão cancelada.");
                                    embedBuilder.setDescription("A demissão foi recusada pelo **" + member.getNickname() + "** e o ticket será cancelado!");
                                })).queue();
                                ticketHolder.archiveTicket(ticket, member, "Demissão recusada pelo " + member.getNickname() + ".");

                            }
                        });
                    }
                } else if (ticket.getType() == TicketType.ANNOTATION && ticket.getStatus() == TicketStatus.WAITING_CONCLUSION) {
                    final Member member = event.getMember();
                    final List<Role> roles = member.getRoles();
                    final Guild guild = event.getGuild();
                    if (roles.contains(guild.getRoleById("708816697968033854")) || roles.contains(guild.getRoleById("722654486967091260"))) {
                        POOL.execute(() -> {
                            final String emoji = event.getReactionEmote().getAsCodepoints();
                            final Color color = guild.getSelfMember().getColor();
                            if (emoji.equalsIgnoreCase("U+2705")) {
                                final Metadata metadata = ticket.getMetadata();
                                Collaborator collaborator = metadata.get("collaborator");
                                HabboUser user = metadata.get("user");
                                final JDA jda = event.getJDA();

                                int count = annotationDao.countAnnotation(user.getName());
                                boolean warning = count >= 3;

                                jda.getTextChannelById("710817367822827540").sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                                    embedBuilder.setTitle((warning ? "Advertência" : "Anotação") + " de " + user.getName())
                                    ;
                                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                                    embedBuilder.setColor(color);
                                    embedBuilder.addField("Autor:", ticket.getCollaborator().getHabboName(), false);
                                    embedBuilder.addField("Nickname:", user.getName(), false);
                                    embedBuilder.addField("Cargo atual:", collaborator.getRole().getName(), false);
                                    embedBuilder.addField("Descrição:", metadata.get("description"), false);
                                    embedBuilder.addField("Autorizada por:", event.getMember().getNickname(), false);
                                })).queue(message -> channel.sendMessage(message.getJumpUrl()).queue());
                                annotationDao.register(new AnnotationDto(ticket.getCollaborator().getHabboName(), user.getName(), metadata.get("description"), System.currentTimeMillis()));
                                channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                                    embedBuilder.setColor(color);
                                    embedBuilder.setTitle("Anotação concluída com sucesso.");
                                    embedBuilder.setDescription("A anotação foi aprovada pelo **" + member.getNickname() + "** e publicada com sucesso!");
                                })).queue();

                                sendInDm(collaborator, DiscordAPI.createEmbed(embedBuilder -> {
                                    embedBuilder.setColor(color);
                                    embedBuilder.setTitle(warning ? "Você recebeu uma advertência" : "Você recebeu uma anotação");
                                    embedBuilder.setDescription("Caso queira contestar, procure um membro da Presidência ou Fundação");
                                    embedBuilder.addField("Observação:", warning ? "Caso totalize 3 advertências," +
                                            " estará passível de medidas disciplinares." : "Ultrapassando 3 anotações, irá" +
                                            " configurar advertência", false);
                                }), DiscordAPI.createEmbed(embedBuilder -> {
                                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                                    embedBuilder.setTitle(warning ? "Advertência" : "Anotação");
                                    embedBuilder.setColor(color);
                                    embedBuilder.addField("Autor:", ticket.getCollaborator().getHabboName(), false);
                                    embedBuilder.addField("Nickname:", user.getName(), false);
                                    embedBuilder.addField("Cargo atual:", collaborator.getRole().getName(), false);
                                    embedBuilder.addField("Descrição:", metadata.get("description"), false);
                                }));

                                ticketHolder.archiveTicket(ticket, event.getMember(), "Anotação realizada com sucesso.");
                            } else {
                                channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                                    embedBuilder.setTitle("Anotação cancelada.");
                                    embedBuilder.setColor(color);
                                    embedBuilder.setDescription("A anotação foi recusada pelo **" + member.getNickname() + "** e o ticket será cancelado!");
                                })).queue();
                                ticketHolder.archiveTicket(ticket, member, "Anotação recusada pelo " + member.getNickname() + ".");

                            }
                        });
                    }
                }
            }
        }
    }

    private void sendInDm(Collaborator collaborator, MessageEmbed... message) {
        if (!collaborator.getDiscordId().equals(" ")) {
            final Member member = DiscordAPI.getGuild().getMemberById(collaborator.getDiscordId());
            if (member != null) {
                member.getUser().openPrivateChannel().queue(privateChannel -> {
                    for (MessageEmbed m : message) {
                        privateChannel.sendMessage(m).queue();
                    }
                });
            }
        }
    }

    private Collaborator getCollaborator(String discordId) {
        return collaboratorDao.findByDiscordId(discordId).orElse(null);
    }

}
