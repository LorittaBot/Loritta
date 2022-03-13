package net.perfectdreams.loritta.plugin.donatorsostentation

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import java.time.LocalDateTime
import java.time.ZonedDateTime

object PremiumSlotsUtils {
	private val logger = KotlinLogging.logger {}
	private const val PREMIUM_SLOTS_CHANNEL_ID = 615568692075298827L
	private const val ANNOUNCE_PREMIUM_SLOTS_OPEN_CHANNEL_ID = 358774895850815488L
	private const val SPONSOR_QUANTITY = 200

	internal fun createPremiumSlotsAdvertisementTask(config: DonatorsOstentationConfig): suspend CoroutineScope.() -> Unit = {
		while (true) {
			val now = ZonedDateTime.now(Constants.LORITTA_TIMEZONE)

			var whenShouldBeAnnounced = LocalDateTime.of(
					now.year,
					now.month,
					now.month.length(false) - 7,
					17,
					0,
					0
			).atZone(Constants.LORITTA_TIMEZONE)

			// Verificar que, se já foi o deste mês, apenas anuncie mês que vem
			if (whenShouldBeAnnounced.isBefore(now))
				whenShouldBeAnnounced = whenShouldBeAnnounced.plusMonths(1L)

			val diff = whenShouldBeAnnounced.toInstant().toEpochMilli() - now.toInstant().toEpochMilli()

			logger.info { "Premium Slots announcement stuff will be done in ${diff}ms" }
			delay(diff)
			logger.info { "Automatically announcing premium slots stuff!" }

			announcePremiumSlots()
		}
	}

	internal fun createPremiumSlotsCloseTask(config: DonatorsOstentationConfig): suspend CoroutineScope.() -> Unit = {
		while (true) {
			val now = ZonedDateTime.now(Constants.LORITTA_TIMEZONE)

			var whenShouldBeAnnounced = LocalDateTime.of(
					now.year,
					now.month,
					now.month.length(false),
					23,
					59,
					59
			).atZone(Constants.LORITTA_TIMEZONE)

			// Verificar que, se já foi o deste mês, apenas anuncie mês que vem
			if (whenShouldBeAnnounced.isBefore(now))
				whenShouldBeAnnounced = whenShouldBeAnnounced.plusMonths(1L)

			val diff = whenShouldBeAnnounced.toInstant().toEpochMilli() - now.toInstant().toEpochMilli()

			logger.info { "Premium Slots close stuff will be done in ${diff}ms" }
			delay(diff)
			logger.info { "Automatically closing premium slots stuff!" }

			closePremiumSlots()
		}
	}

	fun announcePremiumSlots() {
		// Vamos anunciar!
		val guild = lorittaShards.getGuildById(297732013006389252L) ?: return

		val premiumSlotsChannel = guild.getTextChannelById(PREMIUM_SLOTS_CHANNEL_ID)!!
		premiumSlotsChannel.upsertPermissionOverride(
				guild.publicRole
		).setAllow(Permission.MESSAGE_WRITE)
				.queue()

		val premiumSlotsAnnouncementChannel = guild.getTextChannelById(ANNOUNCE_PREMIUM_SLOTS_OPEN_CHANNEL_ID)!!
		premiumSlotsAnnouncementChannel.sendMessage("Premium Slots está aberto!").queue()
	}

	fun closePremiumSlots() {
		val guild = lorittaShards.getGuildById(297732013006389252L) ?: return

		val premiumSlotsChannel = guild.getTextChannelById(PREMIUM_SLOTS_CHANNEL_ID)!!
		premiumSlotsChannel.upsertPermissionOverride(
				guild.publicRole
		).setDeny(Permission.MESSAGE_WRITE)
				.queue()
	}
}