package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.Emotes

class AjudaCommand : AbstractCommand("ajuda", listOf("help", "comandos", "commands"), CommandCategory.MISC) {
	override fun getDescriptionKey() = LocaleKeyData("commands.misc.help.description")

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		val embed = EmbedBuilder()
				.setTitle("${Emotes.LORI_HEART} ${context.locale["commands.misc.help.lorittaHelp"]}")
				.setDescription(context.locale.getList("commands.misc.help.intro").joinToString("\n\n", transform = { it.replace("{0}", context.asMention) }))
				.addField(
						"${Emotes.LORI_PAT} ${context.locale["commands.misc.help.commandList"]}",
						"${loritta.instanceConfig.loritta.website.url}commands",
						false
				)
				.addField(
						"${Emotes.LORI_HM} ${context.locale["commands.misc.help.supportServer"]}",
						"${loritta.instanceConfig.loritta.website.url}support",
						false
				)
				.addField(
						"${Emotes.LORI_YAY} ${context.locale["commands.misc.help.addMe"]}",
						"${loritta.instanceConfig.loritta.website.url}dashboard",
						false
				)
				.addField(
						"${Emotes.LORI_RICH} ${context.locale["commands.misc.help.donate"]}",
						"${loritta.instanceConfig.loritta.website.url}donate",
						false
				)
				.addField(
						"${Emotes.LORI_TEMMIE} ${context.locale["commands.misc.help.blog"]}",
						"${loritta.instanceConfig.loritta.website.url}blog",
						false
				)
				.addField(
						"${Emotes.LORI_RAGE} ${context.locale["commands.misc.help.guidelines"]}",
						"${loritta.instanceConfig.loritta.website.url}guidelines",
						false
				)
				.setThumbnail("https://loritta.website/assets/img/lori_help_short.png")
				.setColor(Constants.LORITTA_AQUA)

		context.sendMessage(embed.build())
	}
}