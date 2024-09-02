package net.perfectdreams.loritta.morenitta.utils

import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.EnvironmentType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.messages.LorittaReply

object DonateUtils {
	/**
	 * Returns a random message (or null) message, used when an user uses a command
	 *
	 * @param locale      the base locale
	 * @param donatorPaid how much the user donated
	 * @return A reply, may be null
	 */
	fun getRandomDonationMessage(loritta: LorittaBot, locale: BaseLocale, profile: Profile): LorittaReply? {
		if (loritta.config.loritta.environment == EnvironmentType.CANARY) {
			return LorittaReply(
                    locale["commands.canaryInstanceDoNotUse"],
                    Emotes.DO_NOT_DISTURB
            )
		}

		return null
	}
}