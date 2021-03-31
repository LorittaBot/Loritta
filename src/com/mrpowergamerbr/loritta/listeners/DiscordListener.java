package com.mrpowergamerbr.loritta.listeners;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandOptions;
import com.mrpowergamerbr.loritta.commands.custom.CustomCommand;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.loritta.whistlers.CodeBlock;
import com.mrpowergamerbr.loritta.whistlers.ICode;
import com.mrpowergamerbr.loritta.whistlers.IPrecondition;
import com.mrpowergamerbr.loritta.whistlers.ReactionCode;
import com.mrpowergamerbr.loritta.whistlers.ReplyCode;
import com.mrpowergamerbr.loritta.whistlers.Whistler;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class DiscordListener extends ListenerAdapter {
	Loritta loritta;
	public ConcurrentMap<Object, Object> cache = CacheBuilder.newBuilder().maximumSize(100L).expireAfterWrite(1L, TimeUnit.MINUTES).build().asMap();

	public DiscordListener(Loritta loritta) {
		this.loritta = loritta;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) { return; }
		if (event.isFromType(ChannelType.TEXT)) {
			loritta.getExecutor().execute(() -> {
				try {
					// cache.put(event.getMessage().getId(), event.getMessage());
					ServerConfig conf = loritta.getServerConfigForGuild(event.getGuild().getId());

					if (!event.getMessage().getContentDisplay().startsWith(conf.commandPrefix())) { // TODO: Filtrar links
						loritta.getHal().add(event.getMessage().getContentDisplay().toLowerCase());
					}

					for (Whistler whistler : conf.whistlers()) {
						processCode(conf, event.getMessage(), whistler.codes);
					}

					if (event.getTextChannel().getIdLong() == 826650286457487370L) {
						// Primeiro os comandos customizados da Loritta(tm)
						for (CommandBase cmd : loritta.getCommandManager().getCommandMap()) {
							if (conf.debugOptions().enableAllModules() || conf.modules().contains(cmd.getClass().getSimpleName())) {
								if (cmd.handle(event, conf)) {
									// event.getChannel().sendTyping().queue();
									CommandOptions cmdOpti = conf.getCommandOptionsFor(cmd);
									if (conf.deleteMessageAfterCommand() || cmdOpti.deleteMessageAfterCommand()) {
										event.getMessage().delete().complete();
									}
									return;
								}
							}
						}

						// E agora os comandos do servidor
						for (CustomCommand cmd : conf.customCommands()) {
							if (cmd.handle(event, conf)) {
								if (conf.deleteMessageAfterCommand()) {
									event.getMessage().delete().complete();
								}
							}
							return;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
	}

	@Override
	public void onGuildMessageDelete(GuildMessageDeleteEvent e) {
		/* if (cache.containsKey(e.getMessageId())) {
			Message message = (Message) cache.get(e.getMessageId());
			if (10000 >= System.currentTimeMillis() - (message.getEditedTime() != null ? message.getEditedTime() : message.getCreationTime()).toInstant().toEpochMilli()) {
				TemmieWebhook temmie = Loritta.getOrCreateWebhook(message.getTextChannel(), "Message Undeleter");

				String avatar = e.getGuild().getMember(message.getAuthor()).getEffectiveName();
				temmie.sendMessage(DiscordMessage.builder()
						.avatarUrl(message.getAuthor().getEffectiveAvatarUrl())
						.username("[CTRL-Z] " + avatar)
						.content(message.getContent())
						.build());
			}
		} */
	}

	// TODO: Isto não deveria ficar aqui...
	public static void processCode(ServerConfig conf, Message message, List<ICode> codes) {
		try {
			wow:
				for (ICode code : codes) {
					if (code instanceof CodeBlock) {
						CodeBlock codeBlock = (CodeBlock) code;

						boolean valid = false;
						for (IPrecondition precondition : codeBlock.preconditions) {
							valid = precondition.isValid(conf, message);
							if (!valid) {
								break wow;
							}
						}

						processCode(conf, message, ((CodeBlock) code).codes);
					}
					if (code instanceof ReplyCode) {
						ReplyCode replyCode = (ReplyCode) code;

						replyCode.handle(message.getTextChannel());
					}
					if (code instanceof ReactionCode) {
						ReactionCode replyCode = (ReactionCode) code;

						replyCode.handle(message);
					}
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		loritta.getExecutor().execute(() -> {
			try {
				ServerConfig conf = loritta.getServerConfigForGuild(event.getGuild().getId());

				if (conf.joinLeaveConfig().isEnabled()) {
					if (conf.joinLeaveConfig().isTellOnJoin()) {
						Guild guild = event.getGuild();

						List<TextChannel> textChannelList = guild.getTextChannelsByName(conf.joinLeaveConfig().getCanalJoin(), true);

						if (!textChannelList.isEmpty()) {
							TextChannel textChannel = textChannelList.get(0);

							if (textChannel.canTalk()) {
								String msg = conf.joinLeaveConfig().getJoinMessage().replace("%UserMention%", event.getMember().getAsMention());
								textChannel.sendMessage(msg).complete();
							} else {
								Loritta.warnOwnerNoPermission(guild, textChannel, conf);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		loritta.getExecutor().execute(() -> {
			try {
				ServerConfig conf = loritta.getServerConfigForGuild(event.getGuild().getId());

				if (conf.joinLeaveConfig().isEnabled()) {
					if (conf.joinLeaveConfig().isTellOnLeave()) {
						Guild guild = event.getGuild();

						List<TextChannel> textChannelList = guild.getTextChannelsByName(conf.joinLeaveConfig().getCanalLeave(), true);

						if (!textChannelList.isEmpty()) {
							TextChannel textChannel = textChannelList.get(0);

							if (textChannel.canTalk()) {
								String msg = conf.joinLeaveConfig().getLeaveMessage().replace("%UserMention%", event.getMember().getAsMention());
								textChannel.sendMessage(msg).complete();
							} else {
								Loritta.warnOwnerNoPermission(guild, textChannel, conf);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
