package com.mrpowergamerbr.loritta.commands;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.userdata.LorittaProfile;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.loritta.utils.LorittaUtils;
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin;
import com.mrpowergamerbr.loritta.utils.TextUtilsKt;
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import org.apache.commons.lang3.ArrayUtils;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;

import static com.mrpowergamerbr.loritta.utils.TextUtilsKt.f;

public abstract class CommandBase {
    public abstract String getLabel();

    public String getDescription() {
        return getDescription(LorittaLauncher.loritta.getLocaleById("default"));
    }

    public String getDescription(CommandContext context) {
        // TODO: Temporário
        String description = getDescription(context.locale);
        if (description.equals("Insira descrição do comando aqui!")) {
            return getDescription();
        }
        return description;
    }

    public String getDescription(BaseLocale locale) {
        return "Insira descrição do comando aqui!";
    }

    public CommandCategory getCategory() {
        return CommandCategory.MISC;
    }

    public String getUsage() {
        return null;
    }

    public Map<String, String> getDetailedUsage() {
        return new HashMap<String, String>();
    }

    public List<String> getExample() {
        return Arrays.asList();
    }

    public Map<String, String> getExtendedExamples() { return new HashMap<String, String>(); }

    public List<String> getAliases() {
        return Arrays.asList();
    }

    public boolean hasCommandFeedback() {
        return true;
    }

    public abstract void run(CommandContext context);

    public String getExtendedDescription() {
        return getDescription();
    }

    public boolean needsToUploadFiles() {
        return false;
    }

    public boolean canUseInPrivateChannel() { return true; }

    /**
     * Retorna as permissões necessárias para utilizar este comando
     *
     * @return A lista de permissões necessárias
     */
    public List<Permission> getDiscordPermissions() { return Arrays.asList(); }

    /**
     * Retorna se somente o dono do bot pode executar este comando
     *
     * @return Se somente o dono do bot pode usar este comando
     */
    public boolean onlyOwner() { return false; }

    /**
     * Retorna se o comando precisa ter o sistema de música ativado
     *
     * @return Se o comando precisa ter o sistema de música ativado
     */
    public boolean requiresMusicEnabled() { return false; }

    public boolean handle(MessageReceivedEvent ev, ServerConfig conf, LorittaProfile profile) {
        String message = ev.getMessage().getContent();
        String rawMessage = ev.getMessage().getRawContent();
        boolean run = false;
        boolean byMention = false;
        String label = conf.commandPrefix() + getLabel();
        if (rawMessage.startsWith("<@" + Loritta.config.getClientId() + ">") || rawMessage.startsWith("<@!" + Loritta.config.getClientId() + ">")) {
            byMention = true;
            rawMessage = rawMessage.replaceFirst("<@" + Loritta.config.getClientId() + "> ", "");
            rawMessage = rawMessage.replaceFirst("<@!" + Loritta.config.getClientId() + "> ", "");
            label = getLabel();
        }
        run = rawMessage.replace("\n", " ").split(" ")[0].equalsIgnoreCase(label);
        if (!run) {
            for (String alias : this.getAliases()) {
                label = conf.commandPrefix() + alias;
                if (rawMessage.startsWith(label)) {
                    run = true;
                    break;
                }
                if (byMention) {
                    label = alias;
                    if (rawMessage.startsWith(label)) {
                        run = true;
                        break;
                    }
                }
            }
        }
        if (run) {
            LorittaUtilsKotlin.trackCommands(ev.getMessage());
            if (hasCommandFeedback()) {
            	try {
					if (conf != LorittaLauncher.getInstance().dummyServerConfig && !ev.getTextChannel().canTalk()) { // Se a Loritta não pode falar no canal de texto, avise para o dono do servidor para dar a permissão para ela
						LorittaUtils.warnOwnerNoPermission(ev.getGuild(), ev.getTextChannel(), conf);
						return true;
					} else {
						ev.getChannel().sendTyping().complete();
					}
				} catch (ErrorResponseException e) {
				}
            }
            String cmd = label;
            String[] args = ArrayUtils.remove(TextUtilsKt.stripCodeMarks(message).split(" "), 0);
            String[] rawArgs = ArrayUtils.remove(TextUtilsKt.stripCodeMarks(ev.getMessage().getRawContent()).split(" "), 0);
            String[] strippedArgs = ArrayUtils.remove(TextUtilsKt.stripCodeMarks(ev.getMessage().getStrippedContent()).split(" "), 0);
            if (byMention) {
                args = ArrayUtils.remove(args, 0);
                rawArgs = ArrayUtils.remove(rawArgs, 0);
                strippedArgs = ArrayUtils.remove(strippedArgs, 0);
            }
            CommandContext context = new CommandContext(conf, ev, this, args, rawArgs, strippedArgs);
            if (args.length >= 1 && args[0].equals("🤷")) { // Usar a ajuda caso 🤷 seja usado
                explain(context);
                return true;
            }
            if (LorittaUtils.handleIfBanned(context, profile)) { return true; }
            if (!context.canUseCommand()) {
                context.sendMessage("\uD83D\uDE45 **|** " + context.getAsMention(true) + "**" + f(context.locale.NO_PERMISSION) + "**");
                return true;
            }
            if (context.isPrivateChannel() && !canUseInPrivateChannel()) {
                context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + f(context.locale.CANT_USE_IN_PRIVATE));
                return true;
            }
            if (needsToUploadFiles()) {
                if (!LorittaUtils.canUploadFiles(context)) { return true; };
            }
            if (requiresMusicEnabled()) {
                if (!context.getConfig().musicConfig.isEnabled()) {
                    context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + f(context.locale.DJ_LORITTA_DISABLED) + " \uD83D\uDE1E");
                    return true;
                }
            }
            run(context);
            return true;
        }
        return false;
    }

    public void explain(CommandContext context) {
        ServerConfig conf = context.getConfig();
        MessageReceivedEvent ev = context.getEvent();
        if (conf.explainOnCommandRun()) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(new Color(0, 193, 223));
            embed.setTitle("\uD83E\uDD14 " + context.locale.getHOW_TO_USE() + "... `" + conf.commandPrefix() + this.getLabel() + "`");

            String usage = getUsage() != null ? " `" + getUsage() + "`" : "";

            String cmdInfo = getDescription(context) + "\n\n";

            cmdInfo += "**" + context.locale.getHOW_TO_USE() + ":** " + conf.commandPrefix() + this.getLabel() + usage + "\n";

            if (!this.getDetailedUsage().isEmpty()) {
                for (Map.Entry<String, String> entry : this.getDetailedUsage().entrySet()) {
                    cmdInfo += "`" + entry.getKey() + "` - " + entry.getValue() + "\n";
                }
            }

            cmdInfo += "\n";

            // Criar uma lista de exemplos
            List<String> examples = new ArrayList<String>();
            for (String example : this.getExample()) { // Adicionar todos os exemplos simples
                examples.add(conf.commandPrefix() + this.getLabel() + (example.isEmpty() ? "" : " `" + example + "`"));
            }
            for (Map.Entry<String, String> entry : this.getExtendedExamples().entrySet()) { // E agora vamos adicionar os exemplos mais complexos/extendidos
                examples.add(conf.commandPrefix() + this.getLabel() + (entry.getKey().isEmpty() ? "" : " `" + entry.getKey() + "` - **" + entry.getValue() + "**"));
            }

            if (examples.isEmpty()) {
                cmdInfo += "**" + context.locale.getEXAMPLE() + ":**\n" + conf.commandPrefix() + this.getLabel();
            } else {
                cmdInfo += "**" + context.locale.getEXAMPLE() + (this.getExample().size() == 1 ? "" : "s") + ":**\n";
                for (String example : examples) {
                    cmdInfo += example + "\n";
                }
            }
            embed.setDescription(cmdInfo);
            embed.setFooter(ev.getAuthor().getName() + "#" + ev.getAuthor().getDiscriminator(), ev.getAuthor().getEffectiveAvatarUrl()); // Adicionar quem executou o comando
            embed.setTimestamp(Instant.now());

            if (conf.explainInPrivate()) {
                ev.getAuthor().openPrivateChannel().complete().sendMessage(embed.build()).complete();
            } else {
                ev.getChannel().sendMessage(embed.build()).complete();
            }
        }
    }

    public void onCommandReactionFeedback(CommandContext context, GenericMessageReactionEvent e, Message msg) {
    } // Quando alguém usa uma reaction na mensagem
}