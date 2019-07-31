package net.perfectdreams.loritta.plugin.fortnite.commands.fortnite

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommandContext
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.plugin.fortnite.FortniteStuff

class FortniteShopCommand(val m: FortniteStuff) : LorittaDiscordCommand(arrayOf("fortniteshop", "fortniteloja"), CommandCategory.FORTNITE) {
	override val needsToUploadFiles: Boolean
		get() = true

	override fun getDescription(locale: BaseLocale) = locale["commands.fortnite.shop.description"]

	@Subcommand
	suspend fun root(context: LorittaCommandContext, locale: BaseLocale) {
		val inputStream = m.updateStoreItems?.storeImage?.inputStream() ?: run {
			context.reply(
					LoriReply(
							locale["commands.fortnite.shop.notLoadedYet"],
							Constants.ERROR
					)
			)
			return
		}


		context.sendFile(
				inputStream,
				"fortnite-shop.png",
				context.getAsMention(true)
		)
	}
}