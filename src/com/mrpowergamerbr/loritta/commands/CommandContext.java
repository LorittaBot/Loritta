package com.mrpowergamerbr.loritta.commands;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.loritta.utils.LorittaUser;
import com.mrpowergamerbr.loritta.utils.LorittaUtils;
import com.mrpowergamerbr.temmiewebhook.DiscordMessage;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;

import lombok.Getter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Contexto do comando executado
 */
@Getter
public class CommandContext {
	private LorittaUser lorittaUser;
	private MessageReceivedEvent event;
	private CommandBase cmd;
	private String[] args;

	public CommandContext(ServerConfig conf, MessageReceivedEvent event, CommandBase cmd, String[] args) {	
		this.lorittaUser = new LorittaUser(event.getMember(), conf);
		this.event = event;
		this.cmd = cmd;
		this.args = args;
	}

	public CommandContext(Member member, ServerConfig conf, MessageReceivedEvent event, CommandBase cmd, String[] args) {
		this.lorittaUser = new LorittaUser(member, conf);
		this.event = event;
		this.cmd = cmd;
		this.args = args;
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
		CommandOptions cmdOptions = getLorittaUser().getConfig().getCommandOptionsFor(cmd);
		if (cmdOptions.override()) {
			return (cmdOptions.mentionOnCommandOutput() ? lorittaUser.getMember().getAsMention() + (addSpace ? " " : "") : "");
		} else {
			return lorittaUser.getAsMention(true);
		}
	}

	public Guild getGuild() {
		return event.getGuild();
	}

	public void sendMessage(String message) {
		sendMessage(new MessageBuilder().append(message).build());
	}

	public void sendMessage(Message message) {
		boolean privateReply = getLorittaUser().getConfig().commandOutputInPrivate();
		CommandOptions cmdOptions = getLorittaUser().getConfig().getCommandOptionsFor(cmd);
		if (cmdOptions.override() && cmdOptions.commandOutputInPrivate()) {
			privateReply = cmdOptions.commandOutputInPrivate();
		}
		if (privateReply) {
			getLorittaUser().getMember().getUser().openPrivateChannel().queue((t) -> {
				t.sendMessage(message).complete();
			});
		} else {
			if (event.getTextChannel().canTalk()) {
				event.getTextChannel().sendMessage(message).complete();
			} else {
				LorittaUtils.warnOwnerNoPermission(getGuild(), event.getTextChannel(), lorittaUser.getConfig());
			}
		}
	}

	public void sendMessage(MessageEmbed embed) {
		boolean privateReply = getLorittaUser().getConfig().commandOutputInPrivate();
		CommandOptions cmdOptions = getLorittaUser().getConfig().getCommandOptionsFor(cmd);
		if (cmdOptions.override() && cmdOptions.commandOutputInPrivate()) {
			privateReply = cmdOptions.commandOutputInPrivate();
		}
		if (privateReply) {
			getLorittaUser().getMember().getUser().openPrivateChannel().queue((t) -> {
				t.sendMessage(embed).complete();
			});
		} else {
			if (event.getTextChannel().canTalk()) {
				event.getTextChannel().sendMessage(embed).complete();
			} else {
				LorittaUtils.warnOwnerNoPermission(getGuild(), event.getTextChannel(), lorittaUser.getConfig());
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
			sendMessage(builder.build());
		}
	}
	public void sendFile(InputStream data, String name, String message) {
		sendFile(data, name, new MessageBuilder().append(message).build());
	}

	public void sendFile(InputStream data, String name, Message message) {
		boolean privateReply = getLorittaUser().getConfig().commandOutputInPrivate();
		CommandOptions cmdOptions = getLorittaUser().getConfig().getCommandOptionsFor(cmd);
		if (cmdOptions.override() && cmdOptions.commandOutputInPrivate()) {
			privateReply = cmdOptions.commandOutputInPrivate();
		}
		if (privateReply) {
			getLorittaUser().getMember().getUser().openPrivateChannel().queue((t) -> {
				t.sendFile(data, name, message).complete();
			});
		} else {
			if (event.getTextChannel().canTalk()) {
				event.getTextChannel().sendFile(data, name, message).complete();
			} else {
				LorittaUtils.warnOwnerNoPermission(getGuild(), event.getTextChannel(), lorittaUser.getConfig());
			}
		}
	}

	public void sendFile(File file, String name, String message) throws IOException {
		sendFile(file, name, new MessageBuilder().append(message).build());
	}

	public void sendFile(File file, String name, Message message) throws IOException {
		boolean privateReply = getLorittaUser().getConfig().commandOutputInPrivate();
		CommandOptions cmdOptions = getLorittaUser().getConfig().getCommandOptionsFor(cmd);
		if (cmdOptions.override() && cmdOptions.commandOutputInPrivate()) {
			privateReply = cmdOptions.commandOutputInPrivate();
		}
		if (privateReply) {
			getLorittaUser().getMember().getUser().openPrivateChannel().queue((t) -> {
				try {
					t.sendFile(file, name, message).complete();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} else {
			if (event.getTextChannel().canTalk()) {
				event.getTextChannel().sendFile(file, name, message).complete();
			} else {
				LorittaUtils.warnOwnerNoPermission(getGuild(), event.getTextChannel(), lorittaUser.getConfig());
			}
		}
	}
}
