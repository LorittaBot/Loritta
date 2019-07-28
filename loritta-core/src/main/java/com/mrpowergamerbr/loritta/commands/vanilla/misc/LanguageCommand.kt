package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.extensions.edit
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import com.mrpowergamerbr.loritta.utils.save
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.perfectdreams.loritta.api.commands.CommandCategory
import java.awt.Color

class LanguageCommand : AbstractCommand("language", listOf("linguagem", "speak"), category = CommandCategory.MISC) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["commands.miscellaneous.language.description", "\uD83D\uDE0A"]
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_SERVER)
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		val embed = EmbedBuilder()
		embed.setColor(Color(0, 193, 223))

		val validLanguages = listOf(
				LocaleWrapper(
						"Português-Brasil",
						loritta.getLocaleById("default"),
						loritta.getLegacyLocaleById("default"),
						"\uD83C\uDDE7\uD83C\uDDF7"
				),
				LocaleWrapper(
						"Português-Portugal",
						loritta.getLocaleById("pt-pt"),
						loritta.getLegacyLocaleById("pt-pt"),
						"\uD83C\uDDF5\uD83C\uDDF9"
				),
				LocaleWrapper(
						"English-US",
						loritta.getLocaleById("en-us"),
						loritta.getLegacyLocaleById("en-us"),
						"\uD83C\uDDFA\uD83C\uDDF8"
				),
				LocaleWrapper(
						"Español",
						loritta.getLocaleById("es-es"),
						loritta.getLegacyLocaleById("es-es"),
						"\uD83C\uDDEA\uD83C\uDDF8"
				),
				LocaleWrapper(
						"Português-Funk",
						loritta.getLocaleById("pt-funk"),
						loritta.getLegacyLocaleById("pt-funk"),
						"<:loritta_quebrada:338679008210190336>"
				),
				LocaleWrapper(
						"Português-Furry",
						loritta.getLocaleById("pt-furry"),
						loritta.getLegacyLocaleById("default"),
						"\uD83D\uDC3E"
				)
		)

		val message = context.sendMessage(
				context.getAsMention(true),
				buildLanguageEmbed(
						locale.toNewLocale(),
						validLanguages.subList(0, 4)
				)
		)

		message.onReactionAddByAuthor(context) {
			if (it.reactionEmote.isEmote("426183783008698391")) {
				message.edit(
						" ",
						buildLanguageEmbed(
								locale.toNewLocale(),
								validLanguages.subList(4, validLanguages.size)
						),
						true
				)

				for (wrapper in validLanguages.subList(4, validLanguages.size)) {
					// O "replace" é necessário já que a gente usa emojis personalizados para algumas linguagens
					message.addReaction(wrapper.emoteName.replace("<", "").replace(">", "")).queue()
				}
				return@onReactionAddByAuthor
			}

			var localeId = "default"

			val newLanguage = validLanguages.firstOrNull { language ->
				if (language.emoteName.startsWith("<")) {
					it.reactionEmote.isEmote(language.emoteName.split(":")[2].removeSuffix(">"))
				} else {
					it.reactionEmote.isEmote(language.emoteName)
				}
			}
			
			localeId = newLanguage?.locale?.id ?: localeId

			context.config.localeId = localeId
			loritta save context.config

			val newLocale = loritta.getLocaleById(localeId)
			if (localeId == "default") {
				localeId = "pt-br" // Já que nós já salvamos, vamos trocar o localeId para algo mais "decente"
			}

			context.reply(newLocale["commands.miscellaneous.language.languageChanged", "`$localeId`"], "\uD83C\uDFA4")
			message.delete().queue()
		}

		for (wrapper in validLanguages.subList(0, 4)) {
			// O "replace" é necessário já que a gente usa emojis personalizados para algumas linguagens
			message.addReaction(wrapper.emoteName.replace("<", "").replace(">", "")).queue()
		}

		message.addReaction("lori_ok_hand:426183783008698391").queue()
	}

	private fun buildLanguageEmbed(locale: BaseLocale, languages: List<LocaleWrapper>): MessageEmbed {
		val embed = EmbedBuilder()
		embed.setColor(Color(0, 193, 223))

		embed.setTitle("\uD83C\uDF0E " + locale["commands.miscellaneous.language.pleaseSelectYourLanguage"])

		for (wrapper in languages) {
			val translators = wrapper.locale.getWithType<List<String>>("loritta.translationAuthors").mapNotNull { lorittaShards.getUserById(it) }

			embed.addField(
					wrapper.emoteName + " " + wrapper.name,
					"**${locale["commands.miscellaneous.language.translatedBy"]}:** ${translators.joinToString(transform = { "`${it.name}`" })}",
					true
			)
		}

		return embed.build()
	}

	private class LocaleWrapper(
			val name: String,
			val locale: BaseLocale,
			val legacyLocale: LegacyBaseLocale,
			val emoteName: String
	)
}