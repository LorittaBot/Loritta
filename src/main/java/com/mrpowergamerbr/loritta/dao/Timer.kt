package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.tables.Timers
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.delay
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import java.util.*

class Timer(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<Timer>(Timers)

	var guildId by Timers.guildId
	var channelId by Timers.channelId
	var startsAt by Timers.startsAt
	var repeatCount by Timers.repeatCount
	var repeatDelay by Timers.repeatDelay
	var activeOnDays by Timers.activeOnDays
	var commands by Timers.commands

	fun calculateRepetitions() {
		val repeat = repeatCount ?: 10

		var first = startsAt

		repeat(repeat) {
			print("$it às $first")
			val absoluteTime = first + getStartOfDay()
			if (System.currentTimeMillis() > absoluteTime) {
				print(" (Passado! ${System.currentTimeMillis()}/$absoluteTime)")
			} else {
				print(" (Será executado daqui ${absoluteTime - System.currentTimeMillis()}ms!)")
			}
			print("\n")
			first += repeatDelay
		}
	}

	suspend fun prepareTimer() {
		println("prepareTimer()")
		var simulatedTime = startsAt

		var i = 0
		val compare = if (repeatCount == null) {
			{ true }
		} else { { repeatCount!! > i } }

		while (compare.invoke()) {
			println("${System.currentTimeMillis()} / $simulatedTime")

			val relativeTimeNow = System.currentTimeMillis() - getStartOfDay()

			if (simulatedTime > relativeTimeNow) {
				println("$i - uwu!!! (Será executado daqui ${simulatedTime - relativeTimeNow}ms!)")

				val start = System.currentTimeMillis()
				delay(simulatedTime - relativeTimeNow)

				println(System.currentTimeMillis() - start)

				execute()
				prepareTimer()
				return
			} else {
				println("$i - Passado...")
			}
			simulatedTime += repeatDelay
			i++
		}
	}

	fun execute() {
		println("Triggered timer $id!")

		val guild = lorittaShards.getGuildById(guildId) ?: return
		val textChannel = guild.getTextChannelById(channelId) ?: return

		val command = commands.random()
		textChannel.sendMessage(command).queue()
	}

	fun getStartOfDay(): Long {
		val calendar = Calendar.getInstance()
		calendar.set(Calendar.HOUR_OF_DAY, 0)
		calendar.set(Calendar.MINUTE, 0)
		calendar.set(Calendar.SECOND, 0)
		calendar.set(Calendar.MILLISECOND, 0)

		return calendar.timeInMillis
	}
}