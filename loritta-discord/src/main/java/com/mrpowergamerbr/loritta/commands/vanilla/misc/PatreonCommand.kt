package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.Emotes
import java.awt.Color

class PatreonCommand : AbstractCommand("donator", listOf("donators", "patreons", "patreon", "doadores", "doador", "apoiador", "apoiadores", "contribuidores", "contribuidor", "doar", "donate"), category = CommandCategory.MISC) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["PATREON_DESCRIPTION"]
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val patrons = "Veja todos os doadores em https://loritta.website/donate (tem tantos doadores que não cabe nesta mensagem! ${Emotes.LORI_CRYING})"

		val embed = EmbedBuilder().apply {
			setThumbnail("https://loritta.website/assets/img/fanarts/Loritta_-_Heathecliff.png")
			setTitle("${Emotes.LORI_RICH} ${context.legacyLocale["PATREON_THANKS"]}")
			setColor(Color(0, 193, 223))
			setDescription(patrons)
			addField("\uD83C\uDF80 " + context.legacyLocale["PATREON_DO_YOU_WANNA_HELP"], context.locale["commands.misc.donate.howToHelp", "${loritta.instanceConfig.loritta.website.url}donate", Emotes.LORI_HEART, Emotes.LORI_CRYING, Emotes.LORI_RICH], false)
		}

		context.sendMessage(context.getAsMention(true), embed.build())
	}
}