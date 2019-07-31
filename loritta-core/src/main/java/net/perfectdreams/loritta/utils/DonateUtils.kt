package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

object DonateUtils {
	/**
	 * Returns a random message (or null) message, used when an user uses a command
	 *
	 * @param locale      the base locale
	 * @param donatorPaid how much the user donated
	 * @return A reply, may be null
	 */
	fun getRandomDonationMessage(locale: BaseLocale, donatorPaid: Double): LoriReply? {
		val randomValue = Loritta.RANDOM.nextInt(0, 100)

		return if (randomValue in 0..3) { // Upvote
			LoriReply(
					locale["commands.ifYouLikeMyFeaturesAndWantToHelp", locale["commands.pleaseUpvote", "<https://discordbots.org/bot/loritta/vote>"]],
					"\uD83D\uDE0A"
			)
		} else if (randomValue in 4..7 && (39.99 > donatorPaid)) { // Doar
			LoriReply(
					locale["commands.ifYouLikeMyFeaturesAndWantToHelp", locale["commands.pleaseDonate", "<${com.mrpowergamerbr.loritta.utils.loritta.config.loritta.website.url}donate>"]],
					Emotes.LORI_OWO
			)
		} else if (randomValue in 8..11) { // Fortnite Code
			LoriReply(
					locale["commands.ifYouLikeMyFeaturesAndWantToHelp", locale["commands.pleaseUseFortniteCreatorCode", com.mrpowergamerbr.loritta.utils.loritta.config.fortniteApi.creatorCode]],
					Emotes.LORI_OWO
			)
		} else null
	}
}