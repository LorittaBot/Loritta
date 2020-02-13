package net.perfectdreams.loritta.plugin.fortnite.commands.fortnite

import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.plugin.fortnite.FortniteStuff
import net.perfectdreams.loritta.plugin.fortnite.commands.fortnite.base.DSLCommandBase

object FortniteShopCommand : DSLCommandBase {
	private val LOCALE_PREFIX = "commands.fortnite.shop"

	override fun command(loritta: LorittaDiscord, m: FortniteStuff) = create(loritta, listOf("fortniteshop", "fortniteloja", "fnshop", "fnloja")) {
		description { it["${LOCALE_PREFIX}.description"] }

		executesDiscord {
			var storeImage: ByteArray? = null

			if (m.updateStoreItems?.storeImages?.containsKey(locale.id) == true)
				storeImage = m.updateStoreItems!!.storeImages[locale.id]
			else if (m.updateStoreItems?.storeImages?.containsKey(Constants.DEFAULT_LOCALE_ID) == true)
				storeImage = m.updateStoreItems!!.storeImages[Constants.DEFAULT_LOCALE_ID]

			if (storeImage == null) {
				reply(
						LorittaReply(
								locale["commands.fortnite.shop.notLoadedYet"],
								Constants.ERROR
						)
				)
				return@executesDiscord
			}

			sendFile(
					storeImage.inputStream(),
					"fortnite-shop.png"
			)
		}
	}
}