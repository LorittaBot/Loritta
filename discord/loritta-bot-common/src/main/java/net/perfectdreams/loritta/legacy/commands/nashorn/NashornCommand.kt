package net.perfectdreams.loritta.legacy.commands.nashorn

import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.utils.MessageUtils
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.serializable.CustomCommandCodeType
import net.perfectdreams.loritta.legacy.utils.ExperienceUtils

/**
 * Comandos usando a Nashorn Engine
 */
class NashornCommand(label: String, val javaScriptCode: String, val codeType: CustomCommandCodeType) : AbstractCommand(label, category = CommandCategory.MISC) {
	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		when (codeType) {
			CustomCommandCodeType.SIMPLE_TEXT -> {
				val customTokens = mutableMapOf<String, String>()

				if (javaScriptCode.contains("{experience") || javaScriptCode.contains("{level") || javaScriptCode.contains("{xp")) {
					customTokens.putAll(
							ExperienceUtils.getExperienceCustomTokens(
									context.config,
									context.handle
							)
					)
				}

				val message = MessageUtils.generateMessage(
						javaScriptCode,
						listOf(
								context.handle,
								context.guild,
								context.message.channel
						),
						context.guild,
						customTokens = customTokens
				) ?: return

				context.sendMessage(message)
			}
			else -> throw RuntimeException("Unsupported code type $codeType")
		}
	}
}