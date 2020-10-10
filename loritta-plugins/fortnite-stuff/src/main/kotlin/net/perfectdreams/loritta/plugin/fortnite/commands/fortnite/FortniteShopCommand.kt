package net.perfectdreams.loritta.plugin.fortnite.commands.fortnite

import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.plugin.fortnite.FortniteStuff
import net.perfectdreams.loritta.plugin.fortnite.commands.fortnite.base.DSLCommandBase
import net.perfectdreams.loritta.utils.Emotes

object FortniteShopCommand : DSLCommandBase {
	private val LOCALE_PREFIX = "commands.fortnite.shop"

	override fun command(loritta: LorittaDiscord, m: FortniteStuff) = create(loritta, listOf("fortniteshop", "fortniteloja", "fnshop", "fnloja")) {
		localizedDescription("${LOCALE_PREFIX}.description")

		executesDiscord {
			val storeFileName = when {
				m.storeFileNamesByLocaleId.containsKey(locale.id) -> m.storeFileNamesByLocaleId[locale.id]
				m.storeFileNamesByLocaleId.containsKey(Constants.DEFAULT_LOCALE_ID) -> m.storeFileNamesByLocaleId[Constants.DEFAULT_LOCALE_ID]
				else -> null
			}

			if (storeFileName == null) {
				reply(
						LorittaReply(
								locale["commands.fortnite.shop.notLoadedYet"],
								Constants.ERROR
						)
				)
				return@executesDiscord
			}

			reply(
					LorittaReply(
							"${loritta.instanceConfig.loritta.website.url}assets/img/fortnite/shop/$storeFileName",
							Emotes.DEFAULT_DANCE
					)
			)
		}
	}
}