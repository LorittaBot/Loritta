package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import com.mrpowergamerbr.loritta.utils.save
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import java.awt.Color

class LanguageCommand : AbstractCommand("language", listOf("linguagem", "speak"), category = CommandCategory.MISC) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["LANGUAGE_DESCRIPTION"]
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_SERVER)
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val embed = EmbedBuilder()
		embed.setColor(Color(0, 193, 223))

		val validLanguages = listOf(
				LocaleWrapper(
						"Português-Brasil",
						loritta.getLocaleById("default"),
						"\uD83C\uDDE7\uD83C\uDDF7"
				),
				LocaleWrapper(
						"Português-Funk",
						loritta.getLocaleById("pt-funk"),
						"<:loritta_quebrada:338679008210190336>"
				),
				LocaleWrapper(
						"Português-Portugal",
						loritta.getLocaleById("pt-pt"),
						"\uD83C\uDDF5\uD83C\uDDF9"
				),
				LocaleWrapper(
						"English-US",
						loritta.getLocaleById("en-us"),
						"\uD83C\uDDFA\uD83C\uDDF8"
				),
				LocaleWrapper(
						"Español",
						loritta.getLocaleById("es-es"),
						"\uD83C\uDDEA\uD83C\uDDF8"
				)
		)

		// TODO: Derp
		embed.setTitle("\uD83C\uDF0E " + context.locale["LANGUAGE_INFO"], "")

		for (wrapper in validLanguages) {
			val translators = wrapper.locale.loritta.translationAuthors.mapNotNull { lorittaShards.getUserById(it) }

			embed.addField(
					wrapper.emoteName + " " + wrapper.name,
					"**Traduzido por:** ${translators.joinToString(transform = { "`${it.name}`" })}",
					true
			)
		}

		val message = context.sendMessage(context.getAsMention(true), embed.build())

		message.onReactionAddByAuthor(context) {
			var localeId = "default"
			if (it.reactionEmote.name == "loritta_quebrada") {
				localeId = "pt-funk"
			}
			if (it.reactionEmote.name == "\uD83C\uDDFA\uD83C\uDDF8") {
				localeId = "en-us"
			}
			if (it.reactionEmote.name == "\uD83C\uDDF5\uD83C\uDDF9") {
				localeId = "pt-pt"
			}
			if (it.reactionEmote.name == "\uD83C\uDDEA\uD83C\uDDF8") {
				localeId = "es-es"
			}

			context.config.localeId = localeId
			loritta save context.config
			val newLocale = loritta.getLocaleById(localeId)
			if (localeId == "default") {
				localeId = "pt-br" // Já que nós já salvamos, vamos trocar o localeId para algo mais "decente"
			}
			context.reply(newLocale["LANGUAGE_USING_LOCALE", localeId], "\uD83C\uDFA4")
			message.delete().queue()
		}

		for (wrapper in validLanguages) {
			message.addReaction(wrapper.emoteName.replace("<", "").replace(">", "")).queue()
		}
	}

	private class LocaleWrapper(
			val name: String,
			val locale: BaseLocale,
			val emoteName: String
	)
}