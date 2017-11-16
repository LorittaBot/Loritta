package com.mrpowergamerbr.loritta.commands;

import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.vanilla.misc.AjudaCommand;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.loritta.utils.GuildLorittaUser;
import com.mrpowergamerbr.loritta.utils.LoriReply;
import com.mrpowergamerbr.loritta.utils.LorittaUser;
import com.mrpowergamerbr.loritta.utils.LorittaUtils;
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale;
import com.mrpowergamerbr.temmiewebhook.DiscordEmbed;
import com.mrpowergamerbr.temmiewebhook.DiscordMessage;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;
import lombok.Getter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
	public String[] strippedArgs;
	public HashMap<String, Object> metadata = new HashMap<>();
	public BaseLocale locale = LorittaLauncher.loritta.getLocales().get("default");

	public CommandContext(ServerConfig conf, LorittaUser profile, MessageReceivedEvent event, CommandBase cmd, String[] args, String[] rawArgs, String[] strippedArgs) {
		this.lorittaUser = profile;
		this.event = event;
		this.cmd = cmd;
		this.args = args;
		this.rawArgs = rawArgs;
		this.strippedArgs = strippedArgs;
		this.locale = LorittaLauncher.loritta.getLocaleById(conf.localeId);
	}

	public CommandContext(Member member, ServerConfig conf, MessageReceivedEvent event, CommandBase cmd, String[] args, String[] rawArgs, String[] strippedArgs) {
		this.lorittaUser = new LorittaUser(member.getUser(), conf, LorittaLauncher.getInstance().getLorittaProfileForUser(event.getMember().getUser().getId()));
		this.event = event;
		this.cmd = cmd;
		this.args = args;
		this.rawArgs = rawArgs;
		this.strippedArgs = strippedArgs;
		this.locale = LorittaLauncher.loritta.getLocaleById(conf.localeId);
	}

	public boolean isPrivateChannel() {
		return event.isFromType(ChannelType.PRIVATE);
	}

	public void explain() {
		cmd.explain(this);
	}

	public Message getMessage() {
		return event.getMessage();
	}

	public ServerConfig getConfig() {
		return lorittaUser.getConfig();
	}

	public Member getHandle() {
		if (lorittaUser instanceof GuildLorittaUser) {
			return ((GuildLorittaUser) lorittaUser).getMember();
		}
		throw new RuntimeException("Trying to use getHandle() in LorittaUser!");
	}

	public User getUserHandle() {
		return lorittaUser.getUser();
	}

	public String getAsMention() {
		return lorittaUser.getAsMention();
	}

	/**
	 * Verifica se o usuário tem permissão para utilizar um comando
	 */
	public boolean canUseCommand() { return lorittaUser.canUseCommand(this); }

	public String getAsMention(boolean addSpace) {
		CommandOptions cmdOptions = getLorittaUser().getConfig().getCommandOptionsFor(cmd);
		if (cmdOptions.getOverride()) {
			return (cmdOptions.getMentionOnCommandOutput() ?
					lorittaUser.getUser().getAsMention() + (addSpace ? " " : "") :
					"");
		}
		return lorittaUser.getAsMention(true);
	}

	public Guild getGuild() {
		return event.getGuild();
	}

	public Message reply(String message) {
		return reply(message, null);
	}

	public Message reply(String message, String prefix) {
		return reply(message, prefix, false);
	}

	public Message reply(String message, String prefix, boolean forceMention) {
		String send = "";
		if (prefix != null) {
			send = prefix + " **|** ";
		}
		send = send + (forceMention ? getUserHandle().getAsMention() + " " : getAsMention(true)) + message;
		return sendMessage(send);
	}

	public Message reply(LoriReply... loriReplies) {
		StringBuilder message = new StringBuilder();
		for (LoriReply loriReply : loriReplies) {
			message.append(loriReply.build(this) + "\n");
		}
		return sendMessage(message.toString());
	}

	public Message reply(BufferedImage image, String fileName, LoriReply... loriReplies) {
		StringBuilder message = new StringBuilder();
		for (LoriReply loriReply : loriReplies) {
			message.append(loriReply.build(this) + "\n");
		}
		return sendFile(image, fileName, message.toString());
	}

	public Message sendMessage(String message) {
		return sendMessage(new MessageBuilder().append(message.isEmpty() ? " " : message).build());
	}

	public Message sendMessage(Message message) {
		boolean privateReply = getLorittaUser().getConfig().commandOutputInPrivate();
		CommandOptions cmdOptions = getLorittaUser().getConfig().getCommandOptionsFor(cmd);
		if (cmdOptions.getOverride() && cmdOptions.getCommandOutputInPrivate()) {
			privateReply = cmdOptions.getCommandOutputInPrivate();
		}
		if (privateReply || cmd instanceof AjudaCommand) {
			return getLorittaUser().getUser().openPrivateChannel().complete().sendMessage(message).complete();
		} else {
			if (isPrivateChannel() || event.getTextChannel().canTalk()) {
				Message sentMessage = event.getChannel().sendMessage(message).complete();
				LorittaLauncher.getInstance().getMessageContextCache().put(sentMessage.getId(), this);
				return sentMessage;
			} else {
				LorittaUtils.warnOwnerNoPermission(getGuild(), event.getTextChannel(), lorittaUser.getConfig());
				return null;
			}
		}
	}

	public Message sendMessage(String message, MessageEmbed embed) {
		return sendMessage(new MessageBuilder().setEmbed(embed).append(message.isEmpty() ? " " : message).build());
	}

	public Message sendMessage(MessageEmbed embed) {
		boolean privateReply = getLorittaUser().getConfig().commandOutputInPrivate();
		CommandOptions cmdOptions = getLorittaUser().getConfig().getCommandOptionsFor(cmd);
		if (cmdOptions.getOverride() && cmdOptions.getCommandOutputInPrivate()) {
			privateReply = cmdOptions.getCommandOutputInPrivate();
		}
		if (privateReply || cmd instanceof AjudaCommand) {
			return getLorittaUser().getUser().openPrivateChannel().complete().sendMessage(embed).complete();
		} else {
			if (isPrivateChannel() || event.getTextChannel().canTalk()) {
				Message sentMessage = event.getChannel().sendMessage(embed).complete();
				LorittaLauncher.getInstance().getMessageContextCache().put(sentMessage.getId(), this);
				return sentMessage;
			} else {
				LorittaUtils.warnOwnerNoPermission(getGuild(), event.getTextChannel(), lorittaUser.getConfig());
				return null;
			}
		}
	}

	public void sendMessage(TemmieWebhook webhook, DiscordMessage message) {
		if (!isPrivateChannel() && webhook != null) { // Se a webhook é diferente de null, então use a nossa webhook disponível!
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

	public Message sendFile(BufferedImage image, String name, String message) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", os);
		} catch (Exception e) {}
		InputStream is = new ByteArrayInputStream(os.toByteArray());

		return sendFile(is, name, message);
	}

	public Message sendFile(BufferedImage image, String name, Message message) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", os);
		} catch (Exception e) {}
		InputStream is = new ByteArrayInputStream(os.toByteArray());

		return sendFile(is, name, message);
	}

	public Message sendFile(BufferedImage image, String name, MessageEmbed message) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", os);
		} catch (Exception e) {}
		InputStream is = new ByteArrayInputStream(os.toByteArray());

		return sendFile(is, name, message);
	}

	public Message sendFile(InputStream data, String name, String message) {
		// Corrigir erro ao construir uma mensagem vazia
		MessageBuilder builder = new MessageBuilder();
		builder.append(message.isEmpty() ? " " : message);
		return sendFile(data, name, builder.build());
	}

	public Message sendFile(InputStream data, String name, MessageEmbed message) {
		MessageBuilder messageBuilder = new MessageBuilder();
		messageBuilder.setEmbed(message);
		messageBuilder.append(" ");
		return sendFile(data, name, messageBuilder.build());
	}

	public Message sendFile(InputStream data, String name, Message message) {
		boolean privateReply = getLorittaUser().getConfig().commandOutputInPrivate();
		CommandOptions cmdOptions = getLorittaUser().getConfig().getCommandOptionsFor(cmd);
		if (cmdOptions.getOverride() && cmdOptions.getCommandOutputInPrivate()) {
			privateReply = cmdOptions.getCommandOutputInPrivate();
		}
		if (privateReply || cmd instanceof AjudaCommand) {
			return getLorittaUser().getUser().openPrivateChannel().complete().sendFile(data, name, message).complete();
		} else {
			if (isPrivateChannel() || event.getTextChannel().canTalk()) {
				Message sentMessage = event.getChannel().sendFile(data, name, message).complete();
				LorittaLauncher.getInstance().getMessageContextCache().put(sentMessage.getId(), this);
				return sentMessage;
			} else {
				LorittaUtils.warnOwnerNoPermission(getGuild(), event.getTextChannel(), lorittaUser.getConfig());
				return null;
			}
		}
	}

	public Message sendFile(File file, String name, String message) throws IOException {
		// Corrigir erro ao construir uma mensagem vazia
		MessageBuilder builder = new MessageBuilder();
		builder.append(message.isEmpty() ? " " : message);
		return sendFile(file, name, builder.build());
	}

	public Message sendFile(File file, String name, Message message) throws IOException {
		boolean privateReply = getLorittaUser().getConfig().commandOutputInPrivate();
		CommandOptions cmdOptions = getLorittaUser().getConfig().getCommandOptionsFor(cmd);
		if (cmdOptions.getOverride() && cmdOptions.getCommandOutputInPrivate()) {
			privateReply = cmdOptions.getCommandOutputInPrivate();
		}
		if (privateReply || cmd instanceof AjudaCommand) {
			return getLorittaUser().getUser().openPrivateChannel().complete().sendFile(file, name, message).complete();
		} else {
			if (isPrivateChannel() || event.getTextChannel().canTalk()) {
				Message sentMessage = event.getChannel().sendFile(file, name, message).complete();;
				LorittaLauncher.getInstance().getMessageContextCache().put(sentMessage.getId(), this);
				return sentMessage;
			} else {
				LorittaUtils.warnOwnerNoPermission(getGuild(), event.getTextChannel(), lorittaUser.getConfig());
				return null;
			}
		}
	}
}
