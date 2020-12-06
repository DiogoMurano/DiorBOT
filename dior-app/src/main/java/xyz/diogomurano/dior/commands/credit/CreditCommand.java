package xyz.diogomurano.dior.commands.credit;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import xyz.diogomurano.dior.api.DiscordAPI;
import xyz.diogomurano.dior.api.models.HabboUser;
import xyz.diogomurano.dior.collaborator.Credit;
import xyz.diogomurano.dior.BotManager;
import xyz.diogomurano.dior.api.HabboAPI;
import xyz.diogomurano.dior.collaborator.Collaborator;
import xyz.diogomurano.dior.database.dao.CollaboratorDao;
import xyz.diogomurano.dior.database.dao.CreditDao;

import javax.annotation.Nonnull;

public class CreditCommand extends ListenerAdapter {

    private final JDA jda;
    private final CollaboratorDao collaboratorDao;
    private final CreditDao creditDao;

    public CreditCommand(BotManager botManager) {
        jda = botManager.getJda();
        collaboratorDao = botManager.getCollaboratorDao();
        creditDao = botManager.getCreditDao();
    }

    @Override
    public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {
        if (event.getMessage().getContentDisplay().equalsIgnoreCase("-saldo")) {
            final Collaborator collaborator = collaboratorDao.findByDiscordId(event.getAuthor().getId()).orElse(null);
            if (collaborator == null) {
                event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Erro");
                    embedBuilder.setDescription("Não localizamos dados a seu respeito.");
                    embedBuilder.setColor(DiscordAPI.getMessageColor());
                })).queue();
                return;
            }

            final HabboUser user = HabboAPI.getUser(collaborator.getHabboName());
            final Credit credit = creditDao.findByName(collaborator.getHabboName());
            if (credit == null) {
                event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Crédito de " + collaborator.getHabboName());
                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    embedBuilder.setDescription("Saldo disponível: **" + 0 + "**");
                    embedBuilder.addField("Instruções:", "O saldo é disponibilizado de acordo com suas " +
                            "metas, semanalmente. Consulte-as em <#708899412830715916>.", false);
                    embedBuilder.setColor(DiscordAPI.getMessageColor());
                })).queue();
                return;
            }

            event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                embedBuilder.setTitle("Crédito de " + collaborator.getHabboName());
                embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                embedBuilder.setDescription("Saldo disponível: **" + credit.getCoins() + "c**");
                embedBuilder.addField("Instruções:", "O saldo é disponibilizado de acordo com suas " +
                        "metas, semanalmente. Consulte-as em <#708899412830715916>.", false);
                embedBuilder.setColor(DiscordAPI.getMessageColor());
            })).queue();
        }
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (event.getChannel().getId().equals("741155023739682938")) {
            final String message = event.getMessage().getContentDisplay();
            if (message.startsWith("-creditar")) {
                String[] args = message.split(" ");

                if (args.length != 3) {
                    event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Erro");
                        embedBuilder.setDescription("Uso: -creditar <nick> <valor>.");
                        embedBuilder.setColor(DiscordAPI.getMessageColor());
                    })).queue();
                    return;
                }

                String nickName = args[1];

                try {

                    int value = Integer.parseInt(args[2]);

                    final HabboUser user = HabboAPI.getUser(nickName);
                    if (user == null) {
                        event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                            embedBuilder.setTitle("Erro");
                            embedBuilder.setDescription("Usuário não encontrado.");
                            embedBuilder.setColor(DiscordAPI.getMessageColor());
                        })).queue();
                        return;
                    }

                    Credit credit = creditDao.findByName(user.getName());
                    if (credit == null) {
                        credit = Credit.builder().habboName(user.getName()).coins(0).build();
                    }
                    credit.setCoins(credit.getCoins() + value);

                    Credit finalCredit = credit;
                    event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Atualização de " + user.getName());
                        embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                        embedBuilder.setDescription("O saldo do colaborador foi atualizado!");
                        embedBuilder.addField("Novo saldo:", finalCredit.getCoins() + "", false);
                        embedBuilder.setColor(DiscordAPI.getMessageColor());
                    })).queue();
                    creditDao.register(credit);

                } catch (NumberFormatException e) {
                    event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Erro");
                        embedBuilder.setDescription("O valor deve ser um número.");
                        embedBuilder.setColor(DiscordAPI.getMessageColor());
                    })).queue();
                }

            }
        }

        if (event.getChannel().getId().equals("741155023739682938")) {
            final String message = event.getMessage().getContentDisplay();
            if (message.startsWith("-sacar")) {
                String[] args = message.split(" ");

                if (args.length != 3) {
                    event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Erro");
                        embedBuilder.setDescription("Uso: -creditar <nick> <valor>.");
                        embedBuilder.setColor(DiscordAPI.getMessageColor());
                    })).queue();
                    return;
                }

                String nickName = args[1];

                try {

                    int value = Integer.parseInt(args[2]);

                    final HabboUser user = HabboAPI.getUser(nickName);
                    if (user == null) {
                        event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                            embedBuilder.setTitle("Erro");
                            embedBuilder.setDescription("Usuário não encontrado.");
                            embedBuilder.setColor(DiscordAPI.getMessageColor());
                        })).queue();
                        return;
                    }

                    final Credit credit = creditDao.findByName(user.getName());
                    credit.setCoins(credit.getCoins() - value);

                    event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Atualização de " + user.getName());
                        embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                        embedBuilder.setDescription("O saldo do colaborador foi atualizado!");
                        embedBuilder.addField("Novo saldo:", credit.getCoins() + "", false);
                        embedBuilder.setColor(DiscordAPI.getMessageColor());
                    })).queue();
                    creditDao.register(credit);

                } catch (NumberFormatException e) {
                    event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Erro");
                        embedBuilder.setDescription("O valor deve ser um número.");
                        embedBuilder.setColor(DiscordAPI.getMessageColor());
                    })).queue();
                }

            }
        }

        if (event.getChannel().getId().equals("741155023739682938")) {
            final String message = event.getMessage().getContentDisplay();
            if (message.startsWith("-saldo")) {
                String[] args = message.split(" ");

                if (args.length != 2) {
                    event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Erro");
                        embedBuilder.setDescription("Uso: -saldo <nick>.");
                        embedBuilder.setColor(DiscordAPI.getMessageColor());
                    })).queue();
                    return;
                }

                String nickName = args[1];
                final HabboUser user = HabboAPI.getUser(nickName);
                if (user == null) {
                    event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                        embedBuilder.setTitle("Erro");
                        embedBuilder.setDescription("Usuário não encontrado.");
                        embedBuilder.setColor(DiscordAPI.getMessageColor());
                    })).queue();
                    return;
                }


                final Credit credit = creditDao.findByName(user.getName());

                event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                    embedBuilder.setTitle("Saldo de " + user.getName());
                    embedBuilder.setThumbnail("https://www.habbo.com/habbo-imaging/avatarimage?figure=" + user.getFigureString());
                    embedBuilder.setDescription("O saldo do colaborador é de: **" + credit.getCoins() + "c**");
                    embedBuilder.setColor(DiscordAPI.getMessageColor());
                })).queue();

            }
        }
    }
}
