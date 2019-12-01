package net.perfectdreams.spicymorenitta.views.dashboard

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import kotlinx.html.stream.createHTML
import kotlinx.serialization.*
import kotlinx.serialization.json.JSON
import net.perfectdreams.spicymorenitta.utils.*
import org.w3c.dom.*
import org.w3c.dom.events.Event
import kotlin.browser.document
import kotlin.collections.set
import kotlin.js.Date

@ImplicitReflectionSerializer
object TimersView {
	var loadedTimers = mutableListOf<Timer>()
	val timerEntries by lazy {
		page.getElementById("timer-entries")
	}
	var currentIndex = -1

	@JsName("start")
	fun start() {
		document.addEventListener("DOMContentLoaded", {
			val timerAsJson = document.getElementById("timers-as-json")?.innerHTML!!

			println("timerAsJson: " + timerAsJson)

			val timers = JSON.parseList<Timer>(timerAsJson)
			println("Timers: ")
			timers.forEach {
				println(it.timerId)
				println(it.guildId)
				println(it.channelId)
				println(it.startsAt)
				println(it.repeatDelay)
				println(it.effects)
				println("---")
			}

			// Vamos guardar os timers atuais em uma lista
			loadedTimers = timers.toMutableList()

			// E depois adicionar a entry deles!
			loadedTimers.forEach {
				addTimerEntry(it)
			}

			(page.getElementByClass("add-timer-button") as HTMLDivElement).onclick = {
				println("Clicked! ${JSON.stringify(loadedTimers)}")
				val timer = Timer(
						(currentIndex--).toString(),
						"297732013006389252",
						"297732013006389252",
						"0",
						"60000",
						arrayOf(
								JSON.stringify(
										Timer.TimerEffect(
												Timer.TimerEffect.TimerEffectType.TEXT,
												listOf(
														JSON.stringify(Timer.TimerEffect.TimerEffectText(20000, "Stay awesome! :3"))
												)
										)
								)
						)
				)

				loadedTimers.add(timer)
				addTimerEntry(timer)
				editTimer(timer)
			}

			(page.getElementByClass("timers-save-button") as HTMLDivElement).onclick = {
				println("Clicked! ${JSON.stringify(loadedTimers)}")
				SaveUtils.prepareSave("timers", {
					it["timers"] = kotlin.js.JSON.parse(JSON.stringify(loadedTimers))
				})
			}

			val entries = document.getElementsByClassName("timer-entry")

			entries.asList().forEach { entry ->
				println("Setting up $it...")
				val timerId = entry.getAttribute("timer-id")!!
				println("Timer ID: $timerId")

				val editTimerButton = entry.getElementsByClassName("edit-timer-button")[0] as HTMLButtonElement
				editTimerButton.onclick = {
					println("Editando timer ${timerId.toLong()}...")
					println("Timers existentes:")
					loadedTimers.forEach {
						println("Timer ${it.timerId}")
					}

					editTimer(loadedTimers.first { it.timerId == timerId })
				}

				val deleteTimerButton = entry.getElementsByClassName("delete-timer-button")[0] as HTMLButtonElement
				deleteTimerButton.onclick = {
					loadedTimers.remove(loadedTimers.first { it.timerId == timerId })
					entry.remove()
				}
			}
		})
	}

	fun addTimerEntry(timer: Timer) {
		println("Generating entry for ${timer.timerId}...")
		val builder = StringBuilder().appendHTML(false).div(classes = "discord-generic-entry timer-entry") {
			attributes["timer-id"] = timer.timerId.toString()
			img(classes = "amino-small-image") {
				style = "width: 6%; height: auto; border-radius: 999999px; float: left;"
				src = "https://cdn.discordapp.com/avatars/418340363946819604/a_5229c09f5e33c95066618840960bf525.gif?size=2048"
			}
			div(classes = "pure-g") {
				div(classes = "pure-u-1 pure-u-md-18-24") {
					div {
						style = "margin-left: 10px; margin-right: 10;"
						div(classes = "amino-title entry-title") {
							style = "font-family: Whitney,Helvetica Neue,Helvetica,Arial,sans-serif;"
							+ "Timer ${timer.timerId.toString()}"
						}
						div(classes = "amino-title toggleSubText") {
							+ "Alguma coisa interessante aqui"
						}
					}
				}
				div(classes = "pure-u-1 pure-u-md-6-24 vertically-centered-content") {
					button(classes="button-discord button-discord-edit pure-button delete-timer-button") {
						style = "margin-right: 8px; min-width: 0px;"
						i(classes = "fas fa-trash") {

						}
					}
					button(classes="button-discord button-discord-edit pure-button edit-timer-button") {
						+ "Editar"
					}
				}
			}
		}
		println("Prepending...")
		timerEntries.appendBuilder(builder)
		println("Done!")
	}

	@JsName("editTimer")
	fun editTimer(timer: Timer) {
		println("Editing $timer...")

		val rawEffect = timer.effects.first()

		val effect = JSON.nonstrict.parse<Timer.TimerEffect>(rawEffect)
		val textEffect = JSON.nonstrict.parse<Timer.TimerEffect.TimerEffectText>(effect.contents.first())

		val modal = TingleModal(
				TingleOptions(
						footer = true,
						cssClass = arrayOf("tingle-modal--overflow")
				)
		)

		modal.addFooterBtn("<i class=\"fas fa-times\"></i> Salvar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
			// Iremos salvar o timer atual, aplicando as mudanças realizadas
			timer.channelId = visibleModal.getElementsByClassName("channel-id")[0]!!.asDynamic().value
			timer.startsAt = visibleModal.getElementsByClassName("starts-at")[0]!!.asDynamic().value
			timer.repeatDelay = visibleModal.getElementsByClassName("repeat-delay")[0]!!.asDynamic().value
			textEffect.deleteAfter = visibleModal.getElementsByClassName("delay-to-delete")[0]!!.asDynamic().value
			val message = visibleModal.getElementsByClassName("message")[0]!!.asDynamic().value

			println("Timer ${timer.timerId} foi editado! :3")
			println("Channel ID: ${timer.channelId}")
			println("startsAt: ${timer.startsAt}")
			println("repeatDelay: ${timer.repeatDelay}")
			println("message: $message")
			textEffect.message = message
			textEffect.message = message
			effect.contents = listOf(JSON.stringify(textEffect))
			timer.effects = arrayOf(JSON.stringify(effect))

			modal.close()
		}

		modal.setContent(
				createHTML().div {
					div(classes = "category-name") {
						+ "Timer ${timer.timerId}"
					}
					// Timers suportam vários "efeitos" diferentes (randômicos!)
					// Mas por enquanto iremos implementar apenas o padrão TEXT e apenas um EFFECT
					input(classes = "channel-id") {
						attributes["value"] = timer.channelId.toString()
					}
					textArea(classes = "message") {
						+ textEffect.message
					}
					numberInput(classes = "delay-to-delete") {
						attributes["value"] = textEffect.deleteAfter?.toString() ?: "0"
					}
					numberInput(classes = "starts-at") {
						attributes["value"] = timer.startsAt.toString()
					}
					numberInput(classes = "repeat-delay") {
						attributes["value"] = timer.repeatDelay.toString()
					}
					div(classes = "time-preview") {
						+ "Aqui irá ficar a preview de quando será a próxima mensagem"
					}
				}
		)
		modal.open()

		val startsAtInput = (visibleModal.getElementsByClassName("starts-at")[0]!! as HTMLInputElement)
		val repeatDelayInput = (visibleModal.getElementsByClassName("repeat-delay")[0]!! as HTMLInputElement)
		val timePreview = (visibleModal.getElementsByClassName("time-preview")[0]!! as HTMLDivElement)

		fun getStartOfDay(): Double {
			val date = Date()
			date.asDynamic().setUTCHours(0, 0, 0, 0)
			return date.getTime()
		}

		fun showTimeSimulation() {
			println("Start of day is ${getStartOfDay()}")
			var simulatedTime = timer.startsAt.toLong()

			var i = 0
			val compare = /* if (repeatCount == null) {
				{ true }
			} else { { repeatCount!! > i } } */ { true }

			while (compare.invoke()) {
				// println("${System.currentTimeMillis()} / $simulatedTime")

				val relativeTimeNow = (Date().getTime() - getStartOfDay()).toLong()

				if (simulatedTime > relativeTimeNow) {
					println("$i - uwu!!! (Será executado daqui ${simulatedTime - relativeTimeNow}ms!)")
					println("s+r: " + getStartOfDay() + relativeTimeNow)
					println("s+st: " + getStartOfDay() + simulatedTime)

					val stuff = (Date().getTime() + (simulatedTime - relativeTimeNow))
					println("Relativo ++: $stuff")
					timePreview.innerHTML = "Se deus quiser, daqui a " +  DateUtils.formatDateDiff(Date().getTime(), stuff) + " a mensagem será enviada ;w; :3"
					/* val start = System.currentTimeMillis()
					delay(simulatedTime - relativeTimeNow)

					println(System.currentTimeMillis() - start)

					try {
						execute()
					} catch (e: Exception) {
						logger.error(e) { "Erro ao executar timer ${id.value} no servidor $guildId"}
					}
					prepareTimer() */
					return
				} else {
					println("Skip...")
					// logger("$i - Passado...")
				}
				simulatedTime += repeatDelayInput.value.toLong()
				i++
			}
		}

		val callback: ((Event) -> dynamic) = {
			println("onchange callback!")
			showTimeSimulation()
		}

		val currentModal = visibleModal
		GlobalScope.launch {
			while (true) {
				if (!currentModal.classList.contains("tingle-modal--visible"))
					return@launch

				// return@launch

				showTimeSimulation()
				delay(1000)
			}
		}

		startsAtInput.onchange = callback
		repeatDelayInput.onchange = callback
	}

	@Serializable
	class Timer(
			// É deserializado para String pois JavaScript é burro e não funciona direito com Longs
			val timerId: String,
			val guildId: String,
			var channelId: String,
			var startsAt: String,
			var repeatDelay: String,
			var effects: Array<String>
	) {
		@Serializable
		data class TimerEffect(val type: TimerEffectType, var contents: List<String>) {
			enum class TimerEffectType {
				TEXT,
				COMMAND,
				JAVASCRIPT
			}

			@Serializable
			data class TimerEffectText(@Optional var deleteAfter: Long? = null, var message: String)

			@Serializable
			data class TimerEffectCommand(val clazzName: String, val arguments: String)
		}
	}
}