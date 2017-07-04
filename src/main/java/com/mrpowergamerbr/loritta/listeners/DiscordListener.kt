package com.mrpowergamerbr.loritta.listeners

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.LorittaServerUserData
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.humanize
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.awt.Color
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.concurrent.thread

class DiscordListener(internal val loritta: Loritta) : ListenerAdapter() {
	override fun onMessageReceived(event: MessageReceivedEvent) {
		if (event.author.isBot) { // Se uma mensagem de um bot, ignore a mensagem!
			return
		}
		if (event.isFromType(ChannelType.TEXT)) {
			if (event.textChannel.isNSFW) { // lol nope, I'm outta here
				return
			}
			loritta.executor.execute {
				try {
					val conf = loritta.getServerConfigForGuild(event.guild.id)
					val profile = loritta.getLorittaProfileForUser(event.member.user.id)
					val ownerProfile = loritta.getLorittaProfileForUser(event.guild.owner.user.id)

					if (ownerProfile.isBanned) { // Se o dono está banido...
						if (event.member.user.id != Loritta.config.ownerId) { // E ele não é o dono do bot!
							event.guild.leave().complete() // Então eu irei sair daqui, me recuso a ficar em um servidor que o dono está banido! ᕙ(⇀‸↼‶)ᕗ
							return@execute
						}
					}

					if (event.message.rawContent.replace("!", "") == "<@297153970613387264>") {
						event.textChannel.sendMessage("Olá " + event.message.author.asMention + "! Meu prefixo neste servidor é `" + conf.commandPrefix + "` Para ver o que eu posso fazer, use `" + conf.commandPrefix + "ajuda`!").complete()
					}

					for (r in event.member.roles) {
						if (r.name.equals("Inimigo da Loritta", ignoreCase = true)) {
							return@execute
						}
					}

					val lorittaProfile = loritta.getLorittaProfileForUser(event.author.id)
					lorittaProfile.xp = lorittaProfile.xp + 1
					loritta.ds.save(lorittaProfile)

					val userData = (conf.userData as java.util.Map<String, LorittaServerUserData>).getOrDefault(event.member.user.id, LorittaServerUserData())
					userData.xp = userData.xp + 1
					conf.userData.put(event.member.user.id, userData)
					loritta.ds.save(conf)

					if (conf.aminoConfig.fixAminoImages) {
						for (attachments in event.message.attachments) {
							if (attachments.fileName.endsWith(".Amino")) {
								val bufferedImage = LorittaUtils.downloadImage(attachments.url)

								val os = ByteArrayOutputStream()
								try {
									ImageIO.write(bufferedImage!!, "png", os)
								} catch (e: Exception) {
								}

								val `is` = ByteArrayInputStream(os.toByteArray())

								event.textChannel.sendFile(`is`, "amino.png", MessageBuilder().append("(Por " + event.member.asMention + ") **Link para o \".Amino\":** " + attachments.url).build()).complete()
								event.message.delete().complete()
							}
						}
					}

					// Primeiro os comandos vanilla da Loritta(tm)
					for (cmd in loritta.commandManager.commandMap) {
						if (conf.debugOptions.enableAllModules || !conf.disabledCommands.contains(cmd.javaClass.simpleName)) {
							if (cmd.handle(event, conf, profile)) {
								val cmdOpti = conf.getCommandOptionsFor(cmd)
								if (conf.deleteMessageAfterCommand || cmdOpti.deleteMessageAfterCommand) {
									event.message.delete().complete()
								}
								return@execute
							}
						}
					}

					// E depois os comandos usando JavaScript (Nashorn)
					for (cmd in conf.nashornCommands) {
						if (cmd.handle(event, conf, profile)) {
							if (conf.deleteMessageAfterCommand) {
								event.message.delete().complete()
							}
							return@execute
						}
					}

					loritta.hal.add(event.message.content.toLowerCase()) // TODO: Filtrar links
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
		}
	}

	override fun onGenericMessageReaction(e: GenericMessageReactionEvent) {
		if (e.user.isBot) {
			return
		} // Ignorar reactions de bots

		if (LorittaLauncher.getInstance().messageContextCache.containsKey(e.messageId)) {
			val context = LorittaLauncher.getInstance().messageContextCache[e.messageId] as CommandContext
			val t = object : Thread() {
				override fun run() {
					val msg = e.textChannel.getMessageById(e.messageId).complete()
					if (msg != null) {
						context.cmd.onCommandReactionFeedback(context, e, msg)
					}
				}
			}
			t.start()
		}

		thread {
			val msg = e.textChannel.getMessageById(e.messageId).complete()
			if (msg != null) {
				val conf = LorittaLauncher.getInstance().getServerConfigForGuild(e.guild.id)
				val guild = e.guild
				// Sistema de Starboard
				if (e.reactionEmote.name == "⭐") {
					if (conf.starboardConfig.isEnabled) {
						val textChannel = guild.getTextChannelById(conf.starboardConfig.starboardId)

						if (textChannel != null) {
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
							embed.setColor(Color(255, 253 - (count * 0.25).toInt(), 241 - (count * 4)))

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
							starCountMessage.append("⭐ **${count}** ${e.textChannel.asMention}")
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

				if (conf.joinLeaveConfig.isEnabled) {
					if (conf.joinLeaveConfig.tellOnJoin) {
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
}
