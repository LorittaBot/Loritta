package net.perfectdreams.loritta.legacy.commands.vanilla.misc

import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.utils.loritta
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils

class AjudaCommand : AbstractCommand("ajuda", listOf("help", "comandos", "commands"), net.perfectdreams.loritta.common.commands.CommandCategory.MISC) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.help.description")

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "help")

		val embed = EmbedBuilder()
				.setTitle("${Emotes.LORI_HEART} ${context.locale["commands.command.help.lorittaHelp"]}")
				.setDescription(context.locale.getList("commands.command.help.intro").joinToString("\n\n", transform = { it.replace("{0}", context.asMention) }))
				.addField(
						"${Emotes.LORI_PAT} ${context.locale["commands.command.help.commandList"]}",
						"${loritta.instanceConfig.loritta.website.url}commands",
						false
				)
				.addField(
						"${Emotes.LORI_HM} ${context.locale["commands.command.help.supportServer"]}",
						"${loritta.instanceConfig.loritta.website.url}support",
						false
				)
				.addField(
						"${Emotes.LORI_YAY} ${context.locale["commands.command.help.addMe"]}",
						"${loritta.instanceConfig.loritta.website.url}dashboard",
						false
				)
				.addField(
						"${Emotes.LORI_RICH} ${context.locale["commands.command.help.donate"]}",
						"${loritta.instanceConfig.loritta.website.url}donate",
						false
				)
				.addField(
						"${Emotes.LORI_TEMMIE} ${context.locale["commands.command.help.blog"]}",
						"${loritta.instanceConfig.loritta.website.url}blog",
						false
				)
				.addField(
						"${Emotes.LORI_RAGE} ${context.locale["commands.command.help.guidelines"]}",
						"${loritta.instanceConfig.loritta.website.url}guidelines",
						false
				)
				.setThumbnail("https://loritta.website/assets/img/lori_help_short.png")
				.setColor(Constants.LORITTA_AQUA)

		context.sendMessage(embed.build())
	}
}