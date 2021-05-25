package com.mrpowergamerbr.loritta.commands.nashorn

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.MessageUtils
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.serializable.CustomCommandCodeType
import net.perfectdreams.loritta.utils.ExperienceUtils

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