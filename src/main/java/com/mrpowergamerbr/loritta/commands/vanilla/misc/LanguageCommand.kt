package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.f
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import java.awt.Color

class LanguageCommand : CommandBase() {
	override fun getLabel(): String {
		return "language"
	}

	override fun getAliases(): List<String> {
		return listOf("linguagem", "speak")
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.LANGUAGE_DESCRIPTION.f()
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_SERVER)
	}

	override fun run(context: CommandContext) {
		val embed = EmbedBuilder()
		embed.setColor(Color(0, 193, 223))

		val validLanguages = "\uD83C\uDDE7\uD83C\uDDF7 Português-Brasil\n<:loritta_quebrada:338679008210190336> Português-Funk\n\uD83C\uDDF5\uD83C\uDDF9 Português-Portugal\n\uD83C\uDDFA\uD83C\uDDF8 English-US"
		embed.setDescription(context.locale.LANGUAGE_INFO.f(validLanguages))
		val message = context.sendMessage(context.getAsMention(true), embed.build())
		message.addReaction("\uD83C\uDDE7\uD83C\uDDF7").complete()
		message.addReaction("loritta_quebrada:338679008210190336").complete()
		message.addReaction("\uD83C\uDDF5\uD83C\uDDF9").complete()
		message.addReaction("\uD83C\uDDFA\uD83C\uDDF8").complete()
	}

	override fun onCommandReactionFeedback(context: CommandContext, e: GenericMessageReactionEvent, msg: Message) {
		if (context.userHandle.id == e.user.id) { // Somente quem executou o comando pode utilizar!
			var localeId = "default"
			if (e.reactionEmote.name == "loritta_quebrada") {
				localeId = "pt-funk"
			}
			if (e.reactionEmote.name == "\uD83C\uDDFA\uD83C\uDDF8") {
				localeId = "en-us"
			}
			if (e.reactionEmote.name == "\uD83C\uDDF5\uD83C\uDDF9") {
				localeId = "pt-pt"
			}

			context.config.localeId = localeId
			loritta save context.config
			val newLocale = loritta.getLocaleById(localeId)
			if (localeId == "default") {
				localeId = "pt-br" // Já que nós já salvamos, vamos trocar o localeId para algo mais "decente"
			}
			context.sendMessage("\uD83C\uDFA4 **|** ${newLocale.get("LANGUAGE_USING_LOCALE", localeId)}")
			msg.delete().complete()
		}
	}
}