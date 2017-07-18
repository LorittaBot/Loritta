package com.mrpowergamerbr.loritta.listeners

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.LorittaServerUserData
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.humanize
import com.mrpowergamerbr.loritta.utils.save
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.audit.ActionType
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.events.guild.GenericGuildEvent
import net.dv8tion.jda.core.events.guild.GuildBanEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.events.guild.GuildUnbanEvent
import net.dv8tion.jda.core.events.guild.member.GenericGuildMemberEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.awt.Color
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Instant
import javax.imageio.ImageIO
import kotlin.concurrent.thread

class DiscordListener(internal val loritta: Loritta) : ListenerAdapter() {
	override fun onMessageReceived(event: MessageReceivedEvent) {
		if (event.author.isBot) { // Se uma mensagem de um bot, ignore a mensagem!
			return
		}
		if (event.isFromType(ChannelType.TEXT)) { // Mensagens em canais de texto
			if (event.textChannel.isNSFW) { // lol nope, I'm outta here
				return
			}
			loritta.executor.execute {
				try {
					val serverConfig = loritta.getServerConfigForGuild(event.guild.id)
					val lorittaProfile = loritta.getLorittaProfileForUser(event.author.id)
					val ownerProfile = loritta.getLorittaProfileForUser(event.guild.owner.user.id)

					if (ownerProfile.isBanned) { // Se o dono está banido...
						if (event.member.user.id != Loritta.config.ownerId) { // E ele não é o dono do bot!
							event.guild.leave().complete() // Então eu irei sair daqui, me recuso a ficar em um servidor que o dono está banido! ᕙ(⇀‸↼‶)ᕗ
							return@execute
						}
					}

					if (event.message.rawContent.replace("!", "") == "<@297153970613387264>") {
						event.textChannel.sendMessage("Olá " + event.message.author.asMention + "! Meu prefixo neste servidor é `" + serverConfig.commandPrefix + "` Para ver o que eu posso fazer, use `" + serverConfig.commandPrefix + "ajuda`!").complete()
					}

					event.member.roles.forEach {
						if (it.name.equals("Inimigo da Loritta", ignoreCase = true)) {
							return@execute
						}
					}

					lorittaProfile.xp = lorittaProfile.xp + 1
					loritta save lorittaProfile

					val userData = (serverConfig.userData as java.util.Map<String, LorittaServerUserData>).getOrDefault(event.member.user.id, LorittaServerUserData())
					userData.xp = userData.xp + 1
					serverConfig.userData.put(event.member.user.id, userData)
					loritta save serverConfig

					if (serverConfig.aminoConfig.fixAminoImages) {
						for (attachments in event.message.attachments) {
							if (attachments.fileName.endsWith(".Amino")) {
								val bufferedImage = LorittaUtils.downloadImage(attachments.url)

								val os = ByteArrayOutputStream()
								ImageIO.write(bufferedImage!!, "png", os)
								val inputStream = ByteArrayInputStream(os.toByteArray())

								event.textChannel.sendFile(inputStream, "amino.png", MessageBuilder().append("(Por " + event.member.asMention + ") **Link para o \".Amino\":** " + attachments.url).build()).complete()
								event.message.delete().complete()
							}
						}
					}

					// Primeiro os comandos vanilla da Loritta(tm)
					loritta.commandManager.commandMap.forEach { cmd ->
						if (serverConfig.debugOptions.enableAllModules || !serverConfig.disabledCommands.contains(cmd.javaClass.simpleName)) {
							if (cmd.handle(event, serverConfig, lorittaProfile)) {
								val cmdOpti = serverConfig.getCommandOptionsFor(cmd)
								if (serverConfig.deleteMessageAfterCommand || cmdOpti.deleteMessageAfterCommand) {
									event.message.delete().complete()
								}
								return@execute
							}
						}
					}

					// E depois os comandos usando JavaScript (Nashorn)
					serverConfig.nashornCommands.forEach { cmd ->
						if (cmd.handle(event, serverConfig, lorittaProfile)) {
							if (serverConfig.deleteMessageAfterCommand) {
								event.message.delete().complete()
							}
							return@execute
						}
					}

					loritta.hal.add(event.message.content.toLowerCase()) // TODO: Filtrar links
				} catch (e: Exception) {
					e.printStackTrace()
					LorittaUtilsKotlin.sendStackTrace(event.message, e)
				}
			}
		} else if (event.isFromType(ChannelType.PRIVATE)) { // Mensagens em DMs
			loritta.executor.execute {
				val serverConfig = LorittaLauncher.loritta.dummyServerConfig
				val profile = loritta.getLorittaProfileForUser(event.author.id) // Carregar perfil do usuário
				if (event.message.rawContent.replace("!", "").trim() == "<@297153970613387264>") {
					event.channel.sendMessage("Olá " + event.message.author.asMention + "! Em DMs você não precisa usar nenhum prefixo para falar comigo! Para ver o que eu posso fazer, use `ajuda`!").complete()
					return@execute
				}

				// Comandos vanilla da Loritta
				loritta.commandManager.commandMap.forEach{ cmd ->
					if (cmd.handle(event, serverConfig, profile)) {
						return@execute
					}
				}
			}
		}
	}

	override fun onGenericMessageReaction(e: GenericMessageReactionEvent) {
		if (e.user.isBot) {
			return
		} // Ignorar reactions de bots

		if (loritta.messageContextCache.containsKey(e.messageId)) {
			val context = LorittaLauncher.getInstance().messageContextCache[e.messageId] as CommandContext
			val t = object : Thread() {
				override fun run() {
					val msg = e.channel.getMessageById(e.messageId).complete()
					if (msg != null) {
						context.cmd.onCommandReactionFeedback(context, e, msg)
					}
				}
			}
			t.start()
		}

		thread {
			val conf = LorittaLauncher.getInstance().getServerConfigForGuild(e.guild.id)
			val guild = e.guild
			// Sistema de Starboard
			if (conf.starboardConfig.isEnabled) {
				if (e.reactionEmote.name == "⭐") {
					val msg = e.textChannel.getMessageById(e.messageId).complete()
					if (msg != null) {
						val textChannel = guild.getTextChannelById(conf.starboardConfig.starboardId)

						if (textChannel != null && msg.textChannel != textChannel) { // Verificar se não é null e verificar se a reaction não foi na starboard
							var starboardMessageId = conf.starboardEmbeds[e.messageId]
							var starboardMessage: Message? = null;
							if (starboardMessageId != null) {
								starboardMessage = textChannel.getMessageById(starboardMessageId).complete()
							}

							val embed = EmbedBuilder()
							val count = e.reaction.users.complete().size;
							var content = msg.rawContent
							embed.setAuthor(msg.author.name, null, msg.author.effectiveAvatarUrl)
							embed.setFooter(msg.creationTime.humanize(), null)
							embed.setColor(Color(255, 255, 200 - (count * 20)))

							var emoji = "⭐";

							if (count >= 5) {
								emoji = "\uD83C\uDF1F";
							}
							if (count >= 10) {
								emoji = "\uD83C\uDF20";
							}
							if (count >= 15) {
								emoji = "\uD83D\uDCAB";
							}
							if (count >= 20) {
								emoji = "\uD83C\uDF0C";
							}

							var hasImage = false;
							if (msg.attachments.isNotEmpty()) { // Se tem attachments...
								content += "\n**Arquivos:**\n"
								for (attach in msg.attachments) {
									if (attach.isImage && !hasImage) { // Se é uma imagem...
										embed.setImage(attach.url) // Então coloque isso como a imagem no embed!
										hasImage = true;
									}
									content += attach.url + "\n"
								}
							}

							embed.setDescription(content)

							val starCountMessage = MessageBuilder()
							starCountMessage.append("$emoji **${count}** ${e.textChannel.asMention}")
							starCountMessage.setEmbed(embed.build())

							if (starboardMessage != null) {
								if (1 > count) { // Remover embed já que o número de stars é menos que 0
									starboardMessage.delete().complete()
									conf.starboardEmbeds.remove(msg.id)
									LorittaLauncher.loritta.ds.save(conf)
									return@thread;
								}
								starboardMessage.editMessage(starCountMessage.build()).complete()
							} else {
								starboardMessage = textChannel.sendMessage(starCountMessage.build()).complete()
							}
							conf.starboardEmbeds.put(msg.id, starboardMessage?.id)
							LorittaLauncher.loritta.ds.save(conf)
						}
					}
				}
			}
		}
	}

	override fun onGuildLeave(e: GuildLeaveEvent) {
		// Quando a Loritta sair de uma guild, automaticamente remova o ServerConfig daquele servidor

		LorittaLauncher.loritta.mongo
				.getDatabase("loritta")
				.getCollection("servers")
				.deleteMany(Filters.eq("_id", e.guild.id)) // Tchau! :(
	}

	override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
		loritta.executor.execute {
			try {
				val conf = loritta.getServerConfigForGuild(event.guild.id)

				if (conf.autoroleConfig.isEnabled) { // Está ativado?
					val rolesId = conf.autoroleConfig.roles // Então vamos pegar todos os IDs...

					val roles = mutableListOf<Role>()

					rolesId.forEach { // E pegar a role dependendo do ID!
						val role = event.guild.getRoleById(it)

						if (role != null) {
							roles.add(role)
						}
					}

					event.guild.controller.addRolesToMember(event.member, roles).complete() // E adicione todas as roles no usuário
				}

				if (conf.joinLeaveConfig.isEnabled) { // Está ativado?
					if (conf.joinLeaveConfig.tellOnJoin) { // E o sistema de avisar ao entrar está ativado?
						val guild = event.guild

						val textChannel = guild.getTextChannelById(conf.joinLeaveConfig.canalJoinId)

						if (textChannel != null) {
							if (textChannel.canTalk()) {
								val msg = LorittaUtils.replaceTokens(conf.joinLeaveConfig.joinMessage, event)
								textChannel.sendMessage(msg).complete()
							} else {
								LorittaUtils.warnOwnerNoPermission(guild, textChannel, conf)
							}
						}
					}
					if (conf.joinLeaveConfig.tellOnPrivate) { // Talvez o sistema de avisar no privado esteja ativado!
						val msg = LorittaUtils.replaceTokens(conf.joinLeaveConfig.joinPrivateMessage, event)
						event.user.openPrivateChannel().complete().sendMessage(msg).complete() // Pronto!
					}
				}
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}

	override fun onGuildMemberLeave(event: GuildMemberLeaveEvent?) {
		loritta.executor.execute {
			try {
				val conf = loritta.getServerConfigForGuild(event!!.guild.id)

				if (conf.joinLeaveConfig.isEnabled) {
					if (conf.joinLeaveConfig.tellOnLeave) {
						val guild = event.guild

						val textChannel = guild.getTextChannelById(conf.joinLeaveConfig.canalLeaveId)

						if (textChannel != null) {
							if (textChannel.canTalk()) {
								val msg = LorittaUtils.replaceTokens(conf.joinLeaveConfig.leaveMessage, event)
								textChannel.sendMessage(msg).complete()
							} else {
								LorittaUtils.warnOwnerNoPermission(guild, textChannel, conf)
							}
						}
					}
				}
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}

	// ===[ EVENT LOG ]===
	override fun onGenericGuild(event: GenericGuildEvent) {
		thread {
			val eventLogConfig = loritta.getServerConfigForGuild(event.guild.id).eventLogConfig
			if (eventLogConfig.isEnabled) {
				val textChannel = event.guild.getTextChannelById(eventLogConfig.eventLogChannelId);

				if (textChannel != null) {
					val embed = EmbedBuilder()
					embed.setTimestamp(Instant.now())

					// ===[ VOICE JOIN ]===
					if (event is GuildVoiceJoinEvent && eventLogConfig.voiceChannelJoins) {
						embed.setColor(Color(35, 209, 96))

						embed.setAuthor("${event.member.user.name}#${event.member.user.discriminator}", null, event.member.user.effectiveAvatarUrl)
						embed.setDescription("\uD83D\uDC49\uD83C\uDFA4 **${event.member.asMention} entrou no canal de voz `${event.channelJoined.name}`**")
						embed.setFooter("ID do usuário: ${event.member.user.id}", null)

						textChannel.sendMessage(embed.build()).complete()
						return@thread;
					}
					// ===[ VOICE LEAVE ]===
					if (event is GuildVoiceLeaveEvent && eventLogConfig.voiceChannelLeaves) {
						embed.setColor(Color(35, 209, 96))

						embed.setAuthor("${event.member.user.name}#${event.member.user.discriminator}", null, event.member.user.effectiveAvatarUrl)
						embed.setDescription("\uD83D\uDC48\uD83C\uDFA4 **${event.member.asMention} saiu do canal de voz `${event.channelLeft.name}`**")
						embed.setFooter("ID do usuário: ${event.member.user.id}", null)

						textChannel.sendMessage(embed.build()).complete()
						return@thread;
					}
					// ===[ USER BANNED ]===
					if (event is GuildBanEvent && eventLogConfig.memberBanned) {
						embed.setColor(Color(35, 209, 96))

						var message = "\uD83D\uDEAB **${event.user.name} foi banido!**";

						if (event.guild.selfMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) {
							// Caso a Loritta consiga ver o audit log, vamos pegar quem baniu e o motivo do ban!
							val auditLog = event.guild.auditLogs.complete().first()

							if (auditLog.type == ActionType.BAN) {
								message += "\n**Banido por:** ${auditLog.user.asMention}";
								message += "\n**Motivo:** `${if (auditLog.reason == null) "\uD83E\uDD37 Nenhum motivo" else auditLog.reason}`";
							}
						}
						embed.setAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl)
						embed.setDescription(message)
						embed.setFooter("ID do usuário: ${event.user.id}", null)

						textChannel.sendMessage(embed.build()).complete()
						return@thread;
					}
					// ===[ USER UNBANNED ]===
					if (event is GuildUnbanEvent && eventLogConfig.memberUnbanned) {
						embed.setColor(Color(35, 209, 96))

						var message = "\uD83E\uDD1D **${event.user.name} foi desbanido!**";

						if (event.guild.selfMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) {
							// Caso a Loritta consiga ver o audit log, vamos pegar quem baniu e o motivo do ban!
							val auditLog = event.guild.auditLogs.complete().first()

							if (auditLog.type == ActionType.UNBAN) {
								message += "\n**Desbanido por:** ${auditLog.user.asMention}";
							}
						}
						embed.setAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl)
						embed.setDescription(message)
						embed.setFooter("ID do usuário: ${event.user.id}", null)

						textChannel.sendMessage(embed.build()).complete()
						return@thread;
					}
					// ===[ GENERIC MEMBER EVENT ]===
					if (event is GenericGuildMemberEvent) {
						embed.setColor(Color(35, 209, 96))

						embed.setAuthor("${event.member.user.name}#${event.member.user.discriminator}", null, event.member.user.effectiveAvatarUrl)

						// ===[ NICKNAME ]===
						if (event is GuildMemberNickChangeEvent && eventLogConfig.nicknameChanges) {
							embed.setDescription("\uD83D\uDCDD **Nickname de ${event.member.asMention} foi alterado!\n\nAntigo nickname: `${if (event.prevNick == null) "\uD83E\uDD37 Nenhum nickname" else event.prevNick}`\nNovo nickname: `${if (event.newNick == null) "\uD83E\uDD37 Nenhum nickname" else event.newNick}`**")
							embed.setFooter("ID do usuário: ${event.member.user.id}", null)

							textChannel.sendMessage(embed.build()).complete()
							return@thread;
						}
					}
				}
			}
		}
	}
}
