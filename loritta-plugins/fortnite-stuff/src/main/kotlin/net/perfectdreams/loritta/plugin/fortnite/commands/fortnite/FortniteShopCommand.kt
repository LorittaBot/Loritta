package net.perfectdreams.loritta.plugin.fortnite.commands.fortnite

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.plugin.fortnite.FortniteStuff

class FortniteShopCommand(val m: FortniteStuff) : LorittaDiscordCommand(arrayOf("fortniteshop", "fortniteloja", "fnshop", "fnloja"), CommandCategory.FORTNITE) {
	override val needsToUploadFiles: Boolean
		get() = true

	override fun getDescription(locale: BaseLocale) = locale["commands.fortnite.shop.description"]

	@Subcommand
	suspend fun root(context: DiscordCommandContext, locale: BaseLocale) {
		var storeImage: ByteArray? = null

		if (loritta.config.isOwner(context.userHandle.idLong) && context.args.getOrNull(0) == "force_resend") {
			context.reply(
					"Enviando imagem da loja do Fortnite em todos os servidores que possuem a funcionalidade ativada..."
			)

			// m.updateStoreItems?.broadcastNewFortniteShopItems()
			return
		}

		if (m.updateStoreItems?.storeImages?.containsKey(locale.id) == true)
			storeImage = m.updateStoreItems!!.storeImages[locale.id]
		else if (m.updateStoreItems?.storeImages?.containsKey(Constants.DEFAULT_LOCALE_ID) == true)
			storeImage = m.updateStoreItems!!.storeImages[Constants.DEFAULT_LOCALE_ID]

		if (storeImage == null) {
			context.reply(
					LoriReply(
							locale["commands.fortnite.shop.notLoadedYet"],
							Constants.ERROR
					)
			)
			return
		}

		context.sendFile(
				storeImage.inputStream(),
				"fortnite-shop.png",
				context.getAsMention(true)
		)
	}
}