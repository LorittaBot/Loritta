package com.mrpowergamerbr.loritta.commands;

import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.vanilla.misc.AjudaCommand;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.loritta.utils.LorittaUser;
import com.mrpowergamerbr.loritta.utils.LorittaUtils;
import com.mrpowergamerbr.temmiewebhook.DiscordEmbed;
import com.mrpowergamerbr.temmiewebhook.DiscordMessage;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;
import lombok.Getter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Contexto do comando executado
 */
@Getter
public class CommandContext {
	public LorittaUser lorittaUser;
	public MessageReceivedEvent event;
	public CommandBase cmd;
	public String[] args;
	public String[] rawArgs;
	public HashMap<String, Object> metadata = new HashMap<>();

	public CommandContext(ServerConfig conf, MessageReceivedEvent event, CommandBase cmd, String[] args, String[] rawArgs) {
		this.lorittaUser = new LorittaUser(event.getMember(), conf, LorittaLauncher.getInstance().getLorittaProfileForUser(event.getMember().getUser().getId()));
		this.event = event;
		this.cmd = cmd;
		this.args = args;
		this.rawArgs = rawArgs;
	}

	public CommandContext(Member member, ServerConfig conf, MessageReceivedEvent event, CommandBase cmd, String[] args, String[] rawArgs) {
		this.lorittaUser = new LorittaUser(member, conf, LorittaLauncher.getInstance().getLorittaProfileForUser(event.getMember().getUser().getId()));
		this.event = event;
		this.cmd = cmd;
		this.args = args;
		this.rawArgs = rawArgs;
	}

	public void explain() {
		cmd.explain(lorittaUser.getConfig(), event);
	}

	public Message getMessage() {
		return event.getMessage();
	}

	public ServerConfig getConfig() {
		return lorittaUser.getConfig();
	}

	public Member getHandle() {
		return lorittaUser.getMember();
	}

	public User getUserHandle() {
		return lorittaUser.getMember().getUser();
	}

	public String getAsMention() {
		return lorittaUser.getAsMention();
	}

	public String getAsMention(boolean addSpace) {
		if (cmd != null) {
			CommandOptions cmdOptions = getLorittaUser().getConfig().getCommandOptionsFor(cmd);
			if (cmdOptions.override()) {
				return (cmdOptions.mentionOnCommandOutput() ?
						lorittaUser.getMember().getAsMention() + (addSpace ? " " : "") :
						"");
			}
		}
		return lorittaUser.getAsMention(true);
	}

	public Guild getGuild() {
		return event.getGuild();
	}

	public Message sendMessage(String message) {
		return sendMessage(new MessageBuilder().append(message).build());
	}

	public Message sendMessage(Message message) {
		boolean privateReply = getLorittaUser().getConfig().commandOutputInPrivate();
		if (cmd != null) {
			CommandOptions cmdOptions = getLorittaUser().getConfig().getCommandOptionsFor(cmd);
			if (cmdOptions.override() && cmdOptions.commandOutputInPrivate()) {
				privateReply = cmdOptions.commandOutputInPrivate();
			}
		}
		if (privateReply || cmd instanceof AjudaCommand) {
			return getLorittaUser().getMember().getUser().openPrivateChannel().complete().sendMessage(message).complete();
		} else {
			if (event.getTextChannel().canTalk()) {
				Message sentMessage = event.getTextChannel().sendMessage(message).complete();
				LorittaLauncher.getInstance().messageContextCache.put(sentMessage.getId(), this);
				return sentMessage;
			} else {
				LorittaUtils.warnOwnerNoPermission(getGuild(), event.getTextChannel(), lorittaUser.getConfig());
				return null;
			}
		}
	}

	public Message sendMessage(MessageEmbed embed) {
		boolean privateReply = getLorittaUser().getConfig().commandOutputInPrivate();
		if (cmd != null) {
			CommandOptions cmdOptions = getLorittaUser().getConfig().getCommandOptionsFor(cmd);
			if (cmdOptions.override() && cmdOptions.commandOutputInPrivate()) {
				privateReply = cmdOptions.commandOutputInPrivate();
			}
		}
		if (privateReply || cmd instanceof AjudaCommand) {
			return getLorittaUser().getMember().getUser().openPrivateChannel().complete().sendMessage(embed).complete();
		} else {
			if (event.getTextChannel().canTalk()) {
				return event.getTextChannel().sendMessage(embed).complete();
			} else {
				LorittaUtils.warnOwnerNoPermission(getGuild(), event.getTextChannel(), lorittaUser.getConfig());
				return null;
			}
		}
	}

	public void sendMessage(TemmieWebhook webhook, DiscordMessage message) {
		if (webhook != null) { // Se a webhook é diferente de null, então use a nossa webhook disponível!
			webhook.sendMessage(message);
		} else { // Se não, iremos usar embeds mesmo...
			EmbedBuilder builder = new EmbedBuilder();
			builder.setAuthor(message.getUsername(), null, message.getAvatarUrl());
			builder.setDescription(message.getContent());
			builder.setFooter("Não consigo usar as permissões de webhook aqui... então estou usando o modo de pobre!", null);

			for (DiscordEmbed embed : message.getEmbeds()) {
                builder.setImage(embed.getImage() != null ? embed.getImage().getUrl() : null);
                if (embed.getTitle() != null) {
                    builder.setTitle(builder.getDescriptionBuilder().toString() + "\n\n**" + embed.getTitle() + "**");
                }
                if (embed.getDescription() != null) {
                    builder.setDescription(builder.getDescriptionBuilder().toString() + "\n\n" + embed.getDescription());
                }
                if (embed.getThumbnail() != null) {
                    builder.setThumbnail(embed.getThumbnail().getUrl());
                }
            }
			sendMessage(builder.build());
		}
	}

	public Message sendFile(InputStream data, String name, String message) {
		return sendFile(data, name, new MessageBuilder().append(message).build());
	}

	public Message sendFile(InputStream data, String name, Message message) {
		boolean privateReply = getLorittaUser().getConfig().commandOutputInPrivate();
		if (cmd != null) {
			CommandOptions cmdOptions = getLorittaUser().getConfig().getCommandOptionsFor(cmd);
			if (cmdOptions.override() && cmdOptions.commandOutputInPrivate()) {
				privateReply = cmdOptions.commandOutputInPrivate();
			}
		}
		if (privateReply || cmd instanceof AjudaCommand) {
			return getLorittaUser().getMember().getUser().openPrivateChannel().complete().sendFile(data, name, message).complete();
		} else {
			if (event.getTextChannel().canTalk()) {
				return event.getTextChannel().sendFile(data, name, message).complete();
			} else {
				LorittaUtils.warnOwnerNoPermission(getGuild(), event.getTextChannel(), lorittaUser.getConfig());
				return null;
			}
		}
	}

	public Message sendFile(File file, String name, String message) throws IOException {
		return sendFile(file, name, new MessageBuilder().append(message).build());
	}

	public Message sendFile(File file, String name, Message message) throws IOException {
		boolean privateReply = getLorittaUser().getConfig().commandOutputInPrivate();
		if (cmd != null) {
			CommandOptions cmdOptions = getLorittaUser().getConfig().getCommandOptionsFor(cmd);
			if (cmdOptions.override() && cmdOptions.commandOutputInPrivate()) {
				privateReply = cmdOptions.commandOutputInPrivate();
			}
		}
		if (privateReply || cmd instanceof AjudaCommand) {
			return getLorittaUser().getMember().getUser().openPrivateChannel().complete().sendFile(file, name, message).complete();
		} else {
			if (event.getTextChannel().canTalk()) {
				return event.getTextChannel().sendFile(file, name, message).complete();
			} else {
				LorittaUtils.warnOwnerNoPermission(getGuild(), event.getTextChannel(), lorittaUser.getConfig());
				return null;
			}
		}
	}
}
