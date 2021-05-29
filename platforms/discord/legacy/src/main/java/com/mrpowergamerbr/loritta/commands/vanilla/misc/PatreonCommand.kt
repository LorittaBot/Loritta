package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.utils.Emotes
import java.awt.Color

class PatreonCommand : AbstractCommand("donator", listOf("donators", "patreons", "patreon", "doadores", "doador", "apoiador", "apoiadores", "contribuidores", "contribuidor", "doar", "donate"), category = CommandCategory.MISC) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.donate.description")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val patrons = "Veja todos os doadores em https://loritta.website/donate (tem tantos doadores que não cabe nesta mensagem! ${Emotes.LORI_CRYING})"

		val embed = EmbedBuilder().apply {
			setThumbnail("https://loritta.website/assets/img/fanarts/Loritta_-_Heathecliff.png")
			setTitle("${Emotes.LORI_RICH} ${context.locale["commands.command.donate.thanks"]}")
			setColor(Color(0, 193, 223))
			setDescription(patrons)
			addField("\uD83C\uDF80 " + context.locale["commands.command.donate.doYouWannaHelp"], context.locale["commands.command.donate.howToHelp", "${loritta.instanceConfig.loritta.website.url}donate", Emotes.LORI_HEART, Emotes.LORI_CRYING, Emotes.LORI_RICH], false)
		}

		context.sendMessage(context.getAsMention(true), embed.build())
	}
}