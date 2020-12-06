package xyz.diogomurano.dior.ticket;

import xyz.diogomurano.dior.collaborator.Role;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import xyz.diogomurano.dior.api.DiscordAPI;
import xyz.diogomurano.dior.ticket.creation.TicketCreation;
import xyz.diogomurano.dior.ticket.creation.TicketCreationService;
import xyz.diogomurano.dior.BotManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class TicketHolderImpl implements TicketHolder {

    private static final String TICKET_CATEGORY = "708537842632097803";
    private static final ExecutorService POOL = Executors.newFixedThreadPool(3);
    private static final ScheduledExecutorService SCHEDULED = Executors.newSingleThreadScheduledExecutor();

    private final JDA jda;
    private final TicketService ticketService;
    private final TicketCreationService ticketCreationService;

    public TicketHolderImpl(BotManager bot) {
        this.jda = bot.getJda();
        this.ticketService = bot.getTicketService();
        this.ticketCreationService = bot.getTicketCreationService();


        loadTickets();
        checkTickets();
    }

    @Override
    public void loadTickets() {
        SCHEDULED.schedule(() -> {
            final List<GuildChannel> channels = jda.getCategoryById("708537842632097803").getChannels();
            for (GuildChannel channel : channels) {
                if (!channel.getId().equals("709242221408419861") && !channel.getId().equals("713963418268205096")) {
                    final TicketImpl ticket = new TicketImpl("", null, TicketType.OTHERS);
                    ticket.setChannelId(channel.getId());
                    ticketService.add(ticket);
                }
            }
        }, 2, TimeUnit.SECONDS);
    }

    @Override
    public void createTicket(Ticket ticket, Member member) {
        POOL.execute(() -> {
            final Category category = jda.getCategoryById(TICKET_CATEGORY);
            if (category == null) {
                System.out.println("[ERROR] Category not found.");
                return;
            }
            final String nickname = member.getNickname();
            if (nickname == null) {
                System.out.println("[ERROR] Nickname is null.");
                return;
            }
            category.createTextChannel(ticket.getType().getName() + "-" + nickname).delay(1, TimeUnit.SECONDS).queue(channel -> {
                ticket.setChannelId(channel.getId());
                channel.createPermissionOverride(member).setAllow(Permission.VIEW_CHANNEL).queue();
                if (ticket.getType() != TicketType.OTHERS && ticket.getType() != TicketType.DOUBT && ticket.getType() !=
                        TicketType.RECLAMATION && ticket.getType() != TicketType.SUGGESTION) {

                    channel.getManager().setTopic("Tempo para cancelamento: **30 minuto(s)**").queue();
                    DiscordAPI.getGuild().getMembersWithRoles(DiscordAPI.findRoleById("709271796884570144"))
                            .forEach(m -> channel.createPermissionOverride(m).setAllow(Permission.VIEW_CHANNEL).queue());
                }

                ping(channel, member);
                ping(channel, member);
                ping(channel, member);

                jda.getTextChannelById("709242221408419861").sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setColor(DiscordAPI.getGuild().getSelfMember().getColor());
                    embedBuilder.setTitle("Ticket aberto");
                    embedBuilder.setDescription("O ticket **" + channel.getName() + "** foi aberto." +
                            "\n" +
                            "\n" +
                            "**Autor:**" + "<@" + ticket.getAuthorId() + ">" +
                            "\n" +
                            "\n" +
                            "**Id:**" +
                            "\n" +
                            channel.getId());
                    embedBuilder.setFooter(new SimpleDateFormat("dd/MM/yyyy - HH:mm").format(new Date()));
                })).queue();

                final TicketCreation ticketCreation = ticketCreationService.findByType(ticket.getType());
                ticketCreation.setupTicket(ticket);
                ticket.getProcess().sendMessage(channel);
            });
        });
        ticketService.add(ticket);
    }

    @Override
    public void archiveTicket(Ticket ticket, Member author, String reason) {
        POOL.execute(() -> {
            if (ticket.isWaitingFinish()) {
                return;
            }
            ticket.setWaitingFinish();
            final TextChannel channel = jda.getTextChannelById(ticket.getChannelId());
            channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                embedBuilder.setTitle("Arquivando...");
                embedBuilder.setDescription("Este ticket será arquivado em 15 segundos...");
                embedBuilder.setColor(DiscordAPI.getGuild().getSelfMember().getColor());
            })).delay(15, TimeUnit.SECONDS).queue(message -> {
                jda.getTextChannelById("709242221408419861").sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setColor(DiscordAPI.getGuild().getSelfMember().getColor());
                    embedBuilder.setTitle("Ticket finalizado");
                    embedBuilder.setDescription("O ticket **" + channel.getName() + "** foi finalizado." +
                            "\n" +
                            "\n" +
                            "**Autor:**" +
                            "\n" +
                            "<@" + ticket.getAuthorId() + ">" +
                            "\n" +
                            "\n" +
                            "**Motivo:**" +
                            "\n" +
                            reason + "" +
                            "\n" +
                            "\n" +
                            "**Id:**" +
                            "\n" +
                            channel.getId());
                    embedBuilder.setFooter(new SimpleDateFormat("dd/MM/yyyy - HH:mm").format(new Date()));
                })).queue();
                channel.delete().queue();
            });
            ticketService.remove(ticket);
        });
    }

    @Override
    public void checkTickets() {
        SCHEDULED.scheduleWithFixedDelay(() -> {
            ticketService.findAllByFilter(ticket -> ticket.getStatus() == TicketStatus.WAITING_DATA && !ticket.getChannelId()
                    .equals("") && !ticket.isWaitingFinish() && ticket.getType() != TicketType.OTHERS && ticket.getType()
                    != TicketType.DOUBT && ticket.getType() != TicketType.SUGGESTION && ticket.getType() != TicketType.RECLAMATION).forEach(ticket -> {
                TextChannel channel = jda.getTextChannelById(ticket.getChannelId());
                if (channel != null) {
                    long finishTime = ticket.getCreatedDate() + (30 * 60 * 1000);
                    if (System.currentTimeMillis() >= finishTime) {
                        channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                            embedBuilder.setTitle("Cancelamento de ticket");
                            embedBuilder.setColor(DiscordAPI.getGuild().getSelfMember().getColor());
                            embedBuilder.setDescription("O ticket excedeu o limite de **30** minutos sem finalização, portanto será cancelado.");
                        })).queue();
                        archiveTicket(ticket, DiscordAPI.getGuild().getSelfMember(), "Excedeu o limite de 30 minutos.");
                    } else {
                        channel.getManager().setTopic("Tempo para cancelamento: **__" + getFormattedTime(finishTime) + "__**").complete();
                    }
                }
            });
        }, 0, 1, TimeUnit.MINUTES);
    }

    private String getFormattedTime(long end) {
        String message = "";
        long now = System.currentTimeMillis();
        long diff = end - now;
        int seconds = (int) (diff / 1000);
        if (seconds >= 60 * 60 * 24) {
            int days = seconds / (60 * 60 * 24);
            seconds = seconds % (60 * 60 * 24);

            message += days + " dia(s) ";
        }
        if (seconds >= 60 * 60) {
            int hours = seconds / (60 * 60);
            seconds = seconds % (60 * 60);

            message += hours + " hora(s) ";
        }
        if (seconds >= 60) {
            int min = seconds / 60;
            seconds = seconds % 60;

            message += min + " minutos(s) ";
        }
        if (seconds >= 0) {
            message += seconds + " segundos(s) ";
        }

        return message;
    }

    private void ping(TextChannel channel, Member target) {
        channel.sendMessage(target.getAsMention()).delay(1, TimeUnit.SECONDS).queue(message -> message.delete().queue());
    }
}
