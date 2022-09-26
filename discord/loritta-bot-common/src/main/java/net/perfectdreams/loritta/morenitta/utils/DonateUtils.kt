package net.perfectdreams.loritta.morenitta.utils

import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.utils.config.EnvironmentType
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.messages.LorittaReply
import java.time.Instant

object DonateUtils {
	/**
	 * Returns a random message (or null) message, used when an user uses a command
	 *
	 * @param locale      the base locale
	 * @param donatorPaid how much the user donated
	 * @return A reply, may be null
	 */
	fun getRandomDonationMessage(locale: BaseLocale, profile: Profile, donatorPaid: Double, guildPaid: Double): LorittaReply? {
		val willRestartAt = loritta.patchData.willRestartAt
		if (willRestartAt != null) {
			val instant = Instant.ofEpochMilli(willRestartAt).atZone(Constants.LORITTA_TIMEZONE)
			val estimatedTime = lorittaShards.shardManager.shards.size * 8_000L
			val fancyFormatted = net.perfectdreams.loritta.morenitta.utils.DateUtils.formatMillis(estimatedTime, loritta.localeManager.getLocaleById(locale.id))

			return LorittaReply(
                    locale[
                            "commands.restartEnabled",
                            "${instant.hour.toString().padStart(2, '0')}:${instant.minute.toString().padStart(2, '0')}",
                            fancyFormatted, // estimative
                            Emotes.LORI_CRYING
                    ],
                    "\uD83D\uDEAB"
            )
		}

		val patchNotes = loritta.patchData.patchNotes

		if (patchNotes != null) {
			val blogPostId = patchNotes.blogPostId
			val expiresAt = patchNotes.expiresAt
			val receivedAt = patchNotes.receivedAt

			if (expiresAt >= System.currentTimeMillis() && receivedAt >= (profile.lastCommandSentAt ?: 0)) {
				return LorittaReply(
                        locale["commands.checkOutPatchNotes", "${loritta.instanceConfig.loritta.website.url}${locale["website.localePath"]}/blog/${blogPostId}?utm_source=discord&utm_medium=link&utm_campaign=update_cmd"],
                        Emotes.LORI_WOW
                )
			}
		}

		if (loritta.config.loritta.environment == EnvironmentType.CANARY) {
			return LorittaReply(
                    locale["commands.canaryInstanceDoNotUse"],
                    Emotes.DO_NOT_DISTURB
            )
		}

		return null
	}
}