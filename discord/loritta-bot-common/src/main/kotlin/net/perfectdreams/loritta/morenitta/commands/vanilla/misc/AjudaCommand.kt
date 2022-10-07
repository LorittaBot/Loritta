package net.perfectdreams.loritta.morenitta.commands.vanilla.misc

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.deviousfun.EmbedBuilder
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.morenitta.LorittaBot

class AjudaCommand(loritta: LorittaBot) : AbstractCommand(loritta, "ajuda", listOf("help", "comandos", "commands"), net.perfectdreams.loritta.common.commands.CommandCategory.MISC) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.help.description")

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "help")

		val embed = EmbedBuilder()
				.setTitle("${Emotes.LORI_HEART} ${context.locale["commands.command.help.lorittaHelp"]}")
				.setDescription(context.locale.getList("commands.command.help.intro").joinToString("\n\n", transform = { it.replace("{0}", context.asMention) }))
				.addField(
						"${Emotes.LORI_PAT} ${context.locale["commands.command.help.commandList"]}",
						"${loritta.config.loritta.website.url}commands",
						false
				)
				.addField(
						"${Emotes.LORI_HM} ${context.locale["commands.command.help.supportServer"]}",
						"${loritta.config.loritta.website.url}support",
						false
				)
				.addField(
						"${Emotes.LORI_YAY} ${context.locale["commands.command.help.addMe"]}",
						"${loritta.config.loritta.website.url}dashboard",
						false
				)
				.addField(
						"${Emotes.LORI_RICH} ${context.locale["commands.command.help.donate"]}",
						"${loritta.config.loritta.website.url}donate",
						false
				)
				.addField(
						"${Emotes.LORI_TEMMIE} ${context.locale["commands.command.help.blog"]}",
						"${loritta.config.loritta.website.url}blog",
						false
				)
				.addField(
						"${Emotes.LORI_RAGE} ${context.locale["commands.command.help.guidelines"]}",
						"${loritta.config.loritta.website.url}guidelines",
						false
				)
				.setThumbnail("https://loritta.website/assets/img/lori_help_short.png")
				.setColor(Constants.LORITTA_AQUA)

		context.sendMessage(embed.build())
	}
}