package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Icon
import net.dv8tion.jda.core.exceptions.ErrorResponseException

class AddEmojiCommand : AbstractCommand("addemoji", listOf("adicionaremoji"), CommandCategory.DISCORD) {
	override fun getUsage(locale: BaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.TEXT) {
				optional = false
			}
			argument(ArgumentType.IMAGE) {
				optional = false
			}
		}
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.commands.addEmoji.description
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_EMOTES)
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_EMOTES)
	}
	
	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val imageUrl = context.getImageUrlAt(1, 0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
		
		try {
			val os = LorittaUtils.downloadFile(imageUrl, 5000)

			os.use { inputStream ->
				val emote = context.guild.controller.createEmote(context.rawArgs[0], Icon.from(inputStream)).await()
				context.reply(
						LoriReply(
								context.locale.commands.addEmoji.success,
								emote.asMention
						)
				)
			}
		} catch (e: Exception) {
			if (e is ErrorResponseException) {
				if (e.errorCode == 30008) {
					context.reply(
							LoriReply(
									context.locale.commands.addEmoji.limitReached,
									Constants.ERROR
							)
					)
					
					return
				}
			}
			
			context.reply(
					LoriReply(
							context.locale.commands.addEmoji.error,
							Constants.ERROR
					)
			)
		}
	}
}
