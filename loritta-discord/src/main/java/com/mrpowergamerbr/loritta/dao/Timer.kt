package com.mrpowergamerbr.loritta.dao

import com.github.salomonbrys.kotson.fromJson
import com.mrpowergamerbr.loritta.tables.Timers
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import mu.KotlinLogging
import net.perfectdreams.loritta.utils.CalendarUtils
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

class Timer(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<Timer>(Timers) {
		private val logger = KotlinLogging.logger {}
	}

	var guildId by Timers.guildId
	var channelId by Timers.channelId
	var startsAt by Timers.startsAt
	var repeatCount by Timers.repeatCount
	var repeatDelay by Timers.repeatDelay
	var activeOnDays by Timers.activeOnDays
	var effects by Timers.effects

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
		if (!coroutineContext.isActive)
			return
		logger.info("prepareTimer() de ${id.value} $coroutineContext...")

		var simulatedTime = startsAt

		var i = 0
		val compare = if (repeatCount == null) {
			{ true }
		} else { { repeatCount!! > i } }

		while (compare.invoke()) {
			// println("${System.currentTimeMillis()} / $simulatedTime")

			val relativeTimeNow = System.currentTimeMillis() - getStartOfDay()

			if (simulatedTime > relativeTimeNow) {
				logger.info("$i - uwu!!! (Será executado daqui ${simulatedTime - relativeTimeNow}ms!)")

				val start = System.currentTimeMillis()
				delay(simulatedTime - relativeTimeNow)

				println(System.currentTimeMillis() - start)

				try {
					execute()
				} catch (e: Exception) {
					logger.error(e) { "Erro ao executar timer ${id.value} no servidor $guildId"}
				}
				prepareTimer()
				return
			} else {
				// logger("$i - Passado...")
			}
			simulatedTime += repeatDelay
			i++
		}
	}

	suspend fun execute() {
		if (!coroutineContext.isActive)
			return

		logger.info("Timer $id ativado!!")

		val guild = lorittaShards.getGuildById(guildId) ?: return
		val textChannel = guild.getTextChannelById(channelId) ?: return

		val _effect = effects.random()

		val effect = gson.fromJson<TimerEffect>(_effect)
		when (effect.type) {
			TimerEffect.TimerEffectType.TEXT -> { // Texto
				// Mensagens são salvas em um "TimerEffectText", dentro do "effect.contents"
				val content = effect.contents.random()

				val (deleteAfter, message) = gson.fromJson<TimerEffect.TimerEffectText>(content)

				// Iremos processar igual como o "+say" funciona
				val discordMessage = try {
					MessageUtils.generateMessage(
							message,
							listOf(guild),
							guild
					)
				} catch (e: Exception) {
					null
				}

				val action = if (discordMessage != null)
					textChannel.sendMessage(discordMessage)
				else
					textChannel.sendMessage(message)

				logger.info("Mensagem $message será enviada em ${guild.id}... Será deletada daqui a $deleteAfter ms!")

				action.queue {
					logger.info("Mensagem enviada! Ela será deletada daqui a $deleteAfter millisegundos...")
					if (deleteAfter != null)
						it.delete().queueAfter(deleteAfter, TimeUnit.MILLISECONDS)
				}
			}
			TimerEffect.TimerEffectType.COMMAND -> {
				// Comandos são salvos em um TimerEffectCommand, o formato é salvo dentro do "effect.contents" em JSON
				val content = effect.contents.random()
				val (clazzName, arguments) = gson.fromJson<TimerEffect.TimerEffectCommand>(content)
				TODO()
			}
			TimerEffect.TimerEffectType.JAVASCRIPT -> {
				// Usa a API de comandos da Lori
				// o formato é apenas o código em JavaScript jogado dentro do "effect.contents"
				TODO()
			}
		}
	}

	fun getStartOfDay(): Long {
		val calendar = CalendarUtils.resetToBeginningOfTheDay(Calendar.getInstance())
		return calendar.timeInMillis
	}

	class TimerEffect(val type: TimerEffectType, val contents: List<String>) {
		enum class TimerEffectType {
			TEXT,
			COMMAND,
			JAVASCRIPT
		}

		data class TimerEffectText(val deleteAfter: Long?, val message: String)
		
		data class TimerEffectCommand(val clazzName: String, val arguments: String)
	}
}