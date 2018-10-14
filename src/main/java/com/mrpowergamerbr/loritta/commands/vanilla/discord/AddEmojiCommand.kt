package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Icon

class AddEmojiCommand : AbstractCommand("addemoji", listOf("adicionaremoji"), CommandCategory.DISCORD) {
	override fun getUsage(): String {
		return "nome link da imagem"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["ADDEMOJI_Description"]
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
								context.locale["EMOJISEARCH_AddSuccess"],
								emote.asMention
						)
				)
			}
		} catch (e: Exception) {
			context.reply(
					LoriReply(
							context.locale["EMOJISEARCH_AddError"],
							Constants.ERROR
					)
			)
		}
	}
}
