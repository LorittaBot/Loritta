package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import dev.kord.common.entity.Permission
import dev.kord.rest.json.JsonErrorCode
import dev.kord.rest.request.KtorRequestException
import io.ktor.http.*
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.LorittaBot

class AddEmojiCommand(loritta: LorittaBot) : AbstractCommand(loritta, "addemoji", listOf("adicionaremoji", "createemoji", "criaremoji"), CommandCategory.MODERATION) {
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
		return listOf(Permission.ManageEmojisAndStickers)
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.ManageEmojisAndStickers)
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
			val os = LorittaUtils.downloadFile(loritta, imageUrl, 5000)

			if (os != null) {
				os.use { inputStream ->
					val emote = context.guild.createEmote(emoteName, inputStream.readAllBytes())
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
			if (e is KtorRequestException) {
				if (e.error?.code == JsonErrorCode.MaxEmojis || e.error?.code == JsonErrorCode.MaxAnimatedEmojis) {
					context.reply(
                            LorittaReply(
                                    context.locale["commands.command.addemoji.limitReached"],
                                    Constants.ERROR
                            )
					)
					return
				}
				if (e.httpResponse.status == HttpStatusCode.BadRequest) {
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