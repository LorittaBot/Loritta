package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.GuildLorittaUser
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import kotlin.concurrent.thread

class MusicMessageListener(internal val loritta: Loritta) : ListenerAdapter() {
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
					val locale = loritta.getLocaleById(serverConfig.localeId)
					val lorittaUser = GuildLorittaUser(event.member, serverConfig, lorittaProfile)

					// Primeiro os comandos vanilla da Loritta(tm)
					loritta.commandManager.commandMap.forEach { cmd ->
						if (serverConfig.debugOptions.enableAllModules || !serverConfig.disabledCommands.contains(cmd.javaClass.simpleName)) {
							if (cmd.handle(event, serverConfig, locale, lorittaUser)) {
								return@execute
							}
						}
					}
				} catch (e: Exception) {
					e.printStackTrace()
					LorittaUtilsKotlin.sendStackTrace(event.message, e)
				}
			}
		}
	}

	override fun onGenericMessageReaction(e: GenericMessageReactionEvent) {
		if (e.user.isBot) {
			return
		} // Ignorar reactions de bots

		if (loritta.messageContextCache.containsKey(e.messageId)) {
			try {
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
			} catch (exception: Exception) {
				exception.printStackTrace()
				LorittaUtilsKotlin.sendStackTrace("[`${e.guild.name}`] **onGenericMessageReaction ${e.member.user.name}**", exception)
			}
		}
	}
}