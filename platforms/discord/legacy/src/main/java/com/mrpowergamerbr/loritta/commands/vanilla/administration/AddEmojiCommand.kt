package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.extensions.await
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply

class AddEmojiCommand : AbstractCommand("addemoji", listOf("adicionaremoji", "createemoji", "criaremoji"), CommandCategory.MODERATION) {
	override fun getUsage(): CommandArguments {
		return arguments {
			argument(ArgumentType.TEXT) {
				optional = false
			}
			argument(ArgumentType.IMAGE) {
				optional = false
			}
		}
	}

	override fun getDescriptionKey() = LocaleKeyData("commands.command.addemoji.description")

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_EMOTES)
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_EMOTES)
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		var imageArgument = 1
		var emoteName: String? = null

		if (context.message.emotes.isNotEmpty()) {
			imageArgument = 0
			emoteName = context.message.emotes[0].name
		}

		if (imageArgument > context.rawArgs.size) {
			context.explain()
			return
		}

		if (emoteName == null)
			emoteName = context.rawArgs[0]

		val imageUrl = context.getImageUrlAt(imageArgument, 1) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		try {
			val os = LorittaUtils.downloadFile(imageUrl, 5000)

			if (os != null) {
				os.use { inputStream ->
					val emote = context.guild.createEmote(emoteName, Icon.from(inputStream)).await()
					context.reply(
                            LorittaReply(
                                    context.locale["commands.command.addemoji.success"],
                                    emote.asMention
                            )
					)
				}
			} else {
				throw RuntimeException("Couldn't download image!")
			}
		} catch (e: Exception) {
			if (e is ErrorResponseException) {
				if (e.errorCode == 30008) {
					context.reply(
                            LorittaReply(
                                    context.locale["commands.command.addemoji.limitReached"],
                                    Constants.ERROR
                            )
					)
					return
				}
				if (e.errorCode == 400) {
					context.reply(
                            LorittaReply(
                                    context.locale["commands.command.addemoji.emoteTooBig", "`256kb`"],
                                    Constants.ERROR
                            )
					)
					return
				}
			}

			context.reply(
                    LorittaReply(
                            context.locale["commands.command.addemoji.error"],
                            Constants.ERROR
                    )
			)
		}
	}
}