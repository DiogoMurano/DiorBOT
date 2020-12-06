package xyz.diogomurano.dior.listener;

import xyz.diogomurano.dior.collaborator.Role;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import xyz.diogomurano.dior.api.DiscordAPI;
import xyz.diogomurano.dior.ticket.TicketType;
import xyz.diogomurano.dior.BotManager;
import xyz.diogomurano.dior.collaborator.Collaborator;
import xyz.diogomurano.dior.database.dao.CollaboratorDao;
import xyz.diogomurano.dior.ticket.TicketHolder;
import xyz.diogomurano.dior.ticket.TicketImpl;

import javax.annotation.Nonnull;
import java.io.File;

public class ReactionListener extends ListenerAdapter {

    private final TicketHolder ticketHolder;
    private final CollaboratorDao collaboratorDao;

    public ReactionListener(BotManager botManager) {
        this.ticketHolder = botManager.getTicketHolder();
        this.collaboratorDao = botManager.getCollaboratorDao();
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        super.onGuildMessageReactionAdd(event);

        if (event.getUser().isBot()) {
            return;
        }

        if (event.getChannel().getId().equals("708746848805978163")) {
            final String reaction = event.getReaction().getReactionEmote().getId();
            final Collaborator collaborator = getCollaborator(event.getUser().getId());
            if (collaborator == null) {
                event.getChannel().removeReactionById(event.getMessageId(), event.getReaction().getReactionEmote().getEmote(), event.getUser()).queue();
                return;
            }
            if (reaction.equals("708872093181280276")) {
                ticketHolder.createTicket(new TicketImpl(event.getMember().getId(), collaborator, TicketType.PROMOTION), event.getMember());
            } else {
                ticketHolder.createTicket(new TicketImpl(event.getMember().getId(), collaborator, TicketType.HIRING), event.getMember());
            }
            event.getChannel().removeReactionById(event.getMessageId(), event.getReaction().getReactionEmote().getEmote(), event.getUser()).queue();
        }

        if (event.getChannel().getId().equals("710815337691742210")) {
            final String reaction = event.getReaction().getReactionEmote().getId();
            final Collaborator collaborator = getCollaborator(event.getUser().getId());
            if (collaborator == null) {
                event.getChannel().removeReactionById(event.getMessageId(), event.getReaction().getReactionEmote().getEmote(), event.getUser()).queue();
                return;
            }
            if (reaction.equals("708872093181280276")) {
                ticketHolder.createTicket(new TicketImpl(event.getMember().getId(), collaborator, TicketType.EVALUATION), event.getMember());
            } else {
                ticketHolder.createTicket(new TicketImpl(event.getMember().getId(), collaborator, TicketType.INTERVIEW), event.getMember());
            }
            event.getChannel().removeReactionById(event.getMessageId(), event.getReaction().getReactionEmote().getEmote(), event.getUser()).queue();
        }

        if (event.getChannel().getId().equals("710817991662633001")) {
            final String reaction = event.getReaction().getReactionEmote().getId();
            final Collaborator collaborator = getCollaborator(event.getUser().getId());
            if (collaborator == null) {
                event.getChannel().removeReactionById(event.getMessageId(), event.getReaction().getReactionEmote().getEmote(), event.getUser()).queue();
                return;
            }
            if (reaction.equals("708872093181280276")) {
                ticketHolder.createTicket(new TicketImpl(event.getMember().getId(), collaborator, TicketType.DEMOTE), event.getMember());
            } else if (reaction.equals("718913765449400381")) {
                ticketHolder.createTicket(new TicketImpl(event.getMember().getId(), collaborator, TicketType.SATISFACTION_INTERVIEW), event.getMember());
            } else {
                ticketHolder.createTicket(new TicketImpl(event.getMember().getId(), collaborator, TicketType.ANNOTATION), event.getMember());
            }
            event.getChannel().removeReactionById(event.getMessageId(), event.getReaction().getReactionEmote().getEmote(), event.getUser()).queue();
        }

        if (event.getChannel().getId().equals("710815587248504872")) {
            final String reaction = event.getReaction().getReactionEmote().getId();
            final Collaborator collaborator = getCollaborator(event.getUser().getId());
            if (collaborator == null) {
                event.getChannel().removeReactionById(event.getMessageId(), event.getReaction().getReactionEmote().getEmote(), event.getUser()).queue();
                return;
            }
            if (reaction.equals("708872093181280276")) {
                ticketHolder.createTicket(new TicketImpl(event.getMember().getId(), collaborator, TicketType.DOUBT), event.getMember());
            } else if (reaction.equals("718913765449400381")) {
                ticketHolder.createTicket(new TicketImpl(event.getMember().getId(), collaborator, TicketType.RECLAMATION), event.getMember());
            } else {
                ticketHolder.createTicket(new TicketImpl(event.getMember().getId(), collaborator, TicketType.SUGGESTION), event.getMember());
            }
            event.getChannel().removeReactionById(event.getMessageId(), event.getReaction().getReactionEmote().getEmote(), event.getUser()).queue();
        }

        if (event.getChannel().getId().equals("714591695769174110")) {
            final String reaction = event.getReaction().getReactionEmote().getId();
            final Collaborator collaborator = getCollaborator(event.getUser().getId());
            if (collaborator == null) {
                event.getChannel().removeReactionById(event.getMessageId(), event.getReaction().getReactionEmote().getEmote(), event.getUser()).queue();
                return;
            }
            if (reaction.equals("708872093181280276")) {
                ticketHolder.createTicket(new TicketImpl(event.getMember().getId(), collaborator, TicketType.REPORT), event.getMember());
            }
            event.getChannel().removeReactionById(event.getMessageId(), event.getReaction().getReactionEmote().getEmote(), event.getUser()).queue();
        }
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        super.onGuildMessageReceived(event);

        if (event.getMessage().getContentDisplay().equals("-emojis") && event.getAuthor().getId().equals("560256699118780418")) {
            for (Emote emote : event.getGuild().getEmotes()) {
                event.getChannel().sendMessage(emote.getAsMention() + " - " + emote.getId()).queue();
            }
        }

        if (event.getMessage().getContentDisplay().equals("!cronograma")) {
            event.getChannel().sendFile(new File("C:" + File.separator + "Users" + File.separator + "brasp" + File.separator + "Downloads", "hierarquia.png")).queue();
            event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                embedBuilder.setColor(event.getGuild().getSelfMember().getColor());
                embedBuilder.setTitle("Cronograma - Dior");
                embedBuilder.setDescription("Segue abaixo as informações sobre todo funcionamento da sede");
                embedBuilder.addField("Domingo à Quinta:", "10:00 às 23:00", false);
                embedBuilder.addField("Sexta e Sábado:", "10:00 às 01:00", false);
                embedBuilder.addField("Faculdades:", "**Setor Avaliativo** - Terça-Feira às 19:00\n" +
                        "**Setor Organizacional** - Quarta-Feira às 19:00\n" +
                        "**Setor Promocional** - Quinta-Feira às 19:00\n" +
                        "**Setor Administrativo** - Sexta-Feira às 19:00\n" +
                        "**Diretoria** - Sábado às 19:00", false);
                embedBuilder.addField("Reunião Geral:", "Domingo às 20:00", false);
                embedBuilder.addField("Pagamento:", "Domingo às 21:00", false);
                embedBuilder.setFooter("Dior TopStar Models");
            })).queue();
        }

        if (event.getMessage().getContentDisplay().equals("!novidades")) {
            event.getChannel().sendFile(new File("C:" + File.separator + "Users" + File.separator + "brasp" + File.separator + "Downloads", "novidades.png")).queue();
            event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                embedBuilder.setColor(event.getGuild().getSelfMember().getColor());
                embedBuilder.setTitle("Novidades - Dior");
                embedBuilder.setDescription("Fique por dentro de todas as atualizações e modificações de todo\n sistema");
                embedBuilder.setFooter("Dior TopStar Models");
            })).queue();
        }

        if(event.getMessage().getContentDisplay().equals("!updatezinho")) {
            event.getChannel().sendMessage("**ALTERAÇÃO**\n" +
                    "\n" +
                    "@everyone\n" +
                    "\n" +
                    "Boa tarde a todos!\n" +
                    "Para maior fidelidade com o trabalho exercido em sede, foi removido o campo **OUVIDORIA** dos relatórios presenciais.\n" +
                    "Também adicionamos a @Presidência, @Fundação e @Federação para respostas dos tickets da #ouvidoria.\n" +
                    "\n" +
                    "Atenciosamente, DiorBOT").queue(message -> {
                message.addReaction(event.getGuild().getEmoteById("712822491591999561")).queue();
            });
        }

        if (event.getMessage().getContentDisplay().equals("!voltei")) {
            event.getChannel().sendMessage("**VOLTEI**\n" +
                    "\n" +
                    "@everyone\n" +
                    "\n" +
                    "Boa tarde!! Sentiram minha falta?\n" +
                    "Retorno agora do meu afastamento de 3 dias e já estou disponível para uso :blush:.\n" +
                    "Foram efetuadas algumas atualizações de desempenho e correção de imprecisões muito antigas em meu código\n" +
                    "\n" +
                    "Agora sim é pra ficar <3\n" +
                    "\n" +
                    "Atenciosamente, DiorBOT").queue(message -> {
                message.addReaction(event.getGuild().getEmoteById("708872093181280276")).queue();
            });
        }

        if (event.getMessage().getContentDisplay().equals("!hierarquia")) {
            event.getChannel().sendFile(new File("C:" + File.separator + "Users" + File.separator + "brasp" + File.separator + "Downloads", "hierarquia.png")).queue();
            event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                embedBuilder.setColor(event.getGuild().getSelfMember().getColor());
                embedBuilder.setTitle("Hierarquia - Dior");
                embedBuilder.setDescription("Segue abaixo as informações relacionadas a política hierarquica\n\n" +
                        "**__SETOR INICIAL__**\n\n" +
                        "**Estagiário** - Assume todos os 4 HALLS\nPromovido em *__dois__* dias de cargo\n\n" +
                        "**Líder de Modelos** - Assume todos os 4 HALLS\nPromovido em *__dois__* dias de cargo\n\n" +
                        "**__SETOR AVALIATIVO__**\n\n" +
                        "**Supervisor** - Responsável pela avaliação dos colaboradores\nPromovido em *__quatro__* dias de cargo\n\n" +
                        "**Coordenador** - Responsável pelas entrevistas pós-promocões\nPromovido em *__cinco__* dias de cargo\n\n" +
                        "**__SETOR ORGANIZACIONAL__**\n\n" +
                        "**Analista** - Assume HALLS/C1/C2/Aux de Sede e HALL 1\nPromovido em *__seis__* dias de cargo\n\n" +
                        "**Orientador** - Assume HALLS/C1/C2/Aux de Sede e HALL 1\nPromovido em *__seis__* dias de cargo\n\n" +
                        "**__SETOR PROMOCIONAL__**\n\n" +
                        "**Sub Gerente** - Responsável pelas promoções do Setor Inicial\nPromovido em *__dez__* dias de cargo\n\n" +
                        "**Gerente** - Responsável pelas promoções do Setor Inicial\nPromovido em *__dez__* dias de cargo\n\n" +
                        "**__SETOR ADMINISTRATIVO__**\n\n" +
                        "**Administrador** - Responsável pelas promoções de Estagiário\nà Gerente\nPromovido em *__dez__* dias de cargo\n\n" +
                        "**Aprendiz de Superintendente** - Responsável pelas promoções\n de Estagiário à Administrador\nPromovido em *__dez__* dias de cargo\n\n" +
                        "**Superintendente** - Responsável pelas promoções de Estagiário\nà Administrador\nPromovido em *__dez__* dias de cargo\n\n" +
                        "**__DIRETORIA__**\n\n" +
                        "**Diretor Financeiro** - Responsável pela confecção dos \nrelatórios de pagamento\nPromoção determinada pela presidência/fundação\n\n" +
                        "**Diretor de RH** - Responsável pelo gerenciamento de tickets e\n relatórios\nPromoção determinada pela presidência/fundação\n\n" +
                        "**Diretor Geral** - Responsável pelo funcionamento da Diretoria\nPromoção determinada pela presidência/fundação"
                );
                embedBuilder.setFooter("A partir do Setor Promocional, deve-se assumir\n HALLS, Comandos e Auxílios somente quando necessário.");
            })).queue();
        }

        if (event.getMessage().getContentDisplay().equals("!processopromover")) {
            event.getChannel().sendFile(new File("C:" + File.separator + "Users" + File.separator + "brasp" + File.separator + "Downloads", "image.png")).queue();
            event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                embedBuilder.setColor(event.getGuild().getSelfMember().getColor());
                embedBuilder.setTitle("Criação de Ticket - Dior");
                embedBuilder.setDescription("Para realizar uma promoção/contratação, será criado um ticket\n" +
                        "corresponde a ação desejada. Fique atento ao canal que será\n" +
                        "gerado e as perguntas feitas pelo BOT.\n\n" +
                        "Clique em " + event.getGuild().getEmoteById("708872093181280276").getAsMention() + " para promoções\n\n" +
                        "Clique em " + event.getGuild().getEmoteById("708872158977458228").getAsMention() + " para contratações");
                embedBuilder.setFooter("Dior TopStar Models");
            })).queue(message -> {
                message.addReaction(event.getGuild().getEmoteById("708872093181280276")).queue();
                message.addReaction(event.getGuild().getEmoteById("708872158977458228")).queue();
            });
        }

        if (event.getMessage().getContentDisplay().equals("!processoavaliar")) {
            event.getChannel().sendFile(new File("C:" + File.separator + "Users" + File.separator + "brasp" + File.separator + "Downloads", "image.png")).queue();
            event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                embedBuilder.setColor(event.getGuild().getSelfMember().getColor());
                embedBuilder.setTitle("Criação de Ticket - Dior");
                embedBuilder.setDescription("· Para realizar uma avaliação ou entrevista, será criado um ticket\n" +
                        "corresponde a ação escolhida. Fique atento ao canal que será\n" +
                        "gerado e as perguntas feitas pelo BOT.\n\n" +
                        "Clique em " + event.getGuild().getEmoteById("708872093181280276").getAsMention() + " para avaliações\n\n" +
                        "Clique em " + event.getGuild().getEmoteById("708872158977458228").getAsMention() + " para entrevistas");
                embedBuilder.setFooter("Dior TopStar Models");
            })).queue(message -> {
                message.addReaction(event.getGuild().getEmoteById("708872093181280276")).queue();
                message.addReaction(event.getGuild().getEmoteById("708872158977458228")).queue();
            });
        }

        if (event.getMessage().getContentDisplay().equals("!admprocesso")) {
            event.getChannel().sendFile(new File("C:" + File.separator + "Users" + File.separator + "brasp" + File.separator + "Downloads", "Administração.png")).queue();
            event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                embedBuilder.setColor(event.getGuild().getSelfMember().getColor());
                embedBuilder.setTitle("Criação de Ticket - Dior");
                embedBuilder.setDescription("· Para realizar uma demissão ou anotação, será criado um ticket\n" +
                        "corresponde a ação escolhida. Fique atento ao canal que será\n" +
                        "gerado e as perguntas feitas pelo BOT.\n\n" +
                        "Clique em " + event.getGuild().getEmoteById("708872093181280276").getAsMention() + " para demissão\n\n" +
                        "Clique em " + event.getGuild().getEmoteById("708872158977458228").getAsMention() + " para anotação\n\n" +
                        "Clique em " + event.getGuild().getEmoteById("718913765449400381").getAsMention() + " para entrevista de satisfação");
                embedBuilder.setFooter("Dior TopStar Models");
            })).queue(message -> {
                message.addReaction(event.getGuild().getEmoteById("708872093181280276")).queue();
                message.addReaction(event.getGuild().getEmoteById("708872158977458228")).queue();
                message.addReaction(event.getGuild().getEmoteById("718913765449400381")).queue();
            });
        }

        if (event.getMessage().getContentDisplay().equals("!diretoriaprocesso")) {
            event.getChannel().sendFile(new File("C:" + File.separator + "Users" + File.separator + "brasp" + File.separator + "Downloads", "Diretoria.png")).queue();
            event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                embedBuilder.setColor(event.getGuild().getSelfMember().getColor());
                embedBuilder.setTitle("Criação de Ticket - Dior");
                embedBuilder.setDescription("" +
                        "· Para a confecção de uma entrevista de satisfação, será criado\n" +
                        "um ticket. Fique atento ao canal que será gerado e as perguntas\n" +
                        "feitas pelo BOT.\n\n" +
                        "Clique em " + event.getGuild().getEmoteById("708872093181280276").getAsMention() + " para entrevistas de satisfação");
                embedBuilder.setFooter("Dior TopStar Models");
            })).queue(message -> {
                message.addReaction(event.getGuild().getEmoteById("708872093181280276")).queue();
            });
        }

        if (event.getMessage().getContentDisplay().equals("!processodemitir")) {
            event.getChannel().sendFile(new File("C:" + File.separator + "Users" + File.separator + "brasp" + File.separator + "Downloads", "image.png")).queue();
            event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                embedBuilder.setColor(event.getGuild().getSelfMember().getColor());
                embedBuilder.setTitle("Criação de Ticket - Dior");
                embedBuilder.setDescription("· Para realizar uma demissão ou anotação, será criado um ticket\n" +
                        "corresponde a ação escolhida. Fique atento ao canal que será\n" +
                        "gerado e as perguntas feitas pelo BOT.\n\n" +
                        "Clique em " + event.getGuild().getEmoteById("708872093181280276").getAsMention() + " para demissão\n\n" +
                        "Clique em " + event.getGuild().getEmoteById("708872158977458228").getAsMention() + " para anotação");
                embedBuilder.setFooter("Dior TopStar Models");
            })).queue(message -> {
                message.addReaction(event.getGuild().getEmoteById("708872093181280276")).queue();
                message.addReaction(event.getGuild().getEmoteById("708872158977458228")).queue();
            });
        }

        if (event.getMessage().getContentDisplay().equals("!organizacional")) {
            event.getChannel().sendFile(new File("C:" + File.separator + "Users" + File.separator + "brasp" + File.separator + "Downloads", "organizacional.png")).queue();
            event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                embedBuilder.setColor(event.getGuild().getSelfMember().getColor());
                embedBuilder.setTitle("Criação de Ticket - Dior");
                embedBuilder.setDescription(
                        "· Para a confecção do relatório presencial, será criado um ticket\n" +
                                "Fique atento ao canal que será gerado e as perguntas feitas pelo\n" +
                                "BOT.\n\n" +
                                "Clique em " + event.getGuild().getEmoteById("708872093181280276").getAsMention() + " para iniciar o ticket");
                embedBuilder.setFooter("Dior TopStar Models");
            })).queue(message -> {
                message.addReaction(event.getGuild().getEmoteById("708872093181280276")).queue();
            });
        }

//        if (event.getMessage().getContentDisplay().equals("!ouvidoria")) {
//            event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
//                embedBuilder.setColor(event.getGuild().getSelfMember().getColor());
//                embedBuilder.setTitle("Criação de Ticket - Dior");
//                embedBuilder.setDescription("Clique em " + event.getGuild().getEmoteById("708872093181280276").getAsMention() + " para retirar uma dúvida\n" +
//                        "Clique em " + event.getGuild().getEmoteById("718913765449400381").getAsMention() + " para enviar uma reclamação\n" +
//                        "Clique em " + event.getGuild().getEmoteById("708872158977458228").getAsMention() + " para enviar uma sugestão");
//                embedBuilder.setFooter("Dior TopStar Models");
//            })).queue(message -> {
//                message.addReaction(event.getGuild().getEmoteById("708872093181280276")).queue();
//                message.addReaction(event.getGuild().getEmoteById("718913765449400381")).queue();
//                message.addReaction(event.getGuild().getEmoteById("708872158977458228")).queue();
//            });
//        }

        if (event.getMessage().getContentDisplay().equals("!cantada")) {
            event.getChannel().sendFile(new File("C:" + File.separator + "Users" + File.separator + "brasp" + File.separator + "Downloads", "cantada.png")).queue();
            event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                embedBuilder.setColor(event.getGuild().getSelfMember().getColor());
                embedBuilder.setTitle("Criação de Ticket - Dior");
                embedBuilder.setDescription(
                        "· Para o envio da cantada,  será  criado  um  processo\n" +
                                "Fique atento às mensagens enviadas em seu privado.\n\n" +
                                "Clique em " + event.getGuild().getEmoteById("708872093181280276").getAsMention() + " para iniciar o processo");
                embedBuilder.setFooter("Dior TopStar Models");
            })).queue(message -> {
                message.addReaction(event.getGuild().getEmoteById("708872093181280276")).queue();
            });
        }

        if (event.getMessage().getContentDisplay().equals("!ouvidoria")) {
            event.getChannel().sendMessage(DiscordAPI.createEmbed(embedBuilder -> {
                embedBuilder.setColor(event.getGuild().getSelfMember().getColor());
                embedBuilder.setTitle("Criação de Ticket - Dior");
                embedBuilder.setDescription("Clique em " + event.getGuild().getEmoteById("708872093181280276").getAsMention() + " para abrir uma dúvida\n" +
                        "Clique em " + event.getGuild().getEmoteById("718913765449400381").getAsMention() + " para ticket de reclamação\n" +
                        "Clique em " + event.getGuild().getEmoteById("708872158977458228").getAsMention() + " para ticket de sugestão");
                embedBuilder.setFooter("Dior TopStar Models");
            })).queue(message -> {
                message.addReaction(event.getGuild().getEmoteById("708872093181280276")).queue();
                message.addReaction(event.getGuild().getEmoteById("718913765449400381")).queue();
                message.addReaction(event.getGuild().getEmoteById("708872158977458228")).queue();
            });
        }
    }

    private Collaborator getCollaborator(String discordId) {
        return collaboratorDao.findByDiscordId(discordId).orElse(null);
    }

}
