package net.perfectdreams.loritta.plugin.fortnite.commands.fortnite

import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.fortnite.FortniteStuff
import net.perfectdreams.loritta.utils.Emotes

class FortniteShopCommand(val m: FortniteStuff) : DiscordAbstractCommandBase(m.loritta, listOf("fortniteshop", "fortniteloja", "fnshop", "fnloja"), CommandCategory.FORTNITE) {
	private val LOCALE_PREFIX = "commands.command.fnshop"

	override fun command() = create {
		localizedDescription("${LOCALE_PREFIX}.description")
		needsToUploadFiles = true

		executesDiscord {
			val storeFileName = when {
				m.storeFileNamesByLocaleId.containsKey(locale.id) -> m.storeFileNamesByLocaleId[locale.id]
				m.storeFileNamesByLocaleId.containsKey(Constants.DEFAULT_LOCALE_ID) -> m.storeFileNamesByLocaleId[Constants.DEFAULT_LOCALE_ID]
				else -> null
			}

			if (storeFileName == null) {
				reply(
						LorittaReply(
								locale["commands.command.fnshop.notLoadedYet"],
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