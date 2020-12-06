package xyz.diogomurano.dior.listener;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import xyz.diogomurano.dior.api.DiscordAPI;
import xyz.diogomurano.dior.collaborator.Role;
import xyz.diogomurano.dior.database.dao.CollaboratorDao;
import xyz.diogomurano.dior.BotManager;
import xyz.diogomurano.dior.collaborator.Collaborator;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SyncListener extends ListenerAdapter {

    private final CollaboratorDao collaboratorDao;
    private static final ExecutorService POOL = Executors.newFixedThreadPool(2);

    public SyncListener(BotManager botManager) {
        collaboratorDao = botManager.getCollaboratorDao();
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        super.onGuildMessageReceived(event);

        final TextChannel channel = event.getChannel();
        if (channel.getId().equals("711471232238747709") && !event.getAuthor().isBot()) {
            final String message = event.getMessage().getContentDisplay();
            event.getMessage().delete().queue();
            if (!message.startsWith("-vincular")) {
                channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Erro");
                    embedBuilder.setDescription("O comando informado está incorreto.");
                    embedBuilder.addField("Instruções:", "Uso: -vincular <Nick> <@Discord>.", false);
                    embedBuilder.setColor(Color.RED);
                })).delay(15, TimeUnit.SECONDS).queue(m -> m.delete().queue());
                return;
            }

            String[] args = message.replace("-vincular", "").trim().split(" ");
            if (args.length < 2) {
                channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Erro");
                    embedBuilder.setDescription("O comando informado está incorreto.");
                    embedBuilder.addField("Instruções:", "Uso: -vincular <Nick> <@Discord>.", false);
                    embedBuilder.setColor(Color.RED);
                })).delay(15, TimeUnit.SECONDS).queue(m -> m.delete().queue());
                return;
            }
            final String nick = args[0];
            final List<Member> users = event.getMessage().getMentionedMembers();
            if (users.size() != 1) {
                channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Erro");
                    embedBuilder.setDescription("Mencione o @ para vincular");
                    embedBuilder.addField("Observações:", "O colaborador deve estar em nosso grupo do discord.", false);
                    embedBuilder.addField("Instruções:", "Uso: -vincular <Nick> <@Discord>.", false);
                    embedBuilder.setColor(Color.RED);
                })).delay(15, TimeUnit.SECONDS).queue(m -> m.delete().queue());
                return;
            }

            POOL.execute(() -> {
                final Collaborator collaborator = collaboratorDao.findByHabboName(nick).orElse(null);
                if (collaborator == null) {
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Erro");
                        embedBuilder.setDescription("Não foi possível localizar o colaborador através do nick **"
                                + nick + "**, verifique e tente novamente.");
                        embedBuilder.addField("Instruções:", "Uso: -vincular <Nick> <@Discord>.", false);
                        embedBuilder.setColor(Color.RED);
                    })).delay(15, TimeUnit.SECONDS).queue(m -> m.delete().queue());
                    return;
                }
                if(collaborator.isLinked()) {
                    channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Erro");
                        embedBuilder.setDescription("O colaborador já está vinculado a outra conta do discord.");
                        embedBuilder.setColor(Color.RED);
                    })).delay(15, TimeUnit.SECONDS).queue(m -> m.delete().queue());
                    return;
                }
                final Member member = users.get(0);
                collaborator.setDiscordId(member.getId());
                collaboratorDao.createOrUpdate(collaborator);
                final Guild guild = event.getGuild();
                channel.sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Vinculação de " + collaborator.getHabboName());
                    embedBuilder.setDescription("Autor: **" + event.getMember().getNickname() + "**\n\n" +
                            "Usuário no discord: **" + member.getUser().getName() + "#" + member.getUser().getDiscriminator() + "** (" + member.getId() + ")");
                    embedBuilder.setColor(guild.getSelfMember().getColor());
                })).queue();
                member.modifyNickname("· " + collaborator.getHabboName()).queue();
                final Set<Role> all = Role.getAllBelow(collaborator.getRole());
                final List<net.dv8tion.jda.api.entities.Role> roles = member.getRoles();
                all.forEach(r -> {
                    if (roles.stream().anyMatch(role -> role.getId().equals(r.getRoleId()))) {
                        guild.removeRoleFromMember(member, DiscordAPI.findRoleById(r.getRoleId())).queue();
                        guild.removeRoleFromMember(member, DiscordAPI.findRoleById(r.getSector().getRoleId())).queue();
                    }
                });
                guild.addRoleToMember(member, guild.getRoleById("708533887931777054")).queue();
                guild.addRoleToMember(member, guild.getRoleById(collaborator.getRole().getRoleId())).queue();
                guild.addRoleToMember(member, guild.getRoleById(collaborator.getRole().getSector().getRoleId())).queue();
            });

        }
    }
}
