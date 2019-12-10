package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import java.time.Instant
import java.time.ZoneId

object DonateUtils {
	/**
	 * Returns a random message (or null) message, used when an user uses a command
	 *
	 * @param locale      the base locale
	 * @param donatorPaid how much the user donated
	 * @return A reply, may be null
	 */
	fun getRandomDonationMessage(locale: BaseLocale, profile: Profile, donatorPaid: Double, guildPaid: Double): LoriReply? {
		val willRestartAt = loritta.patchData.willRestartAt
		if (willRestartAt != null) {
			val instant = Instant.ofEpochMilli(willRestartAt).atZone(ZoneId.systemDefault())
			val estimatedTime = lorittaShards.shardManager.shards.size * 8_000L
			val fancyFormatted = com.mrpowergamerbr.loritta.utils.DateUtils.formatMillis(estimatedTime, loritta.getLegacyLocaleById(locale.id))

			return LoriReply(
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
				return LoriReply(
						locale["commands.checkOutPatchNotes", "${loritta.instanceConfig.loritta.website.url}${locale["website.localePath"]}/blog/${blogPostId}?utm_source=discord&utm_medium=link&utm_campaign=update_cmd"],
						Emotes.LORI_WOW
				)
			}
		}

		val shouldWeSentARandomMessage = Loritta.RANDOM.nextInt(0, 20)

		if (shouldWeSentARandomMessage == 0 && 39.99 > donatorPaid && 59.99 > guildPaid) {
			val randomValue = Loritta.RANDOM.nextInt(0, 5)

			return if (randomValue == 0) { // Upvote
				LoriReply(
						locale["commands.ifYouLikeMyFeaturesAndWantToHelp", locale["commands.pleaseUpvote", "<https://discordbots.org/bot/loritta/vote>"]],
						"\uD83D\uDE0A"
				)
			} else if (randomValue == 1) { // Doar
				LoriReply(
						locale["commands.ifYouLikeMyFeaturesAndWantToHelp", locale["commands.pleaseDonate", "<${com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.website.url}donate>"]],
						Emotes.LORI_OWO
				)
			} else if (randomValue == 2) { // Fortnite Code
				LoriReply(
						locale["commands.ifYouLikeMyFeaturesAndWantToHelp", locale["commands.pleaseUseFortniteCreatorCode", "`${com.mrpowergamerbr.loritta.utils.loritta.config.fortniteApi.creatorCode}`"]],
						Emotes.DEFAULT_DANCE
				)
			} else if (randomValue == 3 && FeatureFlags.ADVERTISE_SPARKLYPOWER && (locale.id == "default" || locale.id == "pt-funk" || locale.id == "pt-pt" || locale.id == "pt-furry")) { // SparklyPower
				LoriReply(
						locale["commands.checkOutSparklyPower", "Minecraft 1.14.4 Survival", "mc.sparklypower.net"],
						Emotes.MINECRAFT_GRASS
				)
			} else if (randomValue == 4 && FeatureFlags.ADVERTISE_SPONSORS) { // Sponsors
				LoriReply(
						locale["commands.checkOutSponsors", "<${loritta.instanceConfig.loritta.website.url}sponsors>"],
						Emotes.LORI_RICH
				)
			} else { null }
		}

		if (loritta.config.loritta.environment == EnvironmentType.CANARY) {
			return LoriReply(
					locale["commands.canaryInstanceDoNotUse"],
					Emotes.DO_NOT_DISTURB
			)
		}

		return null
	}
}