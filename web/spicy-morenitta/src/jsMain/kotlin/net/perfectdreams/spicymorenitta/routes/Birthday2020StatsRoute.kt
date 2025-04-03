package net.perfectdreams.spicymorenitta.routes

import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.utils.Logging

class Birthday2020StatsRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/birthday-2020/stats"), Logging {
	/* val totalPrizeCount: HTMLElement
		get() = document.select<HTMLDivElement>("#total-prize-count")

	var currentPoints = 0
	var connectedEventSource: EventSource? = null
	val rewardSoundEffect by lazy { Audio("/assets/snd/ts1_promotion2.mp3") }
	val state: HTMLDivElement
		get() = document.select("#state")
	val stuffLog: HTMLDivElement
		get() = document.select("#stuff-log")

	override fun onRender(call: ApplicationCall) {
		super.onRender(call)

		m.launch {
			val teamStatsJob = m.async {
				val result = http.get<String> {
					url("${window.location.origin}/api/v1/birthday-2020/team")
				}
				JSON.parse<Json>(result)
			}

			val teamStats = teamStatsJob.await()
			val isActive = teamStats["isActive"] as Boolean
			val currentTeam = teamStats["team"] as String?

			if (currentTeam == null) {
				window.location.replace("/birthday-2020")
				return@launch
			}

			val backgroundsJob = m.async {
				val result = http.get<String> {
					url("${window.location.origin}/api/v1/loritta/backgrounds")
				}
				kotlinx.serialization.json.JSON.nonstrict.decodeFromString(ListSerializer(Background.serializer()), result)
			}

			val profileWrapperJob = m.async {
				val profileWrapper = Image()
				debug("Awaiting load...")
				profileWrapper.awaitLoad("${window.location.origin}/api/v1/users/@me/profile")
				debug("Load complete!")
				profileWrapper
			}

			val backgrounds = backgroundsJob.await()
			val profileWrapper = profileWrapperJob.await()

			val body = document.select<HTMLDivElement>("#birthday-2020")

			body.append {
				div {
					div {
						style = "text-align: center;"
						h1 {
							id = "state"
							style = "color: red;"
						}

						h1 {
							+"Você tem "
							span {
								id = "total-prize-count"
								+"X"
							}
							+" presentes!"
						}

						h2(classes = "has-rainbow-text") {
							+"Team ${currentTeam.lowercase().capitalize()}"
						}

						p {
							+"Colete presentes clicando nos presentes encontrados em mensagens no Discord! Presentes caem aleatoriamente em mensagens de pessoas que estão participando do evento ^-^"
						}

						p {
							+"Mas pegue rápido, se você demorar mais de cinco minutos para coletar, você irá perder o presente para sempre!"
						}

						h2 {
							id = "next-reward-at"
						}
					}

					div {
						id = "next-reward-progress-bar-bg"
						style = "width: 100%; height: 1em; background-color: #b9b909;"

						div {
							id = "next-reward-progress-bar"
							style = "height: 100%; width: 0%; transition: 0.3s; background-color: yellow; box-shadow: yellow 0px 0px 10px, yellow 0px 0px 10px, yellow 0px 0px 10px"
						}
					}

					/* div {
						+ "Recompensas recebidos:"
						div {
							id = "already-received-rewards"
						}
					}

					div {
						+ "Próximas recompensas:"
						div {
							id = "next-rewards"
						}
					} */

					div {
						style = "overflow: auto;\n" +
								"margin: 20px;\n" +
								"background-color: white;\n" +
								"border: 1px solid #00000038;\n" +
								"border-radius: 7px;\n" +
								"padding: 10px;"

						div {
							id = "stuff-log"
							style = "height: 10em; width: 100%:"
						}
					}

					div {
						generateAd("7895601776", "Loritta Birthday 2020 Stats")
					}
				}
			}

			connectToEventSource(currentTeam)
		}
	}

	fun connectToEventSource(currentTeam: String) {
		connectedEventSource?.close()
		state.innerText = "Conectando..."
		val collectedPointsSoundEffects = listOf(
				Audio("/assets/snd/present_get1.ogg"),
				Audio("/assets/snd/present_get2.ogg"),
				Audio("/assets/snd/present_get3.ogg"),
				Audio("/assets/snd/present_get4.ogg")
		)

		val eventSource = EventSource("/api/v1/birthday-2020/stats")
		connectedEventSource = eventSource

		val rewards = if (currentTeam == "PANTUFA") {
			Birthday2020Route.pantufaRewards
		} else {
			Birthday2020Route.gabrielaRewards
		}

		eventSource.addEventListener("open", {
			console.log(it)
			state.innerText = ""
			debug("Open!")
		}, false)

		eventSource.addEventListener("error", {
			debug("Error!")
			console.log(it)
			state.innerText = "Você foi desconectado, espere um tempinho e, se continuar esta mensagem, recarregue a página!"
			m.launch {
				delay(3_000)
				connectToEventSource(currentTeam)
			}
		}, false)

		eventSource.addEventListener("message", {
			console.log(it)

			it as MessageEvent
			it.data as String

			debug(it.data)

			val json = JSON.parse<Json>(it.data as String)
			val type = json["type"] as String?
			if (type == "collectedPoint" || type == "syncPoints" || type == "outdatedPoint") {
				val total = json["total"] as Int
				totalPrizeCount.innerText = total.toString()

				val receivedRewards = rewards.filter { total >= it.requiredPoints }
						.sortedByDescending { it.requiredPoints }
				val nextRewards = rewards.filter { it.requiredPoints > total }
						.sortedBy { it.requiredPoints }

				val lastTierWasAt = receivedRewards.firstOrNull()?.requiredPoints ?: 0

				var recalculateRewards = type == "syncPoints"

				if (type == "collectedPoint") {
					val moment = Moment()
					val sfx = collectedPointsSoundEffects.random()
					sfx.currentTime = 0.0
					sfx.play()

					val hasAnyNewReward = rewards.filter { it.requiredPoints in currentPoints until total }

					stuffLog.prepend {
						div {
							span {
								+ "[${moment.format("HH:mm:ss")}]"
							}
							+ " "
							span {
								+ "Presente coletado!"
							}
						}
					}

					if (hasAnyNewReward.isNotEmpty()) {
						recalculateRewards = true
						rewardSoundEffect.play()
					}
				}

				if (type == "outdatedPoint") {
					val moment = Moment()
					LoriDashboard.configErrorSfx.play()

					stuffLog.prepend {
						div {
							span {
								+ "[${moment.format("HH:mm:ss")}]"
							}
							+ " "
							span {
								+ "Você pegou um presente, mas já se passaram cinco minutos então ele já nem estava mais lá..."
							}
						}
					}
				}

				val nextRewardAtTitle = document.select<HTMLDivElement>("#next-reward-at")

				if (nextRewards.isNotEmpty()) {
					val nextReward = nextRewards.first()
					val percentage = (total.toDouble() - lastTierWasAt) / (nextReward.requiredPoints - lastTierWasAt)
					val nextRewardProgressBar = document.select<HTMLDivElement>("#next-reward-progress-bar")
					nextRewardProgressBar.style.width = "${percentage * 100}%"

					val quantity = nextReward.requiredPoints - total

					nextRewardAtTitle.innerText = "Faltam $quantity presentes para a próxima recompensa!"
				} else {
					nextRewardAtTitle.innerText = "Você já coletou todos os presentes disponíveis! Obrigada por participar do meu evento de aniversário ^-^"
				}

				/* if (recalculateRewards) {
					val alreadyReceivedRewardsDiv = document.select<HTMLDivElement>("#already-received-rewards")
					val nextRewardsDiv = document.select<HTMLDivElement>("#next-rewards")

					alreadyReceivedRewardsDiv.clear()
					nextRewardsDiv.clear()

					receivedRewards.forEach {
						alreadyReceivedRewardsDiv.append {
							div {
								+(it.toString())
							}
						}
					}

					nextRewards.forEach {
						nextRewardsDiv.append {
							div {
								+(it.toString())
							}
						}
					}
				} */
				currentPoints = total
			}
		}, false)
	} */
}