package net.perfectdreams.loritta.plugin.lorittabirthday2020

import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.network.Databases
import kotlinx.coroutines.channels.Channel
import net.perfectdreams.loritta.plugin.lorittabirthday2020.tables.CollectedBirthday2020Points
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap

object LorittaBirthday2020 {
	val pantufaRewards = listOf(
			BackgroundReward(100, "birthday2020TeamPantufa"),
			SonhosReward(200, 7_000),
			BackgroundReward(300, "birthday2020Brabas"),
			SonhosReward(400, 7_000),
			BackgroundReward(500, "birthday2020PantufaHugoo"),
			SonhosReward(600, 7_000),
			BackgroundReward(300, "birthday2020Brabas"),
			SonhosReward(800, 7_000),
			BackgroundReward(300, "birthday2020Brabas"),
			SonhosReward(1_000, 7_000),
			BackgroundReward(300, "birthday2020Brabas"),
			SonhosReward(1_200, 7_000),
			BackgroundReward(300, "birthday2020Brabas"),
			SonhosReward(1_400, 7_000)

			)
	val gabrielaRewards = listOf(
			BackgroundReward(100, "birthday2020TeamGabriela"),
			SonhosReward(200, 7_000),
			BackgroundReward(300, "birthday2020Brabas"),
			SonhosReward(600, 7_000),
			BackgroundReward(300, "birthday2020Brabas"),
			SonhosReward(800, 7_000),
			BackgroundReward(300, "birthday2020Brabas"),
			SonhosReward(1_000, 7_000),
			BackgroundReward(300, "birthday2020Brabas"),
			SonhosReward(1_200, 7_000),
			BackgroundReward(300, "birthday2020Brabas"),
			SonhosReward(1_400, 7_000)
	)

	val openChannels = ConcurrentHashMap<Long, Channel<JsonObject>>()

	val emojis = listOf(
			"lori_gift:653402818199158805",
			"green_gift:659069659160772647",
			"pink_gift:659069658833354773"
	)

	fun isEventActive(): Boolean {
		// val calendar = Calendar.getInstance()
		// return calendar.get(Calendar.YEAR) == 2020
		val endOfEvent = LocalDateTime.of(2020, 3, 30, 17, 0)
				.atZone(ZoneId.of("America/Sao_Paulo"))
		val now = Instant.now().atZone(ZoneId.of("America/Sao_Paulo"))

		return now.isBefore(endOfEvent)
	}

	fun sendPresentCount(m: LorittaBirthday2020Event, id: Long, type: String = "collectedPoint") {
		val channel = openChannels[id] ?: return

		val points = transaction(Databases.loritta) {
			CollectedBirthday2020Points.select {
				CollectedBirthday2020Points.user eq id
			}.count()
		}

		m.launch {
			channel.send(
					jsonObject(
							"type" to type,
							"total" to points
					)
			)
		}
	}

	open class Reward(val requiredPoints: Int)
	class BackgroundReward(requiredPoints: Int, val internalName: String) : Reward(requiredPoints)
	class SonhosReward(requiredPoints: Int, val sonhosReward: Int) : Reward(requiredPoints)
}